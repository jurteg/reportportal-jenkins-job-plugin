package com.jurteg.jenkinsci.plugins.reportportal.plugin.utils;


import hudson.model.Item;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

public class JenkinsJobUtils {

    public static List<String> getJenkinsJobNamesList() {
        List<String> jobNamesList = new ArrayList<>();
        jobNamesList.addAll(Jenkins.get().getJobNames());
        return jobNamesList;
    }

    public static List<String> getJobChildrenNamesList(String name) {
        List<String> chldrenNamesList = new ArrayList<>();
        Item job = getJobByName(name);
        if(job != null) {
            for(Item child : job.getAllJobs()) {
                chldrenNamesList.add(child.getName());
            }
        }
      return chldrenNamesList;
    }

    private static Item getJobByName(String name) {
        return Jenkins.get().getItem(name);
    }

}
