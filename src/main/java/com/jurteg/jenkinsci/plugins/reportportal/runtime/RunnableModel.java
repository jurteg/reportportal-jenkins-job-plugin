package com.jurteg.jenkinsci.plugins.reportportal.runtime;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ExecutableModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import hudson.model.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableModel implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(RunnableModel.class);

    private final Object monitor = new Object();
    private ExecutableModel model;
    private ConfigModel config;
    private Run run;
    private volatile boolean done;

    public RunnableModel(ExecutableModel model, Run run) {
        this.model = model;
        this.run = run;
    }

    @Override
    public void run() {
        //the mess will happen here
        String name;
        if (model instanceof LaunchModel) {
            name = ((LaunchModel) model).getComposedName();
        } else {
            name = ((JobModel) model).getRpTestItemName();
        }

        LOGGER.info(String.format("Starting '%s'. In thread '%s' / %s", name, Thread.currentThread().getName(), Thread.currentThread().getId()));
        model.start();

       /*
        while (!done) {
            try {
                synchronized (monitor) {
                    Thread.sleep(1000); //TODO: change me
                }
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        */

        try {
            synchronized (monitor) {
                monitor.wait();
            }
            LOGGER.info(String.format("Finishing '%s'. In thread '%s' / %s", name, Thread.currentThread().getName(), Thread.currentThread().getId()));
            model.finish();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            done = true;
        }
    }

    public boolean done() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
        return done;
    }

    public ExecutableModel getModel() {
        return model;
    }
}
