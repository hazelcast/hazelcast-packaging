/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.commandline.member;

import java.io.Serializable;

/**
 * The details of Hazelcast process initiated by command line tool
 */
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
