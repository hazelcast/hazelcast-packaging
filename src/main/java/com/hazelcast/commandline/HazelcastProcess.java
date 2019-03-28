package com.hazelcast.commandline;

import java.io.Serializable;

public class HazelcastProcess implements Serializable {
    private int pid;

    public HazelcastProcess(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }
}
