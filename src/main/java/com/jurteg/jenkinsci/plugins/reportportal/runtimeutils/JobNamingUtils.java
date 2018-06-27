package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobNamingUtils {

    public static boolean anyOfBuildsMatchPattern(String jobName, String regex) {
        for (String name : getBuildNames(jobName)) {
            if (getMatcher(name, regex).find()) {
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
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static Matcher getMatcher(String string, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(string);
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


}
