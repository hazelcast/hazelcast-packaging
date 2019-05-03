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

import com.hazelcast.commandline.CommandLineTestSupport;
import com.hazelcast.core.LifecycleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MemberCommandLineTest
        extends CommandLineTestSupport {

    private final String DEFAULT_CLUSTER_NAME = "hazelcast-commandline-test-cluster";
    private final String DEFAULT_PORT = "5701";
    private final String hazelcastHome = System.getProperty("user.home") + "/.hazelcastTest";
    private MemberCommandLine memberCommandLine;

    @Before
    public void setup()
            throws IOException {
        resetOut();
        memberCommandLine = new MemberCommandLine(out, err, hazelcastHome, true);
        killAllRunningHazelcastInstances();
        removeFiles();
    }

    @After
    public void close()
            throws IOException {
        killAllRunningHazelcastInstances();
        removeFiles();
    }

    private void removeFiles()
            throws IOException {
        Path pathToBeDeleted = Files.createDirectories(Paths.get(hazelcastHome));

        Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    private void killAllRunningHazelcastInstances() {
        try {
            for (HazelcastProcess hazelcastProcess : memberCommandLine.getProcessStore().findAll().values()) {
                runCommand("kill -9 " + hazelcastProcess.getPid());
            }
        } catch (Exception e) {
            //ignored, test instances file might not exist.
        }
    }

    @Test(timeout = 10000)
    public void test_start()
            throws IOException, InterruptedException {
        memberCommandLine.start(null, DEFAULT_CLUSTER_NAME, DEFAULT_PORT, null, false, null, null);
        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
        assertTrue(processOutput.anyMatch(out -> out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test(timeout = 10000)
    public void test_start_withConfigFile()
            throws IOException, InterruptedException {
        String groupName = "member-command-line-test";
        startMemberWithConfigFile();
        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
        assertTrue(processOutput
                .anyMatch(out -> out.contains(groupName) && out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test(timeout = 10000)
    public void test_start_withClusterName()
            throws IOException, InterruptedException {
        String groupName = "member-command-line-test";
        memberCommandLine.start(null, groupName, DEFAULT_PORT, null, false, null, null);
        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
        assertTrue(processOutput
                .anyMatch(out -> out.contains(groupName) && out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test(timeout = 10000)
    public void test_start_withPort()
            throws IOException, InterruptedException {
        String port = "9898";
        memberCommandLine.start(null, DEFAULT_CLUSTER_NAME, port, null, false, null, null);
        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
        assertTrue(processOutput.anyMatch(out -> out.contains(port + " is " + LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test
    public void test_stop()
            throws IOException, InterruptedException {
        memberCommandLine.start(null, DEFAULT_CLUSTER_NAME, DEFAULT_PORT, null, false, null, null);
        String processUniqueID = captureOut().replace("\n", "");
        int pid = memberCommandLine.getProcessStore().find(processUniqueID).getPid();
        memberCommandLine.stop(processUniqueID);
        assertTrue(!getRunningJavaProcesses().contains(String.valueOf(pid)));
    }

    @Test
    public void test_list()
            throws IOException, InterruptedException {
        memberCommandLine.start(null, DEFAULT_CLUSTER_NAME, DEFAULT_PORT, null, false, null, null);
        String processUniqueId1 = captureOut().replace("\n", "");
        resetOut();
        memberCommandLine.start(null, DEFAULT_CLUSTER_NAME, DEFAULT_PORT, null, false, null, null);
        String processUniqueId2 = captureOut().replace("\n", "");
        resetOut();
        memberCommandLine.list();
        String out = captureOut();
        assertTrue(out.contains(processUniqueId1));
        assertTrue(out.contains(processUniqueId2));
    }

    @Test
    public void test_logs()
            throws IOException, InterruptedException {
        String groupName = "member-command-line-test";
        startMemberWithConfigFile();
        String processUniqueId = captureOut().replace("\n", "");
        resetOut();
        //await for the logs to be created
        TimeUnit.SECONDS.sleep(5);
        assertTrue(Files.exists(Paths.get(hazelcastHome + "/" + processUniqueId + "/logs/hazelcast.log")));
        memberCommandLine.logs(processUniqueId, 1000);
        assertTrue(captureOut().contains(groupName));
    }

    @Test
    public void test_logs_withLineCount()
            throws IOException, InterruptedException {
        memberCommandLine.start(null, DEFAULT_CLUSTER_NAME, DEFAULT_PORT, null, false, null, null);
        String processUniqueId = captureOut().replace("\n", "");
        resetOut();
        //await for the logs to be created
        TimeUnit.SECONDS.sleep(5);
        assertTrue(Files.exists(Paths.get(hazelcastHome + "/" + processUniqueId + "/logs/hazelcast.log")));
        int numberOfLines = 10;
        memberCommandLine.logs(processUniqueId, numberOfLines);
        int outputLength = captureOut().split("\\n").length;
        assertEquals("Not expected number of lines in logs.", numberOfLines, outputLength);
    }

    private void startMemberWithConfigFile()
            throws IOException, InterruptedException {
        memberCommandLine.start("src/test/resources/test-hazelcast.xml", null, null, null, false, null, null);
    }

    private String getRunningJavaProcesses()
            throws IOException {
        return runCommand("jps");
    }

    private String runCommand(String command)
            throws IOException {
        Process exec = Runtime.getRuntime().exec(command);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String s;
        while ((s = stdInput.readLine()) != null) {
            stringBuilder.append(s).append("\n");
        }

        return stringBuilder.toString();
    }

    private Stream<String> getProcessOutput(InputStream processInputStream)
            throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(processInputStream, StandardCharsets.UTF_8))) {
            return bufferedReader.lines();
        }
    }
}
