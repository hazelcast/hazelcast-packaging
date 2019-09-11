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
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.hazelcast.commandline.HazelcastCommandLine.SEPARATOR;
import static com.hazelcast.commandline.member.HazelcastProcess.Status.RUNNING;
import static com.hazelcast.commandline.member.HazelcastProcess.Status.STOPPED;
import static java.lang.String.format;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Model.CommandSpec;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.Spec;

/**
 * Command line class responsible for Hazelcast member operations.
 */
@Command(name = "member", description = "Utility for the Hazelcast IMDG member operations.",
        versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class MemberCommandLine
        implements Runnable {
    private static final String CLASSPATH_SEPARATOR = ":";
    private static final String LIST_FORMAT = "%-24s %-6s %-7s %-25s %-24s%n";
    private final PrintStream out;
    private final PrintStream err;
    @Spec
    private CommandSpec spec;
    private HazelcastProcessStore hazelcastProcessStore;
    private ProcessExecutor processExecutor;
    //Process input stream is only needed for test purposes, this flag is used to enable it when needed.
    private boolean processInputStreamEnabled;
    private InputStream processInputStream;

    public MemberCommandLine(PrintStream out, PrintStream err, HazelcastProcessStore hazelcastProcessStore,
                             ProcessExecutor processExecutor) {
        this(out, err, hazelcastProcessStore, processExecutor, false);
    }

    protected MemberCommandLine(PrintStream out, PrintStream err, HazelcastProcessStore hazelcastProcessStore,
                                ProcessExecutor processExecutor, boolean processInputStreamEnabled) {
        this.out = out;
        this.err = err;
        this.hazelcastProcessStore = hazelcastProcessStore;
        this.processExecutor = processExecutor;
        this.processInputStreamEnabled = processInputStreamEnabled;
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
            @Option(names = {"-c", "--config"}, paramLabel = "<file>", description = "Use <file> for Hazelcast configuration.")
                    String configFilePath,
            @Option(names = {"-cn", "--cluster-name"}, paramLabel = "<name>",
                    description = "Use the specified cluster <name> (default: 'dev').", defaultValue = "dev")
                    String clusterName,
            @Option(names = {"-p", "--port"}, paramLabel = "<port>",
                    description = "Bind to the specified <port>. Please note that if the specified port is in use, "
                            + "it will auto-increment to the first free port. (default: 5701)", defaultValue = "5701")
                    String port,
            @Option(names = {"-i", "--interface"}, paramLabel = "<interface>",
                    description = "Bind to the specified <interface> (default: bind to all interfaces).")
                    String hzInterface,
            @Option(names = {"-fg", "--foreground"}, description = "Run in the foreground.")
                    boolean foreground,
            @Option(names = {"-j", "--jar"}, paramLabel = "<path>", split = ",", description = "Add <path> to Hazelcast "
                    + "classpath (Use ',' to separate multiple paths).")
                    String[] additionalClassPath,
            @Option(names = {"-J", "--JAVA_OPTS"}, paramLabel = "<option>", split = ",",
                    description = "Specify additional Java <option> (Use ',' to separate multiple options).")
                    List<String> javaOptions)
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

        HazelcastProcess process = hazelcastProcessStore.create();

        args.add("-Djava.util.logging.config.file=" + process.getLoggingPropertiesPath());

        Integer pid = buildJavaProcess(HazelcastMember.class, args, foreground, additionalClassPath);
        process.setPid(pid);
        process.setClusterName(clusterName);
        hazelcastProcessStore.save(process);

        println(process.getName());
    }

    private Integer buildJavaProcess(Class aClass, List<String> parameters, boolean foreground, String[] additionalClassPath)
            throws IOException, InterruptedException {
        List<String> commandList = new ArrayList<>();
        StringBuilder classpath = new StringBuilder(System.getProperty("java.class.path"));
        if (additionalClassPath != null) {
            for (String path : additionalClassPath) {
                classpath.append(CLASSPATH_SEPARATOR).append(path);
            }
        }
        String path = System.getProperty("java.home") + SEPARATOR + "bin" + SEPARATOR + "java";
        commandList.add(path);
        commandList.add("-cp");
        commandList.add(classpath.toString());
        commandList.addAll(parameters);
        commandList.add(aClass.getName());
        Process process = processExecutor.buildAndStart(commandList, foreground);
        if (processInputStreamEnabled) {
            processInputStream = process.getInputStream();
        }
        return processExecutor.extractPid(process);
    }

    @Command(description = "Stops a Hazelcast IMDG member", mixinStandardHelpOptions = true)
    public void stop(
            @Parameters(index = "0", paramLabel = "<name>",
                    description = "Unique name of the process to stop, e.g., elegant_euclid.") String name)
            throws IOException, InterruptedException {
        HazelcastProcess process = hazelcastProcessStore.find(name);
        if (process == null) {
            printlnErr(format("No process found with process id: %s", name));
            return;
        }
        refreshStatus(process);
        if (process.getStatus() == RUNNING) {
            int pid = process.getPid();
            processExecutor.exec(Arrays.asList("kill", "-15", String.valueOf(pid)));
            println(format("%s stopped.", name));
        } else {
            printlnErr(format("%s is not running.", name));
        }
    }

    @Command(description = "Removes information for a stopped Hazelcast IMDG member", mixinStandardHelpOptions = true)
    public void remove(
            @Parameters(index = "0", paramLabel = "<name>",
                    description = "Unique name of the process to remove, e.g., elegant_euclid.") String name)
            throws IOException, InterruptedException {
        HazelcastProcess process = hazelcastProcessStore.find(name);
        if (process == null) {
            printlnErr(format("No process found with process id: %s", name));
            return;
        }
        refreshStatus(process);
        if (process.getStatus() == STOPPED) {
            hazelcastProcessStore.remove(name);
            println(format("%s removed.", name));
        } else {
            printlnErr(format("%s is not stopped. Cannot remove.", name));
        }
    }

    @Command(description = "Lists running Hazelcast IMDG members", mixinStandardHelpOptions = true)
    public void list(
            @Parameters(defaultValue = "", index = "0", paramLabel = "<name>", description = "Unique name of the process to "
                    + "show the status of, e.g., elegant_euclid.") String name,
            @Option(names = {"-n", "--names"}, description = "Shows names only") boolean namesOnly,
            @Option(names = {"-r", "--running"}, description = "Shows running members only") boolean runningOnly)
            throws IOException, InterruptedException {
        Map<String, HazelcastProcess> processes = hazelcastProcessStore.findAll();
        if (!isNullOrEmpty(name)) {
            if (!hazelcastProcessStore.exists(name)) {
                printlnErr(format("No process found with process id: %s", name));
                return;
            }
        }
        if (!namesOnly) {
            printProcessHeader(processes);
        }
        for (HazelcastProcess process : processes.values()) {
            String processName = process.getName();
            if (isNullOrEmpty(name) || name.equals(processName)) {
                refreshStatus(process);
                printProcessEntry(namesOnly, runningOnly, process);
            }
        }
    }

    @Command(description = "Display the logs for Hazelcast IMDG member with the given ID.", mixinStandardHelpOptions = true)
    public void logs(
            @Parameters(index = "0", paramLabel = "<name>",
                    description = "Unique name of the process to show the logs, e.g., elegant_euclid.") String name,
            @Option(names = {"-n", "--numberOfLines"}, paramLabel = "<lineCount>",
                    description = "Display the specified number " + "of lines (default: 10).",
                    defaultValue = "10") int numberOfLines)
            throws IOException {
        if (!hazelcastProcessStore.exists(name)) {
            printlnErr(format("No process found with process id: %s", name));
            return;
        }
        getLogs(out, name, numberOfLines);
    }

    private void refreshStatus(HazelcastProcess process)
            throws IOException, InterruptedException {
        if (processExecutor.isRunning(process.getPid())) {
            process.setStatus(RUNNING);
        } else {
            process.setStatus(STOPPED);
        }
    }

    private void printProcessHeader(Map<String, HazelcastProcess> processes) {
        if (processes.isEmpty()) {
            println("No running process exists.");
        } else {
            printf(LIST_FORMAT, "ID", "PID", "STATUS", "CREATED", "CLUSTER NAME");
        }
    }

    private void printProcessEntry(boolean namesOnly, boolean runningOnly, HazelcastProcess process) {
        int pid = process.getPid();
        String processName = process.getName();
        if (namesOnly) {
            if (!runningOnly || process.getStatus() == RUNNING) {
                println(processName);
            }
        } else if (runningOnly && process.getStatus() == RUNNING) {
            printf(LIST_FORMAT,
                    processName, pid, process.getStatus(), process.getCreationInstant(), process.getClusterName());
        } else if (!runningOnly) {
            printf(LIST_FORMAT,
                    processName, pid, process.getStatus(), process.getCreationInstant(), process.getClusterName());
        }
    }

    private void getLogs(PrintStream out, String name, int numberOfLines)
            throws IOException {
        String logsPath = hazelcastProcessStore.find(name).getLogFilePath();
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

    public HazelcastProcessStore getHazelcastProcessStore() {
        return hazelcastProcessStore;
    }
}
