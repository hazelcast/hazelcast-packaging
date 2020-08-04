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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Abstract command line class.
 */
public abstract class AbstractCommandLine implements Runnable {
    protected static final String WORKING_DIRECTORY = System.getProperty("hazelcast.commandline.workingdirectory", "distro/src");
    static final String HZ_FINE_LEVEL_LOGGING_PROPERTIES_FILE_LOCATION = "/config/hazelcast-fine-level-logging.properties";
    static final String HZ_FINEST_LEVEL_LOGGING_PROPERTIES_FILE_LOCATION = "/config/hazelcast-finest-level-logging.properties";
    static final String CLASSPATH_SEPARATOR = ":";

    protected final PrintStream out;
    protected final PrintStream err;
    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;
    protected ProcessExecutor processExecutor;
    //Process input stream is only needed for test purposes, this flag is used to enable it when needed.
    protected boolean processInputStreamEnabled;
    protected InputStream processInputStream;

    public AbstractCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor,
                               boolean processInputStreamEnabled) {
        this.out = out;
        this.err = err;
        this.processExecutor = processExecutor;
        this.processInputStreamEnabled = processInputStreamEnabled;
    }

    protected void printf(String format, Object... objects) {
        out.printf(format, objects);
    }

    protected void println(String msg) {
        out.println(msg);
    }

    protected void printlnErr(String msg) {
        err.println(msg);
    }

    public InputStream getProcessInputStream() {
        return processInputStream;
    }

    protected void addLogging(List<String> args, boolean verbose, boolean finestVerbose) {
        if (verbose) {
            args.add("-Djava.util.logging.config.file=" + WORKING_DIRECTORY + HZ_FINE_LEVEL_LOGGING_PROPERTIES_FILE_LOCATION);
        }
        if (finestVerbose) {
            args.add("-Djava.util.logging.config.file=" + WORKING_DIRECTORY + HZ_FINEST_LEVEL_LOGGING_PROPERTIES_FILE_LOCATION);
        }
    }

    /**
     * {@code picocli.CommandLine.IParameterConsumer} implementation to handle Java options.
     * Please see the details <a href=https://github.com/remkop/picocli/issues/1125>here</a>.
     */
    public static class JavaOptionsConsumer implements CommandLine.IParameterConsumer {
        public void consumeParameters(Stack<String> args, CommandLine.Model.ArgSpec argSpec,
                                      CommandLine.Model.CommandSpec commandSpec) {
            if (args.isEmpty()) {
                throw new CommandLine.ParameterException(commandSpec.commandLine(),
                        "Error: option '-J', '--JAVA_OPTS' requires a parameter");
            }
            List<String> list = argSpec.getValue();
            if (list == null) {
                list = new ArrayList<>();
                argSpec.setValue(list);
            }
            String arg = args.pop();
            String[] splitArgs = arg.split(argSpec.splitRegex());
            Collections.addAll(list, splitArgs);
        }
    }
}
