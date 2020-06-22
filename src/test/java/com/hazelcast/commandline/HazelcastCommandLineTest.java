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

import com.hazelcast.commandline.test.annotation.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

//import static com.hazelcast.commandline.member.HazelcastProcess.Status.RUNNING;
//import static com.hazelcast.commandline.member.HazelcastProcess.Status.STOPPED;
//import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class HazelcastCommandLineTest {
    private ProcessExecutor processExecutor;
    private HazelcastCommandLine hazelcastCommandLine;
    private PrintStream out;
//    private HazelcastProcessStore hazelcastProcessStore;
//    private HazelcastProcess hazelcastProcess;
//    private HazelcastProcess hazelcastStoppedProcess;

    @Before
    public void setUp()
            throws IOException {
//        hazelcastProcessStore = mock(HazelcastProcessStore.class);
        processExecutor = mock(ProcessExecutor.class);
        Process process = mock(Process.class);
        out = mock(PrintStream.class);
//        hazelcastProcess = mock(HazelcastProcess.class);
//        when(hazelcastProcess.getStatus()).thenReturn(RUNNING);
//
//        hazelcastStoppedProcess = mock(HazelcastProcess.class);
//        when(hazelcastStoppedProcess.getStatus()).thenReturn(STOPPED);

        when(process.getInputStream()).thenReturn(mock(InputStream.class));
//        when(processExecutor.extractPid(process)).thenReturn(99999);
//        when(hazelcastProcessStore.create()).thenReturn(hazelcastProcess);

//        hazelcastCommandLine = new MemberCommandLine(out, mock(PrintStream.class), hazelcastProcessStore, processExecutor, false);
        hazelcastCommandLine = new HazelcastCommandLine(out, mock(PrintStream.class), processExecutor, false);
    }

    @Test
    public void test_start()
            throws IOException, InterruptedException {
        //when
        hazelcastCommandLine.start(null, null, null, null, null);
        //then
        verify(processExecutor, times(1)).buildAndStart(anyList());
    }

//    @Test
//    public void test_start_withConfigFile()
//            throws Exception {
//        // given
//        String configFile = "path/to/test-hazelcast.xml";
//        // when
//        hazelcastCommandLine.start(configFile, null, null, null, false, null, null);
//        // then
//        verify(processExecutor)
//                .buildAndStart((List<String>) argThat(Matchers.hasItem("-Dhazelcast.config=" + configFile)), eq(false));
//    }
//
//    @Test
//    public void test_start_withClusterName()
//            throws Exception {
//        // given
//        String clusterName = "member-command-line-test";
//        // when
//        hazelcastCommandLine.start(null, clusterName, null, null, false, null, null);
//        // then
//        verify(processExecutor).buildAndStart((List<String>) argThat(Matchers.hasItem("-Dgroup.name=" + clusterName)), eq(false));
//    }
//
//    @Test
//    public void test_start_withPort()
//            throws Exception {
//        // given
//        String port = "9999";
//        // when
//        hazelcastCommandLine.start(null, null, port, null, false, null, null);
//        // then
//        verify(processExecutor).buildAndStart((List<String>) argThat(Matchers.hasItem("-Dnetwork.port=" + port)), eq(false));
//    }
//
//    @Test
//    public void test_start_withInterface()
//            throws Exception {
//        // given
//        String hzInterface = "1.1.1.1";
//        // when
//        hazelcastCommandLine.start(null, null, null, hzInterface, false, null, null);
//        // then
//        verify(processExecutor).buildAndStart((List<String>) argThat(
//                Matchers.hasItems("-Dnetwork.interface=" + hzInterface, "-Dbind.any=false", "-Dinterfaces.enabled=true")),
//                eq(false));
//    }
//
//    @Test
//    public void test_start_withForeground()
//            throws Exception {
//        // given
//        boolean foreground = true;
//        // when
//        hazelcastCommandLine.start(null, null, null, null, foreground, null, null);
//        // then
//        verify(processExecutor).buildAndStart(anyList(), eq(foreground));
//    }
}
