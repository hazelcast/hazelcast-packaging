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

import com.hazelcast.commandline.HazelcastVersionProvider;
import com.hazelcast.core.HazelcastException;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.hazelcast.commandline.HazelcastCommandLine.SEPARATOR;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Model.CommandSpec;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.Spec;

/**
 * Command line class responsible for Hazelcast member operations.
 */
@Command(name = "member", description = "Utility for the Hazelcast IMDG member operations.", versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class MemberCommandLine
        implements Runnable {
    private static final String CLASSPATH_SEPARATOR = ":";
    @Spec
    private CommandSpec spec;

    private final PrintStream out;
    private final PrintStream err;
    private ProcessStore processStore;
    //Process input stream is only needed for test purposes, this flag is used to enable it when needed.
    private boolean processInputStreamEnabled;
    private InputStream processInputStream;

    public MemberCommandLine(PrintStream out, PrintStream err, String hazelcastHome, boolean processInputStreamEnabled) {
        this.out = out;
        this.err = err;
        processStore = new ProcessStore(hazelcastHome);
        this.processInputStreamEnabled = processInputStreamEnabled;
    }

    private static int getPid(Process process) {
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
            @Option(names = {"-c", "--config"}, paramLabel = "<file>", description = "Use <file> for Hazelcast configuration.") String configFilePath,
            @Option(names = {"-cn", "--cluster-name"}, paramLabel = "<name>", description = "Use the specified cluster <name> " + "(default: 'dev').", defaultValue = "dev") String clusterName,
            @Option(names = {"-p", "--port"}, paramLabel = "<port>", description = "Bind to the specified <port>. Please note that if the specified port is in use, " + "it will auto-increment to the first free port. (default: 5701)", defaultValue = "5701") String port,
            @Option(names = {"-i", "--interface"}, paramLabel = "<interface>", description = "Bind to the specified <interface>" + " (default: bind to all interfaces).") String hzInterface,
            @Option(names = {"-fg", "--foreground"}, description = "Run in the foreground.") boolean foreground,
            @Option(names = {"-j", "--jar"}, paramLabel = "<path>", description = "Add <path> to Hazelcast class path.") String additionalClassPath,
            @Option(names = {"-J", "--JAVA_OPTS"}, paramLabel = "<option>", split = ",", description = "Specify additional Java" + " <option> (Use ',' char to split multiple options).") List<String> javaOptions)
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
        args.add("-Djava.net.preferIPv4Stack=true");

        HazelcastProcess process = processStore.create();

        args.add("-Djava.util.logging.config.file=" + process.getLoggingPropertiesPath());

        Integer pid = buildJavaProcess(HazelcastMember.class, args, foreground, additionalClassPath);
        process.setPid(pid);
        processStore.save(process);

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
        if (processInputStreamEnabled) {
            processInputStream = process.getInputStream();
        }
        return getPid(process);
    }

    @Command(description = "Stops a Hazelcast IMDG member", mixinStandardHelpOptions = true)
    public void stop(
            @Parameters(index = "0", paramLabel = "<name>", description = "Unique name of the process to stop, for ex.: " + "brave_frog.") String name)
            throws IOException {
        HazelcastProcess process = processStore.find(name);
        if (process == null) {
            printlnErr("No process found with process id: " + name);
            return;
        }
        int pid = process.getPid();
        Runtime.getRuntime().exec("kill -15 " + pid);
        processStore.remove(name);
        println(name + " stopped.");
    }

    @Command(description = "Lists running Hazelcast IMDG members", mixinStandardHelpOptions = true)
    public void list()
            throws IOException {
        Map<String, HazelcastProcess> processes = processStore.findAll();
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
            @Parameters(index = "0", paramLabel = "<name>", description = "Unique name of the process to show the logs, for ex" + ".: brave_frog.") String name,
            @Option(names = {"-n", "--numberOfLines"}, paramLabel = "<lineCount>", description = "Display the specified number " + "of lines (default: 10).", defaultValue = "10") int numberOfLines)
            throws IOException {
        if (!processStore.exists(name)) {
            printlnErr("No process found with process id: " + name);
            return;
        }
        getLogs(out, name, numberOfLines);
    }

    private void getLogs(PrintStream out, String name, int numberOfLines)
            throws IOException {
        String logsPath = processStore.find(name).getLogFilePath();
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

    public InputStream getProcessInputStream() {
        return processInputStream;
    }

    public ProcessStore getProcessStore() {
        return processStore;
    }
}
