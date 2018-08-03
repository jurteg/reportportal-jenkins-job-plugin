package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JobNamingUtils {

    private static final Object ENV_MONITOR = new Object();
    private static final String ENV_VAR_REGEX = "(\\$\\{\\w+})";
    private static final String ENV_VAR_INTERNALS = "\\w+";

    private static Logger LOGGER = LoggerFactory.getLogger(JobNamingUtils.class);

    public static boolean anyOfBuildsMatchPattern(String jobName, String regex) {
        for (String name : getBuildNames(jobName)) {
            Matcher matcher = getMatcher(name, regex);
            if (matcher == null) {
                return false;
            }
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public static String getFirstMatchingBuildWithPatternApplied(String jobName, String regex) {
        for (String name : getBuildNames(jobName)) {
            String result = getResultedString(name, regex);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }

    public static String getResultedString(String string, String pattern) {
        Matcher matcher = getMatcher(string, pattern);
        if (matcher == null) {
            return "";
        }
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static Matcher getMatcher(String string, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(string);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    public static List<String> getBuildNames(String jobName) {
        List<String> buildNames = new ArrayList<>();
        RunList runList = ((Job) Jenkins.getInstance().getItem(jobName)).getBuilds();
        if (runList != null) {
            for (Object run : runList) {
                buildNames.add(((Run) run).getDisplayName());
            }
        }
        return buildNames;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////


    public static String processEnvironmentVariables(Run run, TaskListener listener, String string) {
        String tempResult = string;
        Matcher matcher = getMatcher(string, ENV_VAR_REGEX);
        if (matcher != null) {
            while (matcher.find()) {
                tempResult = tempResult.replace(matcher.group(), getEnv(run, listener, matcher.group()));
            }
        }

        return tempResult;
    }

    private static String getEnv(Run run, TaskListener listener, String variable) {
        Matcher matcher = getMatcher(variable, ENV_VAR_INTERNALS);
        try {
            if (matcher != null) {
                if (matcher.find()) {
                    synchronized (ENV_MONITOR) {
                        String result = run.getEnvironment(listener).get(matcher.group());
                        return !StringUtils.isEmpty(result) ? result : variable;
                    }
                }
            }
        } catch (IOException | InterruptedException | NullPointerException | ConcurrentModificationException e) {
            LOGGER.error(e.getMessage());
            return variable;
        }
        return variable;
    }


}
