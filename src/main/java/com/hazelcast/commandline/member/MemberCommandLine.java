package com.hazelcast.commandline.member;

import com.hazelcast.commandline.HazelcastVersionProvider;
import com.hazelcast.commandline.member.names.MobyNames;
import com.hazelcast.core.HazelcastException;
import picocli.CommandLine;

import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static com.hazelcast.commandline.HazelcastCommandLine.*;
import static picocli.CommandLine.*;

@Command(
        name = "member",
        description = "Utility for the Hazelcast IMDG member operations.",
        versionProvider = HazelcastVersionProvider.class,
        mixinStandardHelpOptions = true,
        sortOptions = false
)
public class MemberCommandLine implements Runnable {
    @Spec
    Model.CommandSpec spec;

    private final PrintStream out;
    private final PrintStream err;
    private final String logsDirString = "logs";
    private final String logsFileNameString = "hazelcast.log";
    private Stream<String> processOutput;
    public final String instancesFilePath = HAZELCAST_HOME + SEPARATOR + "instances.dat";
    private String CLASSPATH_SEPARATOR = ":";

    public MemberCommandLine(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
        createHomeDirectory();
    }

    private void createHomeDirectory() {
        try {
            new File(HAZELCAST_HOME).mkdirs();
        } catch (Exception e) {
            throw new HazelcastException("Process directories couldn't created. This might be related to user " +
                    "permissions, please check your write permissions at: " + HAZELCAST_HOME, e);
        }
    }

    public void run() {
        List<CommandLine> parsed = spec.commandLine().getParseResult().asCommandLineList();
        if (parsed != null && parsed.size() == 1) {
            spec.commandLine().usage(out);
        }
    }

