package com.jurteg.jenkinsci.plugins.reportportal;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ExecutableModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.runtime.ReporterThreadFactory;
import com.jurteg.jenkinsci.plugins.reportportal.runtime.RunnableModel;
import hudson.model.Run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import hudson.EnvVars;
import hudson.model.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {

    private static Logger LOGGER = LoggerFactory.getLogger(Context.class);
    private static Map<Run, EnvVars> envVarsMap = new HashMap<>();
    private static List<LaunchModel> runningLaunchModelList = Collections.synchronizedList(new ArrayList<>());
    private static Map<Run, List<RunnableModel>> runnableModelMap = new ConcurrentHashMap<>();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 5, TimeUnit.HOURS,
            new LinkedBlockingQueue<>(), new ReporterThreadFactory());

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static List<LaunchModel> getRunningLaunches() {
        return runningLaunchModelList;
    }

    public static void addRunningLaunches(List<LaunchModel> launches) {
        runningLaunchModelList.addAll(launches);
    }

    public static void removeRunningLaunches(List<LaunchModel> launches) {
        runningLaunchModelList.removeAll(launches);
    }

    public static List<RunnableModel> getRunnableModels(Run run) {
        return runnableModelMap.get(run);
    }

    public static void addRunnableModels(Run run, List<RunnableModel> list) {
        runnableModelMap.put(run, list);
    }

    public static void addRunnableModel(Run run, RunnableModel model) {
        runnableModelMap.get(run).add(model);
    }

    public static void removeRunnableModels(Run run) {
        runnableModelMap.remove(run);
    }

    public static Map<Run, List<RunnableModel>> getRunnableModelMap() {
        return runnableModelMap;
    }

    public static List<LaunchModel> getAllRunningLaunches() {
        List<LaunchModel> completeList = new ArrayList<>();
        completeList.addAll(getRunningLaunches());
        completeList.addAll(getLaunchesFromRunnableList());
        return completeList;
    }

    public static List<LaunchModel> getLaunchesFromRunnableList() {
        Set<Run> keySet = getRunnableModelMap().keySet();
        List<LaunchModel> launchList = new ArrayList<>();
        if (!keySet.isEmpty()) {
            for (Run runKey : keySet) {
                for (RunnableModel runnable : getRunnableModels(runKey)) {
                    ExecutableModel model = runnable.getModel();
                    if (model instanceof LaunchModel) {
                        launchList.add((LaunchModel) model);
                    }
                }
            }
        }
        return launchList;
    }

    public static void setEnvVars(Run run, TaskListener listener) {
        try {
            envVarsMap.put(run, new EnvVars(run.getEnvironment(listener)));
        }catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static EnvVars getEnvVars(Run run) {
        return envVarsMap.get(run);
    }

    public static void removeEnvVars(Run run) {
        envVarsMap.remove(run);
    }

}
