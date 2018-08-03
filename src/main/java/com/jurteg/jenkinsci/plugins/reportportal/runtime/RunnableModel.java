package com.jurteg.jenkinsci.plugins.reportportal.runtime;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ExecutableModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableModel implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(RunnableModel.class);

    private final Object monitor = new Object();
    private ExecutableModel model;
    private boolean isRunning;
    private volatile boolean done;

    public RunnableModel(ExecutableModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        String name;
        String type;
        if (model instanceof LaunchModel) {
            name = ((LaunchModel) model).getComposedName();
            type = "Launch";
        } else {
            name = ((JobModel) model).getComposedName();
            type = "Item";
        }
        try {
            LOGGER.info(String.format("Starting %s '%s'. In thread '%s' / %s", type, name, Thread.currentThread().getName(), Thread.currentThread().getId()));
            model.start();
            isRunning = true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            isRunning = true;
        }
        try {
            synchronized (monitor) {
                monitor.wait();
            }
            LOGGER.info(String.format("Finishing %s '%s'. In thread '%s' / %s", type, name, Thread.currentThread().getName(), Thread.currentThread().getId()));
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

    public boolean isRunning() {
        return isRunning;
    }

    public ExecutableModel getModel() {
        return model;
    }
}
