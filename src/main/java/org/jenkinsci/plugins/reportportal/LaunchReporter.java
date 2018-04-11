package org.jenkinsci.plugins.reportportal;

import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.reactivex.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rp.com.google.common.base.Function;

import java.util.Calendar;
import java.util.Date;

public class LaunchReporter {

    private static Logger LOGGER = LoggerFactory.getLogger(LaunchReporter.class);

    protected Supplier<Launch> RP;
    protected Supplier<Maybe<String>> rootSuiteId;
    protected Maybe<String> currentTestItemId;
    protected ReporterSettings settings;


    public LaunchReporter(ReporterSettings settings) {
        this.settings = settings;
    }

    public void beforeLaunch() {
        startLaunch();
        beforeSuite();
    }

    protected void beforeSuite() {
        startRootItem();
        startTestItem();
    }

    protected void afterSuite(String status, String consoleLog) {
        reportResult(status, consoleLog);
        finishTestItem(currentTestItemId, status);
        finishSuite();
    }

    /**
     * Finish RP launch
     */
    protected void afterLaunch(String status, String consoleLog) {
        afterSuite(status, consoleLog);
        finishLaunch();
    }

    /**
     * Start RP launch
     */
    protected void startLaunch() {
        RP = Suppliers.memoize(new Supplier<Launch>() {

            /* should no be lazy */
            private final Date startTime = Calendar.getInstance().getTime();

            @Override
            public Launch get() {
                final ReportPortal reportPortal = ReportPortal.builder().withParameters(settings.getListenerParameters()).build();

                StartLaunchRQ rq = new StartLaunchRQ();
                rq.setName(settings.getLaunchName());
                rq.setStartTime(startTime);
                rq.setMode(Mode.DEFAULT);
                rq.setTags(settings.getTags());
                rq.setDescription(settings.getLaunchDescription());

                Launch launch = reportPortal.newLaunch(rq);
                return launch;
            }
        });
    }

    /**
     * Start root suite
     */
    protected void startRootItem() {
        if (settings.getSuiteName() == null) {
            rootSuiteId = Suppliers.memoize(new Supplier<Maybe<String>>() {
                @Override
                public Maybe<String> get() {
                    return null;
                }
            });
        } else {
            final Date startTime = Calendar.getInstance().getTime();
            rootSuiteId = Suppliers.memoize(new Supplier<Maybe<String>>() {

                @Override
                public Maybe<String> get() {
                    StartTestItemRQ rq = new StartTestItemRQ();
                    rq.setName(settings.getSuiteName());
                    rq.setStartTime(startTime);
                    rq.setType("STORY");
                    return RP.get().startTestItem(rq);
                }
            });
        }
    }

    protected void startTestItem() {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setDescription(settings.getLaunchDescription());
        rq.setName(settings.getTestName());
        rq.setTags(settings.getTags());
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("STEP");
        if (rootSuiteId.get() != null) {
            currentTestItemId = RP.get().startTestItem(rootSuiteId.get(), rq);
        } else {
            currentTestItemId = RP.get().startTestItem(rq);
        }
    }

    protected void finishTestItem(Maybe<String> itemId, String status) {
        finishTestItem(RP.get(), itemId, status);
    }

    /**
     * Statuses allowed for RP:
     * "PASSED, FAILED, STOPPED, SKIPPED, RESETED, CANCELLED"
     */
    protected void finishTestItem(Launch rp, Maybe<String> itemId, String status) {
        if (itemId == null) {
            LOGGER.error("BUG: Trying to finish unspecified test item.");
            return;
        }

        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setStatus(status);
        rq.setEndTime(Calendar.getInstance().getTime());

        rp.finishTestItem(itemId, rq);
    }

    /**
     * Start root suite
     */
    protected void finishSuite() {
        finishTestItem(RP.get(), rootSuiteId.get(), null);
        rootSuiteId = null;
    }

    protected void finishLaunch() {
        FinishExecutionRQ finishLaunchRq = new FinishExecutionRQ();
        finishLaunchRq.setEndTime(Calendar.getInstance().getTime());
        RP.get().finish(finishLaunchRq);
    }

    /**
     * Report test item result and error (if present)
     *
     * @param status - Jenkins build status
     * @param log    - optional console log to be logged in addition
     */
    protected void reportResult(String status, String log) {
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
        if (status.equalsIgnoreCase("passed")) {
            mapped = "INFO";
        } else if (status.equalsIgnoreCase("skipped")) {
            mapped = "WARN";
        } else {
            mapped = "ERROR";
        }
        return mapped;
    }


}
