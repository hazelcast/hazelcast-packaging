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

package com.hazelcast.commandline.managementcenter;

import com.hazelcast.commandline.ProcessExecutor;
import com.hazelcast.commandline.test.annotation.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class ManagementCenterCommandLineTest {
    private ProcessExecutor processExecutor;
    private ManagementCenterCommandLine mcCommandLine;
    private PrintStream out;

    @Before
    public void setUp()
            throws IOException {
        processExecutor = mock(ProcessExecutor.class);
        Process process = mock(Process.class);
        out = mock(PrintStream.class);

        when(process.getInputStream()).thenReturn(mock(InputStream.class));

        mcCommandLine = new ManagementCenterCommandLine(out, mock(PrintStream.class), processExecutor);
    }

    @Test
    public void test_start()
            throws IOException, InterruptedException {
        //when
        mcCommandLine.start(null, null, null, false, false);
        //then
        verify(processExecutor, times(1)).buildAndStart(anyList());
    }
}
