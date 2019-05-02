package com.hazelcast.commandline;

import com.hazelcast.commandline.member.MemberCommandLine;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.util.List;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.DefaultExceptionHandler;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.RunAll;

@Command(name = "hazelcast", description = "Utility for the Hazelcast IMDG operations." + "%n%n"
        + "Global options are:%n", versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class HazelcastCommandLine
        implements Runnable {

    public final static String SEPARATOR = FileSystems.getDefault().getSeparator();
    public static String HAZELCAST_HOME = System.getProperty("user.home") + "/.hazelcast";

    @Option(names = {"-v", "--verbosity"}, description = {"Show logs from Hazelcast and full stack trace of errors"}, order = 1)
    protected boolean isVerbose;

    public static void main(String[] args) {
        runCommandLine(System.out, System.err, true, args);
    }

    private static void runCommandLine(PrintStream out, PrintStream err, boolean shouldExit, String[] args) {
        CommandLine cmd = new CommandLine(new HazelcastCommandLine()).addSubcommand("member", new MemberCommandLine(out, err));

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

    @Override
    public void run() {
    }
}
