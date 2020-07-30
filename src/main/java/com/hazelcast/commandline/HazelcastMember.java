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
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.FileSystemYamlConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.core.Hazelcast;

import java.io.FileNotFoundException;

/**
 * Class for starting new Hazelcast members
 */
public final class HazelcastMember {
    private HazelcastMember() {
    }

    public static void main(String[] args)
            throws FileNotFoundException {
        String configPath = System.getProperty("hazelcast.config");
        Config config;
        if (configPath.endsWith(".yaml") || configPath.endsWith(".yml")) {
            config = new FileSystemYamlConfig(configPath);
        } else {
            config = new FileSystemXmlConfig(configPath);
        }
        config.getNetworkConfig().setPort(Integer.parseInt(System.getProperty("network.port"))).setPortAutoIncrement(true);
        String networkInterface = System.getProperty("network.interface");
        config.setProperty("hazelcast.socket.bind.any", "false");
        InterfacesConfig interfaces = config.getNetworkConfig().getInterfaces();
        interfaces.setEnabled(true).addInterface(networkInterface);
        Hazelcast.newHazelcastInstance(config);
    }
}
