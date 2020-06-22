///*
// * Copyright 2020 Hazelcast Inc.
// *
// * Licensed under the Hazelcast Community License (the "License"); you may not use
// * this file except in compliance with the License. You may obtain a copy of the
// * License at
// *
// * http://hazelcast.com/hazelcast-community-license
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OF ANY KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations under the License.
// */
//
//package com.hazelcast.commandline.member;
//
//import com.hazelcast.commandline.CommandLineTestSupport;
//import com.hazelcast.commandline.HazelcastCommandLine;
//import com.hazelcast.commandline.ProcessExecutor;
//import com.hazelcast.commandline.test.annotation.AcceptanceTest;
//import com.hazelcast.core.LifecycleEvent;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.experimental.categories.Category;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Comparator;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Stream;
//
//import static junit.framework.TestCase.assertEquals;
//import static junit.framework.TestCase.assertTrue;
//
//@Category(AcceptanceTest.class)
//public class MemberCommandLineAcceptanceTest
//        extends CommandLineTestSupport {
//
//    private final String DEFAULT_CLUSTER_NAME = "hazelcast-commandline-test-cluster";
//    private final String DEFAULT_PORT = "5701";
//    private final String hazelcastHome = System.getProperty("user.home") + "/.hazelcastTest";
//    private HazelcastCommandLine hazelcastCommandLine;
//    private MemberCommandLine memberCommandLine;
//    private BufferedReader bufferedReader;
//
//    @Before
//    public void setup()
//            throws IOException {
//        resetOut();
//        ProcessExecutor processExecutor = new ProcessExecutor();
//        hazelcastCommandLine = new HazelcastCommandLine(out, err, processExecutor, true);
//        memberCommandLine = new MemberCommandLine(out, err, new HazelcastProcessStore(hazelcastHome), processExecutor,
//                true);
//        killAllRunningHazelcastInstances();
//        removeFiles();
//    }
//
//    @After
//    public void close()
//            throws IOException {
//        killAllRunningHazelcastInstances();
//        removeFiles();
//        if (bufferedReader != null) {
//            bufferedReader.close();
//        }
//    }
//
//    private void removeFiles()
//            throws IOException {
//        Path pathToBeDeleted = Files.createDirectories(Paths.get(hazelcastHome));
//
//        Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
//    }
//
//    private void killAllRunningHazelcastInstances() {
//        try {
//            for (HazelcastProcess hazelcastProcess : memberCommandLine.getHazelcastProcessStore().findAll().values()) {
//                runCommand("kill -9 " + hazelcastProcess.getPid());
//            }
//        } catch (Exception e) {
//            //ignored, test instances file might not exist.
//        }
//    }
//
//    @Test(timeout = 10000)
//    public void test_start()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
//        assertTrue(processOutput.anyMatch(out -> out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
//    }
//
//    @Test(timeout = 10000)
//    public void test_start_withConfigFile()
//            throws IOException, InterruptedException {
//        String groupName = "member-command-line-test";
//        startMemberWithConfigFile();
//        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
//        assertTrue(processOutput
//                .anyMatch(out -> out.contains(groupName) && out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
//    }
//
//    @Test(timeout = 10000)
//    public void test_start_withClusterName()
//            throws IOException, InterruptedException {
//        String groupName = "member-command-line-test";
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
//        assertTrue(processOutput
//                .anyMatch(out -> out.contains(groupName) && out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
//    }
//
//    @Test(timeout = 10000)
//    public void test_start_withPort()
//            throws IOException, InterruptedException {
//        String port = "9898";
//        hazelcastCommandLine.start(null, port, null, null, null);
//        Stream<String> processOutput = getProcessOutput(memberCommandLine.getProcessInputStream());
//        assertTrue(processOutput.anyMatch(out -> out.contains(":" + port + " is " + LifecycleEvent.LifecycleState.STARTED.toString())));
//    }
//
//    @Test
//    public void test_start_withClasspath()
//            throws IOException, InterruptedException {
//        String[] classpath = { "Test1st.jar", "Test2nd.jar" };
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, classpath, null);
//        String processUniqueId = captureOut().replace("\n", "");
//        int pid = memberCommandLine.getHazelcastProcessStore().find(processUniqueId).getPid();
//        String javaProcess = getRunningJavaProcess(pid);
//        for (String s : classpath) {
//            assertTrue(javaProcess.contains(s));
//        }
//    }
//
//    @Test
//    public void test_stop()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueID = captureOut().replace("\n", "");
//        int pid = memberCommandLine.getHazelcastProcessStore().find(processUniqueID).getPid();
//        memberCommandLine.stop(processUniqueID);
//        assertTrue(!getRunningJavaProcesses().contains(String.valueOf(pid)));
//    }
//
//    @Test
//    public void test_list()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId1 = captureOut().replace("\n", "");
//        resetOut();
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId2 = captureOut().replace("\n", "");
//        resetOut();
//        memberCommandLine.stop(processUniqueId2);
//        resetOut();
//        memberCommandLine.list("", false, false);
//        String out = captureOut();
//        assertTrue(out.contains(processUniqueId1));
//        assertTrue(out.contains(processUniqueId2));
//    }
//
//    @Test
//    public void test_list_namesOnly()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId1 = captureOut().replace("\n", "");
//        resetOut();
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId2 = captureOut().replace("\n", "");
//        resetOut();
//        memberCommandLine.list("", true, false);
//        String out = captureOut();
//        assertTrue(out.equals(processUniqueId1 + "\n" + processUniqueId2 + "\n") || out.equals(processUniqueId2 + "\n" + processUniqueId1 + "\n"));
//    }
//
//    @Test
//    public void test_list_runningOnly()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId1 = captureOut().replace("\n", "");
//        resetOut();
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId2 = captureOut().replace("\n", "");
//        resetOut();
//        memberCommandLine.stop(processUniqueId2);
//        resetOut();
//        memberCommandLine.list("", false, true);
//        String out = captureOut();
//        assertTrue(out.contains(processUniqueId1));
//        assertTrue(!out.contains(processUniqueId2));
//    }
//
//    @Test
//    public void test_list_namesAndRunningOnly()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId1 = captureOut().replace("\n", "");
//        resetOut();
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId2 = captureOut().replace("\n", "");
//        resetOut();
//        memberCommandLine.stop(processUniqueId2);
//        resetOut();
//        memberCommandLine.list("", true, true);
//        String out = captureOut();
//        assertEquals(out, processUniqueId1 + "\n");
//    }
//
//    @Test
//    public void test_list_withName()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId1 = captureOut().replace("\n", "");
//        resetOut();
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId2 = captureOut().replace("\n", "");
//        resetOut();
//        memberCommandLine.list(processUniqueId1, false, false);
//        String out = captureOut();
//        assertTrue(out.contains(processUniqueId1));
//        assertTrue(!out.contains(processUniqueId2));
//    }
//
//    @Test
//    public void test_list_withUnknownName()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId1 = captureOut().replace("\n", "");
//        resetOut();
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId2 = captureOut().replace("\n", "");
//        resetOut();
//        memberCommandLine.list("0123456789", false, false);
//        String out = captureOut();
//        String err = captureErr();
//        assertTrue(!out.contains(processUniqueId1));
//        assertTrue(!out.contains(processUniqueId2));
//        assertTrue(err.contains("No process found"));
//    }
//
//    @Test
//    public void test_logs()
//            throws IOException, InterruptedException {
//        String groupName = "member-command-line-test";
//        startMemberWithConfigFile();
//        String processUniqueId = captureOut().replace("\n", "");
//        resetOut();
//        //await for the logs to be created
//        TimeUnit.SECONDS.sleep(5);
//        assertTrue(Files.exists(Paths.get(hazelcastHome + "/" + processUniqueId + "/logs/hazelcast.log")));
//        memberCommandLine.logs(processUniqueId, 1000);
//        assertTrue(captureOut().contains(groupName));
//    }
//
//    @Test
//    public void test_logs_withLineCount()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start(null, DEFAULT_PORT, null, null, null);
//        String processUniqueId = captureOut().replace("\n", "");
//        resetOut();
//        //await for the logs to be created
//        TimeUnit.SECONDS.sleep(5);
//        assertTrue(Files.exists(Paths.get(hazelcastHome + "/" + processUniqueId + "/logs/hazelcast.log")));
//        int numberOfLines = 10;
//        memberCommandLine.logs(processUniqueId, numberOfLines);
//        int outputLength = captureOut().split("\\n").length;
//        assertEquals("Not expected number of lines in logs.", numberOfLines, outputLength);
//    }
//
//    private void startMemberWithConfigFile()
//            throws IOException, InterruptedException {
//        hazelcastCommandLine.start("src/test-acceptance/resources/test-hazelcast.xml", null, null, null, null);
//    }
//
//    private String getRunningJavaProcesses()
//            throws IOException {
//        return runCommand("jps");
//    }
//
//    private String getRunningJavaProcess(int pid)
//            throws IOException {
//        return runCommand("ps auxww " + pid);
//    }
//
//    private String runCommand(String command)
//            throws IOException {
//        Process exec = Runtime.getRuntime().exec(command);
//        BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
//
//        StringBuilder stringBuilder = new StringBuilder();
//        String s;
//        while ((s = stdInput.readLine()) != null) {
//            stringBuilder.append(s).append("\n");
//        }
//
//        return stringBuilder.toString();
//    }
//
//    private Stream<String> getProcessOutput(InputStream processInputStream) {
//        bufferedReader = new BufferedReader(new InputStreamReader(processInputStream, StandardCharsets.UTF_8));
//        return bufferedReader.lines();
//    }
//}
