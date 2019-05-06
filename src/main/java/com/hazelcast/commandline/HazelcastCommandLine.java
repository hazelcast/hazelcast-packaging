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

package com.hazelcast.commandline;

import com.hazelcast.commandline.member.MemberCommandLine;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.util.List;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.DefaultExceptionHandler;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.RunAll;

/**
 * Main command class for Hazelcast IMDG operations
 */
@Command(name = "hazelcast", description = "Utility for the Hazelcast IMDG operations." + "%n%n"
        + "Global options are:%n", versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class HazelcastCommandLine
        implements Runnable {

    /**
     * File system separator of the runtime environment
     */
    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    @Option(names = {"-v", "--verbosity"}, description = {"Show logs from Hazelcast and full stack trace of errors"}, order = 1)
    protected boolean isVerbose;

    public static void main(String[] args) {
        runCommandLine(System.out, System.err, true, args);
    }

    private static void runCommandLine(PrintStream out, PrintStream err, boolean shouldExit, String[] args) {
        String hazelcastHome = System.getProperty("user.home") + "/.hazelcast";
        CommandLine cmd = new CommandLine(new HazelcastCommandLine())
                .addSubcommand("member", new MemberCommandLine(out, err, hazelcastHome, false));

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
