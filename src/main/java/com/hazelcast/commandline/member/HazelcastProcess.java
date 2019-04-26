package com.hazelcast.commandline.member;

import java.io.Serializable;

public class HazelcastProcess
        implements Serializable {
    private final String name;
    private final String loggingPropertiesPath;
    private final String logFilePath;
    private int pid;

    public HazelcastProcess(String name, String loggingPropertiesPath, String logFilePath) {
        this.name = name;
        this.loggingPropertiesPath = loggingPropertiesPath;
        this.logFilePath = logFilePath;
    }

    public String getName() {
        return name;
    }

    public String getLoggingPropertiesPath() {
        return loggingPropertiesPath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
