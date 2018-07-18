package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ExecutableModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.runtime.ReporterThreadFactory;
import com.jurteg.jenkinsci.plugins.reportportal.runtime.RunnableModel;
import hudson.model.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RunUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(RunUtils.class);
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 5, TimeUnit.HOURS,
            new LinkedBlockingQueue<>(), new ReporterThreadFactory());
    private static Map<Run, List<RunnableModel>> runnableModelMap = new ConcurrentHashMap<>();

    public RunUtils() {
        executor.prestartAllCoreThreads();
    }

    public static void runExistingDownstreamJobsOrCreateSiblings(Run run, List<LaunchModel> launchModelListToRunFrom) {
        boolean isReportingMoreThanOnce = false;
        List<LaunchModel> completeList = new ArrayList<>();
        completeList.addAll(launchModelListToRunFrom);
        completeList.addAll(getLaunchesFromRunnableList());
        List<JobModel> existingModels = ModelUtils.getDownStreamJobsFromModelsByName(completeList, getRunJobName(run));
        int instanceCounter;
        for (JobModel jobModel : existingModels) {
            if (!jobModel.isClone()) {
                if (jobModel.getRpTestItemId() != null) { //is running
                    if (!jobModel.getRun().getFullDisplayName().equals(run.getFullDisplayName()) && ModelUtils.haveSameParent(jobModel, run)) {
                        instanceCounter = getRunningClones(jobModel).size() + 1;
                        try {
                            DownStreamJobModel newJobModel = (DownStreamJobModel) ((DownStreamJobModel) jobModel).clone();
                            newJobModel.setRun(run);
                            ((JobModel) jobModel.getParent()).addDownStreamJobModel(newJobModel);
                            if (getRunJobName(newJobModel.getRun()).equals(getRunJobName(jobModel.getRun())) &&
                                    newJobModel.getComposedName().equals(((DownStreamJobModel) jobModel).getComposedName())) {
                                newJobModel.setRpTestItemName(newJobModel.getRpTestItemName() + String.format(" [%d]", instanceCounter));
                            }
                            createRunnableAndExecute(newJobModel, run);
                        } catch (CloneNotSupportedException e) {
                            LOGGER.error(String.format("Unable to clone test item with name '%s'.", jobModel.getRpTestItemName()));
                        }
                    }
                } else {
                    jobModel.setRun(run);
                    if (isReportingMoreThanOnce || isAlreadyRunningAsLaunch(jobModel, completeList)) {
                        createRunnableAndExecute(jobModel, run);
                    } else {
                        jobModel.start();
                        isReportingMoreThanOnce = true;
                    }
                }
            }
        }
    }

    private static List<JobModel> getRunningClones(JobModel model) {
        List<JobModel> cloneList = new ArrayList<>();
        JobModel parent = (JobModel)model.getParent();
        for(JobModel jobModel : parent.getDownStreamJobModelList()) {
            if(getRunJobName(model.getRun()).equals(getRunJobName(jobModel.getRun())) && jobModel.isClone()) {
                cloneList.add(jobModel);
            }
        }
        return cloneList;
    }

    private static boolean isAlreadyRunningAsLaunch(JobModel model, List<LaunchModel> launchModelListToLook) {
        for (LaunchModel launch : launchModelListToLook) {
            if (launch.getUpstreamJobModel().getRun().equals(model.getRun())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAlreadyRunningAsItem(LaunchModel model, Run run, List<LaunchModel> launchModelListToLook) {
        List<JobModel> existingModels = ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToLook, getRunJobName(run));
        for (JobModel job : existingModels) {
            if (model.getUpstreamJobModel().getRun().equals(job.getRun()) && job.getRpTestItemId() != null) {
                return true;
            }
        }
        return false;
    }

    private static void createRunnableAndExecute(ExecutableModel executable, Run run) {
        RunnableModel runnable = new RunnableModel(executable, run);
        executor.execute(runnable);
        addNewRunnableModel(run, runnable);
    }

    private static void addNewRunnableModel(Run run, RunnableModel model) {
        if (runnableModelMap.get(run) == null) {
            List<RunnableModel> emptyList = new ArrayList<>();
            runnableModelMap.put(run, emptyList);
        }
        runnableModelMap.get(run).add(model);
    }

    private static void finishRunnableModel(Run run, ExecutableModel model) {
        List<RunnableModel> itemsToRemove = new ArrayList<>();
        List<RunnableModel> list = runnableModelMap.get(run);
        if (list != null) {
            try {
                for (RunnableModel runnable : runnableModelMap.get(run)) {
                    if (runnable.getModel().equals(model)) {
                        itemsToRemove.add(runnable);
                        while (!runnable.done()) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    }
                }
            } finally {
                list.removeAll(itemsToRemove);
                if (runnableModelMap.get(run) == null || runnableModelMap.get(run).isEmpty()) {
                    runnableModelMap.remove(run);
                }
            }
        }
    }

    private static List<LaunchModel> getLaunchesFromRunnableList() {
        Set<Run> list = runnableModelMap.keySet();
        List<LaunchModel> launchList = new ArrayList<>();
        if (!list.isEmpty()) {
            for (Run runKey : runnableModelMap.keySet()) {
                for (RunnableModel runnable : runnableModelMap.get(runKey)) {
                    ExecutableModel model = runnable.getModel();
                    if (model instanceof LaunchModel) {
                        launchList.add((LaunchModel) model);
                    }
                }
            }
        }
        return launchList;
    }

    public static void finishRunningDownStreamJobs(Run run, List<LaunchModel> launchModelListToRunFrom) {
        List<LaunchModel> completeList = new ArrayList<>();
        completeList.addAll(launchModelListToRunFrom);
        completeList.addAll(getLaunchesFromRunnableList());
        for (JobModel jobModel : ModelUtils.getDownStreamJobsFromModelsByName(completeList, getRunJobName(run))) {
            if (run.equals(jobModel.getRun()) && jobModel.getRpTestItemId() != null) { //running
                if (isRunnableModel(jobModel, run)) {
                    finishRunnableModel(run, jobModel);
                } else {
                    jobModel.finish();
                }
            }
        }
    }

    private static boolean isRunnableModel(ExecutableModel model, Run run) {
        List<RunnableModel> list = runnableModelMap.get(run);
        if (list != null) {
            for (RunnableModel runnable : list) {
                if (runnable.getModel().equals(model)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void finishRunningLaunches(Run run, List<LaunchModel> launchesToFinish) {
        List<LaunchModel> completeList = new ArrayList<>();
        completeList.addAll(launchesToFinish);
        completeList.addAll(getLaunchesFromRunnableList());
        try {
            for (LaunchModel model : completeList) {
                if (run.equals(model.getRun()) && model.getLaunch() != null) {
                    if (isRunnableModel(model, run)) {
                        finishRunnableModel(run, model);
                    } else {
                        model.finish();
                    }
                }
            }
        } finally {
            runnableModelMap.remove(run);
        }
        LOGGER.error(String.format("Number of threads: %d", ManagementFactory.getThreadMXBean().getThreadCount()));
    }

    public static List<LaunchModel> runNewLaunches(Run run, List<LaunchModel> runningLaunchesList, ConfigModel config) {
        List<LaunchModel> newLaunchesToRun = new ArrayList<>();
        List<LaunchModel> completeList = new ArrayList<>();
        completeList.addAll(runningLaunchesList);
        completeList.addAll(getLaunchesFromRunnableList());
        boolean hasSibling = false;
        for (LaunchModel model : ModelUtils.getLaunchModelListToRun(run, config)) {
            if (!isAlreadyRunningInList(model, completeList)) {
                if (model.isReportingEnabled()) {
                    if (!hasSibling && !isAlreadyRunningAsItem(model, run, runningLaunchesList)) {
                        model.start();
                        newLaunchesToRun.add(model);
                        hasSibling = true;
                    } else {
                        createRunnableAndExecute(model, run);
                    }
                }
            }
        }
        return newLaunchesToRun;
    }


    public static String getRunJobName(Run run) {
        return run.getFullDisplayName().replace(run.getSearchName(), "").trim();
    }

    public static boolean isAlreadyRunningInList(LaunchModel launchModel, List<LaunchModel> launchModelListToCompareWith) {
        for (LaunchModel model : launchModelListToCompareWith) {
            if (model.equals(launchModel)) {
                return true;
            }
        }
        return false;
    }

}
