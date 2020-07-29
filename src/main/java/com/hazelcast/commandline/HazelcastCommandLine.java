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

import com.hazelcast.commandline.managementcenter.ManagementCenterCommandLine;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;
import static com.hazelcast.internal.util.StringUtil.isNullOrEmpty;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * Main command class for Hazelcast operations
 */
@Command(name = "hz", description = "Utility for the Hazelcast operations." + "%n%n"
        + "Global options are:%n", versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class HazelcastCommandLine
        extends AbstractCommandLine {

    /**
     * File system separator of the runtime environment
     */
    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    public HazelcastCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor) {
        super(out, err, processExecutor, false);
    }

    public HazelcastCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor,
                                boolean processInputStreamEnabled) {
        super(out, err, processExecutor, processInputStreamEnabled);
    }

    public static void main(String[] args)
            throws IOException {
        runCommandLine(System.out, System.err, args);
    }

    private static void runCommandLine(PrintStream out, PrintStream err, String[] args)
            throws IOException {
        ProcessExecutor processExecutor = new ProcessExecutor();
        CommandLine cmd = new CommandLine(new HazelcastCommandLine(out, err, processExecutor))
                .addSubcommand("mc", new ManagementCenterCommandLine(out, err, processExecutor))
                .setOut(new PrintWriter(out))
                .setErr(new PrintWriter(err)).setTrimQuotes(true);
        cmd.execute(args);

        String version = getBuildInfo().getVersion();
        cmd.getCommandSpec().usageMessage().header("Hazelcast IMDG " + version);
        if (args.length == 0) {
            cmd.usage(out);
        }
    }

    @Override
    public void run() {
    }

    @Command(description = "Starts a new Hazelcast IMDG member", mixinStandardHelpOptions = true, sortOptions = false)
    public void start(
            @Option(names = {"-c", "--config"}, paramLabel = "<file>", description = "Use <file> for Hazelcast configuration. "
                    + "Accepted formats are XML and YAML. ")
                    String configFilePath,
            @Option(names = {"-p", "--port"}, paramLabel = "<port>",
                    description = "Bind to the specified <port>. Please note that if the specified port is in use, "
                            + "it will auto-increment to the first free port. (default: 5701)", defaultValue = "5701")
                    String port,
            @Option(names = {"-i", "--interface"}, paramLabel = "<interface>",
                    description = "Bind to the specified <interface> (default: bind to all interfaces).")
                    String hzInterface,
            @Option(names = {"-j", "--jar"}, paramLabel = "<path>", split = ",", description = "Add <path> to Hazelcast "
                    + "classpath (Use ',' to separate multiple paths). You can add jars, classes, or the directories that contain classes/jars.")
                    String[] additionalClassPath,
            @Option(names = {"-J", "--JAVA_OPTS"}, paramLabel = "<option>", split = ",", parameterConsumer = JavaOptionsConsumer.class,
                    description = "Specify additional Java <option> (Use ',' to separate multiple options).")
                    List<String> javaOptions,
            @Option(names = {"-v", "--verbose"},
                    description = "Output with FINE level verbose logging.")
                    boolean verbose,
            @Option(names = {"-vv", "--vverbose"},
                    description = "Output with FINEST level verbose logging.")
                    boolean finestVerbose)
            throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        if (isNullOrEmpty(configFilePath)) {
            configFilePath = AbstractCommandLine.WORKING_DIRECTORY + "/config/hazelcast.yaml";
        }
        args.add("-Dhazelcast.config=" + configFilePath);
        args.add("-Dnetwork.port=" + port);
        args.add("-Dnetwork.interface=" + hzInterface);
        if (javaOptions != null && javaOptions.size() > 0) {
            args.addAll(javaOptions);
        }
        args.add("-Djava.net.preferIPv4Stack=true");

        addLogging(args, verbose, finestVerbose);

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
