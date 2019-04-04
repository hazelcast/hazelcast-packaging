package com.hazelcast.commandline;

import com.hazelcast.core.HazelcastException;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static picocli.CommandLine.*;

@Command(
        name = "member",
        description = "Utility for the Hazelcast IMDG member operations.",
        versionProvider = HazelcastVersionProvider.class,
        mixinStandardHelpOptions = true,
        sortOptions = false
)
public class MemberCommandLine implements Callable<Void> {
    private final PrintStream out;
    private final PrintStream err;
    private final String FILE_PATH = "instances.dat";
    private Stream<String> processOutput;

    public MemberCommandLine(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    public Void call() {
        return null;
    }

    @Command(description = "Starts a new Hazelcast IMDG member",
            mixinStandardHelpOptions = true
    )
    public void start(
            @Option(names = {"-c", "--config"},
            description = "Use <file> for Hazelcast configuration.")
            String configFilePath) throws IOException, ClassNotFoundException {
        println("Starting a new Hazelcast IMDG member...");
        List<String> parameters = new ArrayList<>();
        if (configFilePath != null && !configFilePath.isEmpty()) {
            parameters.add("-Dhazelcast.config=" + configFilePath);
        }
        Integer pid = buildJavaProcess(HazelcastMember.class, parameters);
        saveProcess(new HazelcastProcess(pid));
        println("Hazelcast IMDG Member started with pid: " + pid);
    }

    @Command(description = "Stops a Hazelcast IMDG member",
            mixinStandardHelpOptions = true
    )
    public void stop(
            @Parameters(index = "0",
            paramLabel = "<pid>",
            description = "Process id of the process to stop")
                                 Integer pid) throws IOException, ClassNotFoundException {
        println("Stopping Hazelcast IMDG member with pid: " + pid);

        Runtime.getRuntime().exec("kill -15 " + pid);
        removeProcess(pid);
    }

    @Command(description = "Lists running Hazelcast IMDG members",
            mixinStandardHelpOptions = true
    )
    public void list() throws ClassNotFoundException {
        Map<Integer, HazelcastProcess> processMap = getProcessMap();
        if (processMap.isEmpty()){
            println("No running process exists.");
            return;
        }
        for (HazelcastProcess process : processMap.values()) {
            println(process.getPid() + "");
        }
    }

    private void removeProcess(Integer pid) throws IOException, ClassNotFoundException {
        Map<Integer, HazelcastProcess> processMap = getProcessMap();
        if (processMap == null || !processMap.containsKey(pid)){
            println("No process found with pid: " + pid);
            return;
        }
        processMap.remove(pid);
        updateFile(processMap);
    }

    private Map<Integer, HazelcastProcess> getProcessMap() throws ClassNotFoundException {
        FileInputStream fileInputStream;
        Map<Integer, HazelcastProcess> processes = new HashMap<>();
        try {
            Path path = FileSystems.getDefault().getPath(FILE_PATH);
            if (!Files.exists(path)){
                Files.createFile(path);
            }
            fileInputStream = new FileInputStream(FILE_PATH);
            if (Files.size(path) == 0){
                return processes;
            }
            ObjectInputStream input = new ObjectInputStream(fileInputStream);
            processes = (Map<Integer, HazelcastProcess>) input.readObject();
            input.close();
        } catch (IOException e) {
            throw new HazelcastException("Error when reading from file.", e);
        }
        return processes;
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
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        processOutput = bufferedReader.lines();
        return getPid(process);
    }

    private void saveProcess(HazelcastProcess process) throws IOException, ClassNotFoundException {
        Map<Integer, HazelcastProcess> processMap = getProcessMap();
        processMap.put(process.getPid(), process);
        updateFile(processMap);
    }

    private void updateFile(Map<Integer, HazelcastProcess> processMap) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(FILE_PATH);
        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
        objectOut.writeObject(processMap);
        objectOut.close();
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

    private void printf(String format, Object... objects) {
        out.printf(format, objects);
    }

    private void println(String msg) {
        out.println(msg);
    }

    public Stream<String> getProcessOutput() {
        return processOutput;
    }
}
