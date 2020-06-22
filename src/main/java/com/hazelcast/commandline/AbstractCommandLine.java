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

/**
 * Abstract command line class.
 */
public abstract class AbstractCommandLine implements Runnable {
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
}
