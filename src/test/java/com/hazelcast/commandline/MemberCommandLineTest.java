package com.hazelcast.commandline;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.LifecycleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class MemberCommandLineTest extends CommandLineTestSupport {

    private MemberCommandLine memberCommandLine;

    @Before
    public void setup(){
        resetOut();
        memberCommandLine = new MemberCommandLine(out, err);
    }

    @After
    public void close() throws IOException, InterruptedException {
        killAllRunningInstances();
    }

    private void killAllRunningInstances() throws IOException, InterruptedException {
        String out = getRunningProcesses();
        List<String> pids = getPids(out);
        runCommand("/bin/kill -9 " + String.join(" ", pids));

    }

    private List<String> getPids(String out) {
        List<String> pids = new ArrayList<>();
        for (String line : out.split("\n")) {
            if (line.contains(HazelcastMember.class.getName())){
                pids.add(line.split(" ")[3]);
            }
        }
        return pids;
    }

    @Test
    public void test_start() throws IOException, ClassNotFoundException {
        memberCommandLine.start(null);
        assertTrue(memberCommandLine.getProcessOutput().anyMatch(out ->
                out.contains(LifecycleEvent.LifecycleState.STARTED.toString())));
    }

    @Test
    public void test_stop() throws IOException, ClassNotFoundException, InterruptedException {
        Integer pid = buildJavaProcess(HazelcastMember.class, new ArrayList<>());
        memberCommandLine.stop(pid);
        assertTrue(!getRunningProcesses().contains(String.valueOf(pid)));
    }

    @Test
    public void test_list() throws ClassNotFoundException, IOException, InterruptedException {
        memberCommandLine.start(null);
        memberCommandLine.start(null);
        memberCommandLine.list();
        for (String pid : getPids(getRunningProcesses())) {
            assertTrue(captureOut().contains(String.valueOf(pid)));
        }
    }

    private String getRunningProcesses() throws IOException, InterruptedException {
        return runCommand("/bin/ps -ef");
    }

    private Integer buildJavaProcess(Class aClass, List<String> parameters) throws IOException{
        List<String> commandList = new ArrayList<>();
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        commandList.add(path);
        commandList.add("-cp");
        commandList.add(classpath);
        commandList.add(aClass.getName());
        commandList.addAll(parameters);
        return getPid(new ProcessBuilder(commandList).start());
    }

    private int getPid(Process process) {
        int pid = 0;
        if(process.getClass().getName().equals("java.lang.UNIXProcess")) {
            /* get the PID on unix/linux systems */
            try {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getInt(process);
            } catch (Throwable e) {
                throw new HazelcastException("Exception when accesing the pid of a process.", e);
            }
        }else {
            /* other plattforms */
            throw new UnsupportedOperationException("Platforms other than UNIX are not supported right now.");
        }

        return pid;
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
