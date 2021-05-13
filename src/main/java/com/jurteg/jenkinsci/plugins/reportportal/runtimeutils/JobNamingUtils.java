package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.Context;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JobNamingUtils {

    private static final Object ENV_MONITOR = new Object();
    private static final String ENV_VAR_REGEX = "(\\$\\{\\w+})";
    private static final String ENV_VAR_INTERNALS = "\\w+";

    private static Logger LOGGER = LoggerFactory.getLogger(JobNamingUtils.class);

    public static Matcher getMatcher(String string, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(string);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    public static String processEnvironmentVariables(Run run, String string) {
        String tempResult = string;
        Matcher matcher = getMatcher(string, ENV_VAR_REGEX);
        if (matcher != null) {
            while (matcher.find()) {
                tempResult = tempResult.replace(matcher.group(), getEnv(run, matcher.group()));
            }
        }

        return tempResult;
    }

    private static String getEnv(Run run, String variable) {
        Matcher matcher = getMatcher(variable, ENV_VAR_INTERNALS);
        try {
            if (matcher != null) {
                if (matcher.find()) {
                    synchronized (ENV_MONITOR) {
                        String result = Context.getEnvVars(run).get(matcher.group());
                        return !StringUtils.isEmpty(result) ? result : variable;
                    }
                }
            }
        } catch (NullPointerException | ConcurrentModificationException e) {
            LOGGER.error(e.getMessage());
            return variable;
        }
        return variable;
    }


}
