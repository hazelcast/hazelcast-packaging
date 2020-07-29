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

import java.io.IOException;
import java.util.Properties;

/**
 * Implementation of {@link picocli.CommandLine.IVersionProvider} for providing version information.
 */
public class HazelcastVersionProvider
        implements CommandLine.IVersionProvider {

    private final String toolVersion;
    private final String hzVersion;
    private final String mcVersion;

    public HazelcastVersionProvider()
            throws IOException {
        Properties commandlineProperties = new Properties();
        commandlineProperties.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
        toolVersion = commandlineProperties.getProperty("tool.version");
        hzVersion = commandlineProperties.getProperty("hz.version");
        mcVersion = commandlineProperties.getProperty("mc.version");
    }

    public String[] getVersion() {
        return new String[]{"CLI tool: " + toolVersion,
                            "Hazelcast IMDG included: " + hzVersion,
                            "Hazelcast Management Center included: " + mcVersion};
    }

    public String getMcVersion() {
        return mcVersion;
    }
}
