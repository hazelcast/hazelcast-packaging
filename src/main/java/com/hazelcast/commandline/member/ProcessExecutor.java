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

import com.hazelcast.core.HazelcastException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Handler for OS level process operations.
 */
public class ProcessExecutor {

    Process buildAndStart(List<String> commandList, boolean foreground)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.redirectErrorStream(true);
        if (foreground) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        Process process = processBuilder.start();
        if (foreground) {
            process.waitFor();
        }
        return process;
    }

    int exec(List<String> commandList) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        return process.waitFor();
    }

    /**
     * Extracts the PID of a process using reflection. Note that this method only works in Unix-like environments.
     *
     * @param process that the PID to be extracted
     * @return PID of the process
     */
    int extractPid(Process process) {
        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return f.getInt(process);
        } catch (Throwable e) {
            throw new HazelcastException("Exception when accessing the pid of a process.", e);
        }
    }

    boolean isRunning(int pid) throws IOException, InterruptedException {
        return 0 == exec(Arrays.asList("ps", "-p", String.valueOf(pid)));
    }

}