    @Command(description = "Starts a new Hazelcast IMDG member",
            mixinStandardHelpOptions = true
    )
    public void start(
            @Option(names = {"-c", "--config"},
                    paramLabel = "<file>",
                    description = "Use <file> for Hazelcast configuration")
                    String configFilePath,
            @Option(names = {"-cn", "--cluster-name"},
                    paramLabel = "<name>",
                    description = "Use the specified cluster <name> (default: 'dev')",
                    defaultValue = "dev")
                    String clusterName,
            @Option(names = {"-p", "--port"},
                    paramLabel = "<port>",
                    description = "Bind to the specified <port> (default: 5701)",
                    defaultValue = "5701")
                    String port,
            @Option(names = {"-i", "--interface"},
                    paramLabel = "<interface>",
                    description = "Bind to the specified <interface> (default: bind to all interfaces)")
                    String hzInterface,
            @Option(names = {"-fg", "--foreground"},
                    description = "Run in the foreground")
                    boolean foreground,
            @Option(names = {"-j", "--jar"},
                    paramLabel = "<path>",
                    description = "Add <path> to Hazelcast class path")
                    String additionalClassPath,
            @Option(names = {"-J", "--JAVA_OPTS"},
                    paramLabel = "<option>",
                    split = ",",
                    description = "Specify additional Java <option> (Use ',' char to split multiple options)")
                    List<String> javaOptions) throws IOException, ClassNotFoundException, InterruptedException {
        List<String> args = new ArrayList<>();
        if (!isNullOrEmpty(configFilePath)) {
            args.add("-Dhazelcast.config=" + configFilePath);
        }
        args.add("-Dgroup.name=" + (!isNullOrEmpty(clusterName) ? clusterName : "dev"));
        args.add("-Dnetwork.port=" + (!isNullOrEmpty(port) ? port : "5701"));
        if (!isNullOrEmpty(hzInterface)) {
            args.add("-Dbind.any=false");
            args.add("-Dinterfaces.enabled=true");
        }else {
            args.add("-Dbind.any=true");
            args.add("-Dinterfaces.enabled=false");
        }
        args.add("-Dnetwork.interface=" + hzInterface);
        if (javaOptions != null && javaOptions.size() > 0){
            args.addAll(javaOptions);
        }

        String processUniqueId = MobyNames.getRandomName(0);
        String processDir = createProcessDirs(processUniqueId);
        String loggingPropertiesPath = createLoggingPropertiesFile(processUniqueId, processDir);
        args.add("-Djava.util.logging.config.file=" + loggingPropertiesPath);

        Integer pid = buildJavaProcess(HazelcastMember.class, args, foreground, additionalClassPath);
        saveProcess(new HazelcastProcess(processUniqueId, pid));

        println(processUniqueId);
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    private String createProcessDirs(String processUniqueId) {
        String processPath = HAZELCAST_HOME + SEPARATOR + processUniqueId;
        String logPath = processPath + SEPARATOR + logsDirString;
        try {
            new File(logPath).mkdirs();
        } catch (Exception e) {
            throw new HazelcastException("Process directories couldn't created.");
        }
        return processPath;
    }

    private String createLoggingPropertiesFile(String processUniqueId, String processDir)
            throws FileNotFoundException {
        String loggingPropertiesPath = processDir + SEPARATOR + "logging.properties";
        PrintWriter printWriter = new PrintWriter(loggingPropertiesPath);
        String fileContent = "handlers= java.util.logging.FileHandler, java.util.logging.ConsoleHandler\n" +
                ".level= INFO\n" +
                "java.util.logging.FileHandler.pattern = " +
                processDir + SEPARATOR + logsDirString + SEPARATOR + "hazelcast.log\n" +
                "java.util.logging.FileHandler.limit = 50000\n" +
                "java.util.logging.FileHandler.count = 1\n" +
                "java.util.logging.FileHandler.maxLocks = 100\n" +
                "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n" +
                "java.util.logging.FileHandler.append=true\n" +
                "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter";
        printWriter.println(fileContent);
        printWriter.close();
        return loggingPropertiesPath;
    }

    @Command(description = "Stops a Hazelcast IMDG member",
            mixinStandardHelpOptions = true
    )
    public void stop(
            @Parameters(index = "0",
                        paramLabel = "<process-id>",
                        description = "Process id of the process to stop")
                        String processUniqueId) throws IOException, ClassNotFoundException {
        HazelcastProcess process = getProcessMap().get(processUniqueId);
        if (process == null){
            printlnErr("No process found with process id: " + processUniqueId);
            return;
        }
        int pid = process.getPid();
        Runtime.getRuntime().exec("kill -15 " + pid);
        removeProcess(processUniqueId);
        println(processUniqueId + " stopped.");
    }

    @Command(description = "Lists running Hazelcast IMDG members",
            mixinStandardHelpOptions = true
    )
    public void list() throws ClassNotFoundException {
        Map<String, HazelcastProcess> processMap = getProcessMap();
        if (processMap.isEmpty()){
            println("No running process exists.");
            return;
        }
        for (HazelcastProcess process : processMap.values()) {
            println(process.getProcessUniqueId());
        }
    }

    @Command(description = "Display the logs for Hazelcast member with the given ID.",
            mixinStandardHelpOptions = true
    )
    public void logs(
            @Parameters(index = "0",
                        paramLabel = "<process-id>",
                        description = "Process id of the process to show the logs")
                        String processUniqueId,
            @Option(names = {"-n", "--numberOfLines"},
                    paramLabel = "<lineCount>",
                    description = "Display the specified number of lines",
                    defaultValue = "10")
                    int numberOfLines) throws ClassNotFoundException, IOException {
        if (!getProcessMap().containsKey(processUniqueId)) {
            printlnErr("No process found with process id: " + processUniqueId);
        }
        getLogs(out, processUniqueId, numberOfLines);
    }

    private void getLogs(PrintStream out, String processUniqueId, int numberOfLines) throws IOException {
        String logsPath = HAZELCAST_HOME + SEPARATOR + processUniqueId + SEPARATOR
                + logsDirString + SEPARATOR + logsFileNameString;
        long totalLineCount = Files.lines(Paths.get(logsPath)).count();
        long skipLineCount = 0;
        if (totalLineCount > numberOfLines){
            skipLineCount = totalLineCount - numberOfLines;
        }
        Stream<String> stream = Files.lines( Paths.get(logsPath)).skip(skipLineCount);
        stream.forEach(out::println);
    }

    private void removeProcess(String processUniqueId) throws IOException, ClassNotFoundException {
        Map<String, HazelcastProcess> processMap = getProcessMap();
        if (processMap == null || !processMap.containsKey(processUniqueId)){
            println("No process found with pid: " + processUniqueId);
            return;
        }
        processMap.remove(processUniqueId);
        updateFile(processMap);
    }

    public Map<String, HazelcastProcess> getProcessMap() throws ClassNotFoundException {
        FileInputStream fileInputStream;
        Map<String, HazelcastProcess> processes = new HashMap<>();
        try {
            Path path = FileSystems.getDefault().getPath(instancesFilePath);
            if (!Files.exists(path)){
                Files.createFile(path);
            }
            fileInputStream = new FileInputStream(instancesFilePath);
            if (Files.size(path) == 0){
                return processes;
            }
            ObjectInputStream input = new ObjectInputStream(fileInputStream);
            processes = (Map<String, HazelcastProcess>) input.readObject();
            input.close();
        } catch (IOException e) {
            throw new HazelcastException("Error when reading from file.", e);
        }
        return processes;
    }

    private Integer buildJavaProcess(Class aClass, List<String> parameters, boolean foreground, String additionalClassPath)
            throws IOException, InterruptedException {
        List<String> commandList = new ArrayList<>();
        String classpath = System.getProperty("java.class.path");
        if (!isNullOrEmpty(additionalClassPath)){
            classpath += CLASSPATH_SEPARATOR + additionalClassPath;
        }
        String path = System.getProperty("java.home")
                + SEPARATOR + "bin" + SEPARATOR + "java";
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

    private void saveProcess(HazelcastProcess process) throws IOException, ClassNotFoundException {
        Map<String, HazelcastProcess> processMap = getProcessMap();
        processMap.put(process.getProcessUniqueId(), process);
        updateFile(processMap);
    }

    private void updateFile(Map<String, HazelcastProcess> processMap) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(instancesFilePath);
        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
        objectOut.writeObject(processMap);
        objectOut.close();
    }

    private int getPid(Process process) {
        int pid = 0;
        String className = process.getClass().getName();
        if(className.equals("java.lang.UNIXProcess")
                || className.equals("java.lang.ProcessImpl") /* to get the PID on Java9+ */
        ) {
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

    private void printlnErr(String msg) {
        err.println(msg);
    }

    public Stream<String> getProcessOutput() {
        return processOutput;
    }
}
