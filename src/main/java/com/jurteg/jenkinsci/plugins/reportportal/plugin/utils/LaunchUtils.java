package com.jurteg.jenkinsci.plugins.reportportal.plugin.utils;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.reportportal.utils.properties.SystemAttributesExtractor;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.jurteg.jenkinsci.plugins.reportportal.RunResult;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import hudson.model.Run;
import io.reactivex.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rp.com.google.common.base.Function;

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static rp.com.google.common.base.Strings.isNullOrEmpty;

public class LaunchUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(LaunchUtils.class);

    private static final String AGENT_PROPERTIES_FILE = "agent.properties";
    private static final String SKIPPED_ISSUE_KEY = "skippedIssue";

    /**
     * Start RP launch
     */
    public Launch startLaunch(ConfigModel config, String name, String description, Set<ItemAttributesRQ> attributes) {
        final ReportPortal reportPortal = ReportPortal.builder().withParameters(getListenerParameters(config)).build();
        ListenerParameters parameters = reportPortal.getParameters();
        StartLaunchRQ launch = new StartLaunchRQ();
        launch.setName(name);
        launch.setStartTime(Calendar.getInstance().getTime());
        launch.setMode(parameters.getLaunchRunningMode());
        launch.setAttributes(attributes);
        launch.getAttributes().addAll(
                SystemAttributesExtractor.extract(AGENT_PROPERTIES_FILE, getClass().getClassLoader()));
        launch.setDescription(description);
        launch.setRerun(parameters.isRerun());
        if (!isNullOrEmpty(parameters.getRerunOf())) {
            launch.setRerunOf(parameters.getRerunOf());
        }

        if (null != parameters.getSkippedAnIssue()) {
            ItemAttributesRQ skippedIssueAttribute = new ItemAttributesRQ();
            skippedIssueAttribute.setKey(SKIPPED_ISSUE_KEY);
            skippedIssueAttribute.setValue(parameters.getSkippedAnIssue().toString());
            skippedIssueAttribute.setSystem(true);
            launch.getAttributes().add(skippedIssueAttribute);
        }

//
//        launch.setName(launchName);
//        launch.setStartTime(startTime);
//        launch.setMode(Mode.DEFAULT);
//        launch.setAttributes(attributes);
//        launch.setDescription(launchDescription);

        return reportPortal.newLaunch(launch);
    }

    public Maybe<String> startTestItem(Launch launch, Maybe<String> parent, String name, String description, Set<ItemAttributesRQ> attributes, String itemType, Optional<String> testCaseId) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setName(name);
        rq.setDescription(description);
        rq.setAttributes(attributes);
        rq.setType(itemType);
        if ("STEP".equals(itemType)) {
            rq.setTestCaseId(testCaseId.orElse(null));
        }
        rq.setStartTime(Calendar.getInstance().getTime());
        if (parent != null) {
            return launch.startTestItem(parent, rq);
        } else {
            return launch.startTestItem(rq);
        }
    }

    /**
     * Statuses allowed for RP:
     * "PASSED, FAILED, STOPPED, SKIPPED, RESETED, CANCELLED"
     */
    public void finishTestItem(Launch rp, Maybe<String> itemId, Run run, boolean reportResult) {
        if (reportResult) {
            reportResult(run);
        }
        if (itemId == null) {
            LOGGER.error("BUG: Trying to finish unspecified test item.");
            return;
        }

        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setStatus(RunResult.getStatus(run).name());
        rq.setEndTime(Calendar.getInstance().getTime());
        rp.finishTestItem(itemId, rq);
    }

    public void finishTestItem(Launch rp, Maybe<String> itemId, Run run) {
        finishTestItem(rp, itemId, run, true);
    }

    public void finishLaunch(Launch launch) {
        FinishExecutionRQ finishLaunchRq = new FinishExecutionRQ();
        finishLaunchRq.setEndTime(Calendar.getInstance().getTime());
        launch.finish(finishLaunchRq);
    }

    /**
     * Report test item result and error (if present)
     *
     * @param run - Jenkins build
     */
    public void reportResult(Run run) {
        String level = RunResult.mapLevel(run.getResult());
        String log = RunResult.getLog(run);
        sendLog(() -> log, level, null);
        if (!run.getCauses().isEmpty()) {
            //sendLog(getStackTraceAsString(result.getError()), level);
        }
    }

    public void sendLog(Supplier<String> logSupplier, final String level, final SaveLogRQ.File file) {
        ReportPortal.emitLog((Function<String, SaveLogRQ>) item -> {
            SaveLogRQ rq = new SaveLogRQ();
            if(logSupplier.get().isEmpty()) {
                LOGGER.error("BUG: Log is empty");
            } else {
                LOGGER.error("Log is not empty. \n" + logSupplier.get());
            }
            rq.setMessage(logSupplier.get());
            rq.setUuid(item);
            rq.setLevel(level);
            rq.setLogTime(Calendar.getInstance().getTime());
            if (file != null) {
                rq.setFile(file);
            }
            return rq;
        });
    }

    public ListenerParameters getListenerParameters(ConfigModel config) {
        ListenerParameters listenerParameters = new ListenerParameters(PropertiesLoader.load());
        listenerParameters.setApiKey(config.getUuid());
        listenerParameters.setProjectName(config.getProject());
        listenerParameters.setBaseUrl(config.getEndpoint());
        listenerParameters.setEnable(true);
        return listenerParameters;
    }
}
