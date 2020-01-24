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

import picocli.CommandLine;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.ParseResult;

/**
 * Handler class for exceptions during the command run
 *
 * @param <R> the type of the parsed command
 */
public class ExceptionHandler<R>
        extends CommandLine.DefaultExceptionHandler<R> {
    @Override
    public R handleExecutionException(ExecutionException ex, ParseResult parseResult) {
        CommandLine cmdLine = ex.getCommandLine();
        while (cmdLine.getParent() != null) {
            cmdLine = cmdLine.getParent();
        }
        HazelcastCommandLine hzCmd = cmdLine.getCommand();
        if (hzCmd.isVerbose) {
            ex.printStackTrace(err());
        } else {
            err().println("ERROR: " + ex.getCause().getMessage());
            err().println();
            err().println("To see the full stack trace, please re-run with the -v/--verbosity option at the top level command");
        }
        if (hasExitCode()) {
            exit(exitCode());
        }
        throw ex;
    }
}
