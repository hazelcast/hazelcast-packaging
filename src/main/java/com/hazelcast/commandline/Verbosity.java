package com.hazelcast.commandline;

import picocli.CommandLine.Option;

public class Verbosity {

    @Option(names = {"-v", "--verbosity"}, description = {"Show logs from Hazelcast and full stack trace of errors"}, order = 1)
    private boolean isVerbose;

    public boolean isVerbose() {
        return isVerbose;
    }

    public void merge(Verbosity other) {
        isVerbose |= other.isVerbose;
    }
}