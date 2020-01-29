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

import com.hazelcast.commandline.test.annotation.UnitTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hazelcast.commandline.member.HazelcastProcess.Status.RUNNING;
import static com.hazelcast.commandline.member.HazelcastProcess.Status.STOPPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@Category(UnitTest.class)
public class MemberCommandLineTest {
    private ProcessExecutor processExecutor;
    private MemberCommandLine memberCommandLine;
    private PrintStream out;
    private HazelcastProcessStore hazelcastProcessStore;
    private HazelcastProcess hazelcastProcess;
    private HazelcastProcess hazelcastStoppedProcess;

    @Before
    public void setUp()
            throws IOException {
        hazelcastProcessStore = mock(HazelcastProcessStore.class);
        processExecutor = mock(ProcessExecutor.class);
        Process process = mock(Process.class);
        out = mock(PrintStream.class);
        hazelcastProcess = mock(HazelcastProcess.class);
        when(hazelcastProcess.getStatus()).thenReturn(RUNNING);

        hazelcastStoppedProcess = mock(HazelcastProcess.class);
        when(hazelcastStoppedProcess.getStatus()).thenReturn(STOPPED);

        when(process.getInputStream()).thenReturn(mock(InputStream.class));
        when(processExecutor.extractPid(process)).thenReturn(99999);
        when(hazelcastProcessStore.create()).thenReturn(hazelcastProcess);

        memberCommandLine = new MemberCommandLine(out, mock(PrintStream.class), hazelcastProcessStore, processExecutor, false);
    }

    @Test
    public void test_start()
            throws IOException, InterruptedException {
        //when
        memberCommandLine.start(null, null, null, null, false, null, null);
        //then
        verify(hazelcastProcessStore, times(1)).create();
        verify(processExecutor, times(1)).buildAndStart(anyList(), eq(false));
        verify(hazelcastProcessStore, times(1)).save(any());
    }

    @Test
    public void test_start_withConfigFile()
            throws Exception {
        // given
        String configFile = "path/to/test-hazelcast.xml";
        // when
        memberCommandLine.start(configFile, null, null, null, false, null, null);
        // then
        verify(processExecutor)
                .buildAndStart((List<String>) argThat(Matchers.hasItem("-Dhazelcast.config=" + configFile)), eq(false));
    }

    @Test
    public void test_start_withClusterName()
            throws Exception {
        // given
        String clusterName = "member-command-line-test";
        // when
        memberCommandLine.start(null, clusterName, null, null, false, null, null);
        // then
        verify(processExecutor).buildAndStart((List<String>) argThat(Matchers.hasItem("-Dgroup.name=" + clusterName)), eq(false));
    }

    @Test
    public void test_start_withPort()
            throws Exception {
        // given
        String port = "9999";
        // when
        memberCommandLine.start(null, null, port, null, false, null, null);
        // then
        verify(processExecutor).buildAndStart((List<String>) argThat(Matchers.hasItem("-Dnetwork.port=" + port)), eq(false));
    }

    @Test
    public void test_start_withInterface()
            throws Exception {
        // given
        String hzInterface = "1.1.1.1";
        // when
        memberCommandLine.start(null, null, null, hzInterface, false, null, null);
        // then
        verify(processExecutor).buildAndStart((List<String>) argThat(
                Matchers.hasItems("-Dnetwork.interface=" + hzInterface, "-Dbind.any=false", "-Dinterfaces.enabled=true")),
                eq(false));
    }

    @Test
    public void test_start_withForeground()
            throws Exception {
        // given
        boolean foreground = true;
        // when
        memberCommandLine.start(null, null, null, null, foreground, null, null);
        // then
        verify(processExecutor).buildAndStart(anyList(), eq(foreground));
    }

    @Test
    public void test_stop()
            throws IOException, InterruptedException {
        //given
        String processName = "aProcess";
        when(hazelcastProcessStore.find(processName)).thenReturn(hazelcastProcess);
        //when
        memberCommandLine.stop(processName);
        //then
        verify(processExecutor).exec((List<String>) argThat(Matchers.hasItems("kill", "-15")));
    }

    @Test
    public void test_stop_withNoProcess()
            throws IOException, InterruptedException {
        //given
        String processName = "aProcess";
        when(hazelcastProcessStore.find(processName)).thenReturn(null);
        //when
        memberCommandLine.stop(processName);
        //then
        verifyZeroInteractions(processExecutor);
        verify(hazelcastProcessStore, times(0)).remove(any());
    }

    @Test
    public void test_remove()
            throws IOException, InterruptedException {
        //given
        String processName = "aProcess";
        when(hazelcastProcessStore.find(processName)).thenReturn(hazelcastStoppedProcess);
        //when
        memberCommandLine.remove(processName);
        //then
        verify(hazelcastProcessStore, times(1)).remove(processName);
    }

    @Test
    public void test_remove_runningProcess()
            throws IOException, InterruptedException {
        //given
        String processName = "aProcess";
        when(hazelcastProcessStore.find(processName)).thenReturn(hazelcastProcess);
        //when
        memberCommandLine.remove(processName);
        //then
        verify(hazelcastProcessStore, never()).remove(processName);
    }

    @Test
    public void test_remove_withNoProcess()
            throws IOException, InterruptedException {
        //given
        String processName = "aProcess";
        when(hazelcastProcessStore.find(processName)).thenReturn(null);
        //when
        memberCommandLine.remove(processName);
        //then
        verifyZeroInteractions(processExecutor);
        verify(hazelcastProcessStore, times(0)).remove(any());
    }

    @Test
    public void test_list()
            throws IOException, InterruptedException {
        //given
        String process1Name = "process1";
        String process2Name = "process2";
        HazelcastProcess process1 = mock(HazelcastProcess.class);
        when(process1.getName()).thenReturn(process1Name);
        HazelcastProcess process2 = mock(HazelcastProcess.class);
        when(process2.getName()).thenReturn(process2Name);
        Map<String, HazelcastProcess> processMap = new HashMap<>();
        processMap.put(process1Name, process1);
        processMap.put(process2Name, process2);
        when(hazelcastProcessStore.findAll()).thenReturn(processMap);
        //when
        memberCommandLine.list("", true, false);
        //then
        verify(out, times(processMap.size())).println(anyString());
    }
}
