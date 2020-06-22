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

package com.hazelcast.commandline;

import com.hazelcast.core.HazelcastException;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;
import static com.hazelcast.internal.util.StringUtil.isNullOrEmpty;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.DefaultExceptionHandler;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.RunAll;

/**
 * Main command class for Hazelcast IMDG operations
 */
@Command(name = "hz", description = "Utility for the Hazelcast IMDG operations." + "%n%n"
        + "Global options are:%n", versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class HazelcastCommandLine
        extends AbstractCommandLine {

    /**
     * File system separator of the runtime environment
     */
    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    @Option(names = {"-v", "--verbosity"}, description = {"Show logs from Hazelcast and full stack trace of errors"}, order = 1)
    protected boolean isVerbose;

    public HazelcastCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor) {
        super(out, err, processExecutor, false);
    }

    public HazelcastCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor,
                                boolean processInputStreamEnabled) {
        super(out, err, processExecutor, processInputStreamEnabled);
    }

    public static void main(String[] args) {
        runCommandLine(System.out, System.err, true, args);
    }

    private static void runCommandLine(PrintStream out, PrintStream err, boolean shouldExit, String[] args) {
        checkIfEnvSupported();
        ProcessExecutor processExecutor = new ProcessExecutor();
        CommandLine cmd = new CommandLine(new HazelcastCommandLine(out, err, processExecutor));

        String version = getBuildInfo().getVersion();
        cmd.getCommandSpec().usageMessage().header("Hazelcast IMDG " + version);
        if (args.length == 0) {
            cmd.usage(out);
        } else {
            DefaultExceptionHandler<List<Object>> excHandler = new ExceptionHandler<List<Object>>().useErr(err)
                                                                                                   .useAnsi(Help.Ansi.AUTO);
            if (shouldExit) {
                excHandler.andExit(1);
            }
            List<Object> parsed = cmd.parseWithHandlers(new RunAll().useOut(out).useAnsi(Help.Ansi.AUTO), excHandler, args);
            // when only top command was executed, print usage info
            if (parsed != null && parsed.size() == 1) {
                cmd.usage(out);
            }
        }
    }

    private static void checkIfEnvSupported() {
        String osName = System.getProperty("os.name");
        if (!osName.equals("Linux") && !osName.contains("OS X") && !osName.equals("SunOS") && !osName.equals("AIX")) {
            throw new HazelcastException("Platforms other than Unix-like are not supported right now.");
        }
    }

    @Override
    public void run() {
    }

    @Command(description = "Starts a new Hazelcast IMDG member", mixinStandardHelpOptions = true)
    public void start(
            @Option(names = {"-c", "--config"}, paramLabel = "<file>", description = "Use <file> for Hazelcast configuration.")
                    String configFilePath,
            @Option(names = {"-p", "--port"}, paramLabel = "<port>",
                    description = "Bind to the specified <port>. Please note that if the specified port is in use, "
                            + "it will auto-increment to the first free port. (default: 5701)", defaultValue = "5701")
                    String port,
            @Option(names = {"-i", "--interface"}, paramLabel = "<interface>",
                    description = "Bind to the specified <interface> (default: bind to all interfaces).")
                    String hzInterface,
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

//        args.add("-Djava.util.logging.config.file=" + process.getLoggingPropertiesPath());

        buildAndStartJavaProcess(HazelcastMember.class, args, additionalClassPath);
    }

    private void buildAndStartJavaProcess(Class aClass, List<String> parameters, String[] additionalClassPath)
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
        processExecutor.buildAndStart(commandList);
    }
}
