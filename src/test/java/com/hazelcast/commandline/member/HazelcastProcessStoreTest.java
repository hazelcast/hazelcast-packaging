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

package com.hazelcast.commandline.member;

import com.hazelcast.core.HazelcastException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * HazelcastProcessStore unit tests
 */
public class HazelcastProcessStoreTest {

    private Path hazelcastHome;
    private HazelcastProcessStore hazelcastProcessStore;
    private HazelcastProcess firstProcess;
    private String firstProcessName;

    /**
     * Creates a HazelcastProcessStore and the first HazelcastProcess
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        hazelcastHome = Files.createTempDirectory("hz-test");
        System.out.println(hazelcastHome);
        hazelcastProcessStore = new HazelcastProcessStore(hazelcastHome.toString());
        firstProcess = hazelcastProcessStore.create();
        firstProcessName = firstProcess.getName();
    }

    @Test
    public void test_initialState() throws IOException {
        // given setup

        // when nothing is done

        // then
        Assert.assertTrue("instances.dat must not exist initially", Files.notExists(hazelcastHome.resolve("instances.dat")));
        Assert.assertTrue("process directory must have been created", Files.isDirectory(hazelcastHome.resolve(firstProcessName)));
    }

    @Test
    public void test_save() throws IOException {
        // given setup

        // when
        hazelcastProcessStore.save(firstProcess);

        // then
        Assert.assertTrue("instances.dat must exist after save", Files.exists(hazelcastHome.resolve("instances.dat")));
    }

    @Test
    public void test_findAll() throws IOException {
        // given
        hazelcastProcessStore.save(firstProcess);

        // when
        Map<String, HazelcastProcess> processMap = hazelcastProcessStore.findAll();

        // then
        assertEquals("process map must have size 1", 1, processMap.size());
        assertNotNull("process map must contain the saved process", processMap.get(firstProcessName));
    }

    @Test
    public void test_findAllOnInitialState() throws IOException {
        // given setup

        // when
        Map<String, HazelcastProcess> processMap = hazelcastProcessStore.findAll();

        // then
        assertTrue("process map must be empty initially", processMap.isEmpty());
    }

    @Test
    public void test_updateFile() {
    }

    @Test
    public void test_find() throws IOException {
        // given
        firstProcess.setPid(999);
        hazelcastProcessStore.save(firstProcess);

        // when
        HazelcastProcess process = hazelcastProcessStore.find(firstProcessName);

        // then
        assertTrue("retrieved entry must be a different object from deserialization", process != firstProcess);
        assertEquals("pid must have been saved", 999, process.getPid());
    }

    @Test
    public void test_remove() throws IOException {
        // given
        hazelcastProcessStore.save(firstProcess);

        // when
        hazelcastProcessStore.remove(firstProcessName);

        // then
        assertNull(hazelcastProcessStore.find(firstProcessName));
        Assert.assertFalse("Process directory should be deleted.", Files.exists(hazelcastHome.resolve(firstProcessName)));
    }


    @Test(expected = HazelcastException.class)
    public void test_removeNonexistentProcess() throws IOException {
        // given setup

        // when
        hazelcastProcessStore.remove("someProcess");

        // then throw
    }

    @Test
    public void test_exists() throws IOException {
        // given
        hazelcastProcessStore.save(firstProcess);

        // when
        boolean exists = hazelcastProcessStore.exists(firstProcessName);

        // then
        assertTrue("must return true for saved process", exists);
    }

    @Test
    public void test_existsOnUnexistentProcess() throws IOException {
        // given setup

        // when
        boolean exists = hazelcastProcessStore.exists("someProcess");

        // then
        assertFalse("must return false for unsaved process", exists);
    }

    @Test
    public void test_create() throws FileNotFoundException, UnsupportedEncodingException {
        // given setup

        // when
        HazelcastProcess newProcess = hazelcastProcessStore.create();
        String newProcessName = newProcess.getName();

        // then
        Assert.assertTrue("process directory must have been created", Files.isDirectory(hazelcastHome.resolve(newProcessName)));
    }
}
