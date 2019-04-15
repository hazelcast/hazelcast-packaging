package com.hazelcast.commandline;

import com.hazelcast.core.LifecycleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

public class MemberCommandLineTest extends CommandLineTestSupport {

    private MemberCommandLine memberCommandLine;

    @Before
    public void setup() {
        resetOut();
        memberCommandLine = new MemberCommandLine(out, err);
    }

    @After
    public void close() throws IOException, InterruptedException {
        killAllRunningInstances();
        removeFiles();
    }

    private void removeFiles() throws IOException {
        Path pathToBeDeleted = Files.createDirectories(Paths.get(HazelcastCommandLine.HAZELCAST_HOME));

        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private void killAllRunningInstances() throws IOException, InterruptedException {
        String out = getRunningProcesses();
        List<String> pids = getPids(out);
        runCommand("/bin/kill -9 " + String.join(" ", pids));

    }

    private List<String> getPids(String out) {
        List<String> pids = new ArrayList<>();
        for (String line : out.split("\n")) {
            if (line.contains(HazelcastMember.class.getSimpleName())) {
                pids.add(line.split(" ")[0]);
            }
        }
        return pids;
    }

    @Test(timeout = 10000)
    public void test_start() throws IOException, ClassNotFoundException, InterruptedException {
        memberCommandLine.start(null, null, null, null, false, null);
        assertTrue(memberCommandLine.getProcessOutput().anyMatch(out ->
                out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test(timeout = 10000)
    public void test_start_withConfigFile() throws IOException, ClassNotFoundException, InterruptedException {
        String groupName = "member-command-line-test";
        startMemberWithConfigFile();
        assertTrue(memberCommandLine.getProcessOutput().anyMatch(out ->
                out.contains(groupName) && out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test(timeout = 10000)
    public void test_start_withClusterName() throws IOException, ClassNotFoundException, InterruptedException {
        String groupName = "member-command-line-test";
        memberCommandLine.start(null, groupName, null, null, false, null);
        assertTrue(memberCommandLine.getProcessOutput().anyMatch(out ->
                out.contains(groupName) && out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test(timeout = 10000)
    public void test_start_withPort() throws IOException, ClassNotFoundException, InterruptedException {
        String port = "9898";
        memberCommandLine.start(null, null, port, null, false, null);
        assertTrue(memberCommandLine.getProcessOutput().anyMatch(out ->
                out.contains(port + " is " + LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test
    public void test_stop() throws IOException, ClassNotFoundException, InterruptedException {
        memberCommandLine.start(null, null, null, null, false, null);
        String processUniqueID = captureOut().replace("\n", "");
        int pid = memberCommandLine.getProcessMap().get(processUniqueID).getPid();
        memberCommandLine.stop(processUniqueID);
        assertTrue(!getRunningProcesses().contains(String.valueOf(pid)));
    }

    @Test
    public void test_list() throws ClassNotFoundException, IOException, InterruptedException {
        memberCommandLine.start(null, null, null, null, false, null);
        String processUniqueId1 = captureOut().replace("\n", "");
        resetOut();
        memberCommandLine.start(null, null, null, null, false, null);
        String processUniqueId2 = captureOut().replace("\n", "");
        resetOut();
        memberCommandLine.list();
        String out = captureOut();
        assertTrue(out.contains(processUniqueId1));
        assertTrue(out.contains(processUniqueId2));
    }

    @Test
    public void test_logs() throws IOException, ClassNotFoundException, InterruptedException {
        String groupName = "member-command-line-test";
        startMemberWithConfigFile();
        String processUniqueId = captureOut().replace("\n", "");
        resetOut();
        //await for the logs to be created
        TimeUnit.SECONDS.sleep(5);
        assertTrue(Files.exists(Paths.get(HazelcastCommandLine.HAZELCAST_HOME
                + "/" + processUniqueId + "/logs/hazelcast.log")));
        memberCommandLine.logs(processUniqueId);
        assertTrue(captureOut().contains(groupName));
    }

    private void startMemberWithConfigFile() throws IOException, ClassNotFoundException, InterruptedException {
        memberCommandLine.start("src/test/resources/test-hazelcast.xml", null, null, null, false, null);
    }

    private String getRunningProcesses() throws IOException, InterruptedException {
        return runCommand("jps");
    }

    private String runCommand(String command) throws IOException, InterruptedException {
        Process exec = Runtime.getRuntime().exec(command);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(exec.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String s;
        while ((s = stdInput.readLine()) != null) {
            stringBuilder.append(s).append("\n");
        }

        return stringBuilder.toString();
    }
}
