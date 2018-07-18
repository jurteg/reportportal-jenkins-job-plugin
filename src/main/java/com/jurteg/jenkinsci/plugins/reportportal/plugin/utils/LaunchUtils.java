package com.jurteg.jenkinsci.plugins.reportportal.plugin.utils;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
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
import java.util.Date;
import java.util.Set;

public class LaunchUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(LaunchUtils.class);

    /**
     * Start RP launch
     */
    public static Launch startLaunch(ConfigModel config, String launchName, String launchDescription, Set<String> tags) {
        /* should no be lazy */
        final Date startTime = Calendar.getInstance().getTime();
        final ReportPortal reportPortal = ReportPortal.builder().withParameters(getListenerParameters(config)).build();

        StartLaunchRQ rq = new StartLaunchRQ();
        rq.setName(launchName);
        rq.setStartTime(startTime);
        rq.setMode(Mode.DEFAULT);
        rq.setTags(tags);
        rq.setDescription(launchDescription);

        Launch launch = reportPortal.newLaunch(rq);
        return launch;
    }

    public static Maybe<String> startTestItem(Launch rp, Maybe<String> parent, String name, String description, Set<String> tags, String itemType) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setName(name);
        rq.setDescription(description);
        rq.setTags(tags);
        rq.setType(itemType);
        rq.setStartTime(Calendar.getInstance().getTime());
        if (parent != null) {
            return rp.startTestItem(parent, rq);
        } else {
            return rp.startTestItem(rq);
        }
    }

    /**
     * Statuses allowed for RP:
     * "PASSED, FAILED, STOPPED, SKIPPED, RESETED, CANCELLED"
     */
    public static void finishTestItem(Launch rp, Maybe<String> itemId, Run run, boolean reportResult) {
        if (reportResult) {
            reportResult(RunResult.getStatus(run), RunResult.getLog(run));
        }
        if (itemId == null) {
            LOGGER.error("BUG: Trying to finish unspecified test item.");
            return;
        }

        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setStatus(RunResult.getStatus(run));
        rq.setEndTime(Calendar.getInstance().getTime());

        rp.finishTestItem(itemId, rq);
    }

    public static void finishTestItem(Launch rp, Maybe<String> itemId, Run run) {
        finishTestItem(rp, itemId, run, true);
    }

    public static void finishLaunch(Launch launch) {
        FinishExecutionRQ finishLaunchRq = new FinishExecutionRQ();
        finishLaunchRq.setEndTime(Calendar.getInstance().getTime());
        launch.finish(finishLaunchRq);
    }

    /**
     * Report test item result and error (if present)
     *
     * @param status - Jenkins build status
     * @param log    - optional console log to be logged in addition
     */
    public static void reportResult(String status, String log) {
        String level = mapLevel(status);
        if (log != null) {
            sendLog(log, level, null);
        }
    }

    public static void sendLog(final String log, final String level, final SaveLogRQ.File file) {
        ReportPortal.emitLog(new Function<String, SaveLogRQ>() {
            @Override
            public SaveLogRQ apply(String item) {
                SaveLogRQ rq = new SaveLogRQ();
                rq.setMessage(log);
                rq.setTestItemId(item);
                rq.setLevel(level);
                rq.setLogTime(Calendar.getInstance().getTime());
                if (file != null) {
                    rq.setFile(file);
                }
                return rq;
            }
        });
    }

    /**
     * Map Cucumber statuses to RP log levels
     *
     * @param status - Cucumber status
     * @return regular log level
     */
    public static String mapLevel(String status) {
        String mapped = null;
        if ("passed".equalsIgnoreCase(status)) {
            mapped = "INFO";
        } else if ("skipped".equalsIgnoreCase(status)) {
            mapped = "WARN";
        } else {
            mapped = "ERROR";
        }
        return mapped;
    }

    public static ListenerParameters getListenerParameters(ConfigModel config) {
        ListenerParameters listenerParameters = new ListenerParameters(PropertiesLoader.load());
        listenerParameters.setUuid(config.getUuid());
        listenerParameters.setProjectName(config.getProject());
        listenerParameters.setBaseUrl(config.getEndpoint());
        listenerParameters.setEnable(true);
        return listenerParameters;
    }
}
