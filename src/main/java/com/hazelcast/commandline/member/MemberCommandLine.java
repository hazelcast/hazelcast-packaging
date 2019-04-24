package com.hazelcast.commandline.member;

import com.hazelcast.commandline.HazelcastVersionProvider;
import com.hazelcast.core.HazelcastException;
import picocli.CommandLine;

import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.hazelcast.commandline.HazelcastCommandLine.SEPARATOR;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Spec;
import static picocli.CommandLine.Model;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

@Command(name = "member", description = "Utility for the Hazelcast IMDG member operations.", versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class MemberCommandLine
        implements Runnable {
    protected Model.CommandSpec spec;
    private final PrintStream out;
    private final PrintStream err;
    @Spec
    private Stream<String> processOutput;
    private String CLASSPATH_SEPARATOR = ":";

    public MemberCommandLine(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public void run() {
        List<CommandLine> parsed = spec.commandLine().getParseResult().asCommandLineList();
        if (parsed != null && parsed.size() == 1) {
            spec.commandLine().usage(out);
        }
    }

    @Command(description = "Starts a new Hazelcast IMDG member", mixinStandardHelpOptions = true)
    public void start(
            @Option(names = {"-c", "--config"}, paramLabel = "<file>", description = "Use <file> for Hazelcast configuration") String configFilePath,
            @Option(names = {"-cn", "--cluster-name"}, paramLabel = "<name>", description = "Use the specified cluster <name> (default: 'dev')", defaultValue = "dev") String clusterName,
            @Option(names = {"-p", "--port"}, paramLabel = "<port>", description = "Bind to the specified <port> (default: 5701)", defaultValue = "5701") String port,
            @Option(names = {"-i", "--interface"}, paramLabel = "<interface>", description = "Bind to the specified <interface> (default: bind to all interfaces)") String hzInterface,
            @Option(names = {"-fg", "--foreground"}, description = "Run in the foreground") boolean foreground,
            @Option(names = {"-j", "--jar"}, paramLabel = "<path>", description = "Add <path> to Hazelcast class path") String additionalClassPath,
            @Option(names = {"-J", "--JAVA_OPTS"}, paramLabel = "<option>", split = ",", description = "Specify additional Java <option> (Use ',' char to split multiple options)") List<String> javaOptions)
            throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        if (!isNullOrEmpty(configFilePath)) {
            args.add("-Dhazelcast.config=" + configFilePath);
        }
        args.add("-Dgroup.name=" + clusterName);
        args.add("-Dnetwork.port=" + port);
        if (!isNullOrEmpty(hzInterface)) {
            args.add("-Dbind.any=false");
            args.add("-Dinterfaces.enabled=true");
        } else {
            args.add("-Dbind.any=true");
            args.add("-Dinterfaces.enabled=false");
        }
        args.add("-Dnetwork.interface=" + hzInterface);
        if (javaOptions != null && javaOptions.size() > 0) {
            args.addAll(javaOptions);
        }

        HazelcastProcess process = ProcessUtil.createProcess();

        args.add("-Djava.util.logging.config.file=" + process.getLoggingPropertiesPath());

        Integer pid = buildJavaProcess(HazelcastMember.class, args, foreground, additionalClassPath);
        process.setPid(pid);
        ProcessUtil.saveProcess(process);

        println(process.getName());
    }

    private Integer buildJavaProcess(Class aClass, List<String> parameters, boolean foreground, String additionalClassPath)
            throws IOException, InterruptedException {
        List<String> commandList = new ArrayList<>();
        String classpath = System.getProperty("java.class.path");
        if (!isNullOrEmpty(additionalClassPath)) {
            classpath += CLASSPATH_SEPARATOR + additionalClassPath;
        }
        String path = System.getProperty("java.home") + SEPARATOR + "bin" + SEPARATOR + "java";
        commandList.add(path);
        commandList.add("-cp");
        commandList.add(classpath);
        commandList.addAll(parameters);
        commandList.add(aClass.getName());
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.redirectErrorStream(true);
        if (foreground) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        Process process = processBuilder.start();
        if (foreground) {
            process.waitFor();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        processOutput = bufferedReader.lines();
        return getPid(process);
    }

    private int getPid(Process process) {
        int pid;
        String className = process.getClass().getName();
        if (className.equals("java.lang.UNIXProcess") || className
                .equals("java.lang.ProcessImpl") /* to get the PID on Java9+ */) {
            try {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getInt(process);
            } catch (Throwable e) {
                throw new HazelcastException("Exception when accessing the pid of a process.", e);
            }
        } else {
            throw new UnsupportedOperationException("Platforms other than UNIX are not supported right now.");
        }

        return pid;
    }

    @Command(description = "Stops a Hazelcast IMDG member", mixinStandardHelpOptions = true)
    public void stop(
            @Parameters(index = "0", paramLabel = "<name>", description = "Unique name of the process to stop, for ex.: brave_frog") String name)
            throws IOException {
        HazelcastProcess process = ProcessUtil.getProcess(name);
        if (process == null) {
            printlnErr("No process found with process id: " + name);
            return;
        }
        int pid = process.getPid();
        Runtime.getRuntime().exec("kill -15 " + pid);
        ProcessUtil.removeProcess(name);
        println(name + " stopped.");
    }

    @Command(description = "Lists running Hazelcast IMDG members", mixinStandardHelpOptions = true)
    public void list() {
        Map<String, HazelcastProcess> processes = ProcessUtil.getProcesses();
        if (processes.isEmpty()) {
            println("No running process exists.");
            return;
        }
        for (HazelcastProcess process : processes.values()) {
            println(process.getName());
        }
    }

    @Command(description = "Display the logs for Hazelcast member with the given ID.", mixinStandardHelpOptions = true)
    public void logs(
            @Parameters(index = "0", paramLabel = "<name>", description = "Unique name of the process to show the logs, for ex.: brave_frog") String name,
            @Option(names = {"-n", "--numberOfLines"}, paramLabel = "<lineCount>", description = "Display the specified number of lines (default: 10)", defaultValue = "10") int numberOfLines)
            throws IOException {
        if (!ProcessUtil.processExists(name)) {
            printlnErr("No process found with process id: " + name);
        }
        getLogs(out, name, numberOfLines);
    }

    private void getLogs(PrintStream out, String name, int numberOfLines)
            throws IOException {
        String logsPath = ProcessUtil.getProcess(name).getLogFilePath();
        long totalLineCount = Files.lines(Paths.get(logsPath)).count();
        long skipLineCount = 0;
        if (totalLineCount > numberOfLines) {
            skipLineCount = totalLineCount - numberOfLines;
        }
        Stream<String> stream = Files.lines(Paths.get(logsPath)).skip(skipLineCount);
        stream.forEach(out::println);
    }

    private void printf(String format, Object... objects) {
        out.printf(format, objects);
    }

    private void println(String msg) {
        out.println(msg);
    }

    private void printlnErr(String msg) {
        err.println(msg);
    }

    public Stream<String> getProcessOutput() {
        return processOutput;
    }
}
