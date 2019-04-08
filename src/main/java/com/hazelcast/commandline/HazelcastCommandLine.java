package com.hazelcast.commandline;

import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Callable;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;
import static picocli.CommandLine.*;

@Command(
        name = "hazelcast",
        description = "Utility for the Hazelcast IMDG operations." +
                "%n%n" +
                "Global options are:%n",
        versionProvider = HazelcastVersionProvider.class,
        mixinStandardHelpOptions = true,
        sortOptions = false
)
public class HazelcastCommandLine implements Callable<Void> {

    public static String HAZELCAST_HOME = System.getProperty("user.home") + "/.hazelcast";
    public final static String SEPARATOR = System.getProperty("file.separator");

    @Mixin(name = "verbosity")
    private Verbosity verbosity;

    public Void call() {
        return null;
    }

    public static void main(String[] args) {
        runCommandLine(System.out, System.err, true, args);
    }

    static void runCommandLine(
            PrintStream out, PrintStream err,
            boolean shouldExit,
            String[] args
    ) {
        CommandLine cmd = new CommandLine(new HazelcastCommandLine())
                .addSubcommand("member", new MemberCommandLine(out, err));

        String version = getBuildInfo().getVersion();
        cmd.getCommandSpec().usageMessage().header("Hazelcast IMDG " + version);
        if (args.length == 0) {
            cmd.usage(out);
        } else {
            DefaultExceptionHandler<List<Object>> excHandler =
                    new ExceptionHandler<List<Object>>().useErr(err).useAnsi(Help.Ansi.AUTO);
            if (shouldExit) {
                excHandler.andExit(1);
            }
            List<Object> parsed = cmd.parseWithHandlers(new RunAll().useOut(out).useAnsi(Help.Ansi.AUTO), excHandler, args);
            // only top command was executed
            if (parsed != null && parsed.size() == 1) {
                cmd.usage(out);
            }
        }
    }

    public static class Verbosity {

        @Option(names = {"-v", "--verbosity"},
                description = {"Show logs from Hazelcast and full stack trace of errors"},
                order = 1
        )
        private boolean isVerbose;

        void merge(Verbosity other) {
            isVerbose |= other.isVerbose;
        }
    }

    static class ExceptionHandler<R> extends DefaultExceptionHandler<R> {
        @Override
        public R handleExecutionException(ExecutionException ex, ParseResult parseResult) {
            // find top level command
            CommandLine cmdLine = ex.getCommandLine();
            while (cmdLine.getParent() != null) {
                cmdLine = cmdLine.getParent();
            }
            HazelcastCommandLine hzCmd = cmdLine.getCommand();
            if (hzCmd.verbosity.isVerbose) {
                ex.printStackTrace(err());
            } else {
                err().println("ERROR: " + ex.getCause().getMessage());
                err().println();
                err().println("To see the full stack trace, re-run with the -v/--verbosity option");
            }
            if (hasExitCode()) {
                exit(exitCode());
            }
            throw ex;
        }
    }
}
