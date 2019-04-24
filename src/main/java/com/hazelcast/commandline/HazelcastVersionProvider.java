package com.hazelcast.commandline;

import picocli.CommandLine;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;

public class HazelcastVersionProvider
        implements CommandLine.IVersionProvider {
    public String[] getVersion() {
        return new String[]{getBuildInfo().getVersion()};
    }
}
