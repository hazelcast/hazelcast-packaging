package com.hazelcast.commandline.member;

import java.io.Serializable;

public class HazelcastProcess implements Serializable {
    private String processUniqueId;
    private int pid;

    public HazelcastProcess(String processUniqueId, int pid) {
        this.processUniqueId = processUniqueId;
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }

    public String getProcessUniqueId() {
        return processUniqueId;
    }
}
