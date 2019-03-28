package com.hazelcast.commandline;

import picocli.CommandLine;

import static com.hazelcast.instance.BuildInfoProvider.getBuildInfo;

public class HazelcastVersionProvider implements CommandLine.IVersionProvider {
    public String[] getVersion() throws Exception {
        return new String[]{getBuildInfo().getVersion()};
    }
}
