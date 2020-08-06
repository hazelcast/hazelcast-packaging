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

import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigStream;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.FileSystemYamlConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.internal.config.MemberXmlConfigRootTagRecognizer;
import com.hazelcast.internal.config.MemberYamlConfigRootTagRecognizer;

import java.io.FileInputStream;

import static com.hazelcast.internal.util.StringUtil.isNullOrEmpty;

/**
 * Class for starting new Hazelcast members
 */
public final class HazelcastMember {
    private HazelcastMember() {
    }

    public static void main(String[] args)
            throws Exception {
        Hazelcast.newHazelcastInstance(config());
    }

    protected static Config config()
            throws Exception {
        String hazelcastConfig = System.getProperty("hazelcast.config");
        if (!isNullOrEmpty(hazelcastConfig)) {
            return createConfig(hazelcastConfig);
        }
        String defaultHazelcastConfig = System.getProperty("hazelcast.default.config");
        Config config = createConfig(defaultHazelcastConfig);
        config.getNetworkConfig().setPort(Integer.parseInt(System.getProperty("network.port")));
        String networkInterface = System.getProperty("network.interface");
        config.setProperty("hazelcast.socket.bind.any", "false");
        InterfacesConfig interfaces = config.getNetworkConfig().getInterfaces();
        interfaces.setEnabled(true).addInterface(networkInterface);
        return config;
    }

    private static Config createConfig(String configPath)
            throws Exception {
        if (new MemberYamlConfigRootTagRecognizer().isRecognized(new ConfigStream(new FileInputStream(configPath)))) {
            return new FileSystemYamlConfig(configPath);
        } else if (new MemberXmlConfigRootTagRecognizer().isRecognized(new ConfigStream(new FileInputStream(configPath)))) {
            return new FileSystemXmlConfig(configPath);
        } else {
            throw new InvalidConfigurationException("Provided configuration file is invalid.");
        }
    }
}
