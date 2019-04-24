package com.hazelcast.commandline;

import picocli.CommandLine;

import static picocli.CommandLine.*;

public class ExceptionHandler<R>
        extends CommandLine.DefaultExceptionHandler<R> {
    @Override
    public R handleExecutionException(ExecutionException ex, ParseResult parseResult) {
        // find top level command
        CommandLine cmdLine = ex.getCommandLine();
        while (cmdLine.getParent() != null) {
            cmdLine = cmdLine.getParent();
        }
        HazelcastCommandLine hzCmd = cmdLine.getCommand();
        if (hzCmd.verbosity.isVerbose()) {
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
