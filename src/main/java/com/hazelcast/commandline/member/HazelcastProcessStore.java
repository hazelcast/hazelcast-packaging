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

package com.hazelcast.commandline.member;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.hazelcast.commandline.member.names.MobyNames;
import com.hazelcast.core.HazelcastException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.hazelcast.commandline.HazelcastCommandLine.SEPARATOR;

/**
 * Manages {@link HazelcastProcess} operations by keeping them in local filesystem.
 */
public class HazelcastProcessStore {
    private static final String LOGS_DIR_STRING = "logs";
    private static final String LOGS_FILE_NAME_STRING = "hazelcast.log";
    private final String hazelcastHome;
    private final String instancesFilePath;
    private final Kryo kryo;

    public HazelcastProcessStore(String hazelcastHome) {
        this.hazelcastHome = hazelcastHome;
        this.instancesFilePath = hazelcastHome + SEPARATOR + "instances.dat";
        createHazelcastHomeDirectory();
        kryo = new Kryo();
        kryo.register(HazelcastProcess.class, new CompatibleFieldSerializer(kryo, HazelcastProcess.class));
    }

    private void createHazelcastHomeDirectory() {
        try {
            new File(hazelcastHome).mkdirs();
        } catch (Exception e) {
            throw new HazelcastException("Process directories couldn't created. This might be related to user "
                    + "permissions, please check your write permissions at: " + hazelcastHome, e);
        }
    }

    void save(HazelcastProcess process)
            throws IOException {
        Map<String, HazelcastProcess> processMap = findAll();
        processMap.put(process.getName(), process);
        updateFile(processMap);
    }

    Map<String, HazelcastProcess> findAll()
            throws IOException {
        Map<String, HazelcastProcess> processes = new HashMap<>();
        Path path = FileSystems.getDefault().getPath(instancesFilePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (Files.size(path) == 0) {
            return processes;
        }
        try (FileInputStream fileInputStream = new FileInputStream(instancesFilePath)) {
            Input input = new Input(fileInputStream);
            processes = kryo.readObject(input, HashMap.class);
        }
        return processes;
    }

    HazelcastProcess find(String name)
            throws IOException {
        return findAll().get(name);
    }

    void remove(String name)
            throws IOException {
        Map<String, HazelcastProcess> processMap = findAll();
        if (processMap == null || !processMap.containsKey(name)) {
            throw new HazelcastException("No process found with pid: " + name);
        }
        if (!deleteProcessDirs(name)) {
            throw new HazelcastException("Process directories couldn't be deleted: " + name);
        }
        processMap.remove(name);
        updateFile(processMap);
    }

    private boolean deleteProcessDirs(String name) {
        String processPath = hazelcastHome + SEPARATOR + name;
        return deleteDirectory(new File(processPath));
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!deleteDirectory(file)) {
                    return false;
                }
            }
        }
        return directoryToBeDeleted.delete();
    }

    boolean exists(String name)
            throws IOException {
        return findAll().containsKey(name);
    }

    HazelcastProcess create()
            throws FileNotFoundException, UnsupportedEncodingException {
        String name = MobyNames.getRandomName(0);
        String processDir = createProcessDirs(name);
        String logFilePath = processDir + SEPARATOR + LOGS_DIR_STRING + SEPARATOR + LOGS_FILE_NAME_STRING;
        String loggingPropertiesPath = createLoggingPropertiesFile(processDir, logFilePath);
        return new HazelcastProcess(name, loggingPropertiesPath, logFilePath);
    }

    private void updateFile(Map<String, HazelcastProcess> processMap)
            throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(instancesFilePath);
             Output output = new Output(fileOut)) {
            kryo.writeObject(output, processMap);
        }
    }

    private String createProcessDirs(String name) {
        String processPath = hazelcastHome + SEPARATOR + name;
        String logPath = processPath + SEPARATOR + LOGS_DIR_STRING;
        boolean dirsCreated;
        try {
            dirsCreated = new File(logPath).mkdirs();
        } catch (Exception e) {
            throw new HazelcastException("Process directories couldn't be created.", e);
        }
        if (!dirsCreated) {
            throw new HazelcastException("Process directories couldn't be created.");
        }
        return processPath;
    }

    private String createLoggingPropertiesFile(String processDir, String logFilePath)
            throws FileNotFoundException, UnsupportedEncodingException {
        String loggingPropertiesPath = processDir + SEPARATOR + "logging.properties";
        PrintWriter printWriter = new PrintWriter(loggingPropertiesPath, StandardCharsets.UTF_8.name());
        String fileContent = "handlers= java.util.logging.FileHandler, java.util.logging.ConsoleHandler\n"
                + ".level= INFO\n"
                + "java.util.logging.FileHandler.pattern = " + logFilePath + "\n"
                + "java.util.logging.FileHandler.limit = 50000\n" + "java.util.logging.FileHandler.count = 1\n"
                + "java.util.logging.FileHandler.maxLocks = 100\n"
                + "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n"
                + "java.util.logging.FileHandler.append=true\n"
                + "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter";
        printWriter.println(fileContent);
        printWriter.close();
        return loggingPropertiesPath;
    }
}
