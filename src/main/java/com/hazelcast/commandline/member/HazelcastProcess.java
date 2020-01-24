/*
* Copyright 2020 Hazelcast Inc.
*
* Licensed under the Hazelcast Community License (the "License"); you may not use
* this file except in compliance with the License. You may obtain a copy of the
* License at
*
* http://hazelcast.com/hazelcast-community-license
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OF ANY KIND, either express or implied. See the License for the
* specific language governing permissions and limitations under the License.
*/

package com.hazelcast.commandline.member;

import java.time.Instant;

/**
 * The details of Hazelcast process initiated by command line tool.
 * <p/>
 * NOTE: This class is used for persisting the process information. Backward-compatibility is provided for it, but
 * remember that the changes to this class won't be available for the old versions. For instance; if you add a new
 * field and read from an old version of this class, the new field will have a {@code null} value.
 * */
public class HazelcastProcess {

    /**
     * Process status
     */
    public enum Status {
        /** after deserialization */
        UNKNOWN,
        /** after creation */
        CREATED,
        /** running on OS */
        RUNNING,
        /** not running on OS */
        STOPPED,
    }

    private String name;
    private String loggingPropertiesPath;
    private String logFilePath;
    private int pid;
    private String clusterName;
    private final Instant creationInstant;
    private transient Status status;

    /**
     * This constructor is only used by Kryo for deserialization purposes.
     */
    public HazelcastProcess() {
        status = Status.UNKNOWN;
        creationInstant = null;
    }

    public HazelcastProcess(String name, String loggingPropertiesPath, String logFilePath) {
        this.name = name;
        this.loggingPropertiesPath = loggingPropertiesPath;
        this.logFilePath = logFilePath;
        this.status = Status.CREATED;
        this.creationInstant = Instant.now();
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Instant getCreationInstant() {
        return creationInstant;
    }
}
