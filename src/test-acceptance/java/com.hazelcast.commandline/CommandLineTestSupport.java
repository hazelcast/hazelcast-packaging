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

package com.hazelcast.commandline;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CommandLineTestSupport {

    protected PrintStream out;
    protected PrintStream err;
    private ByteArrayOutputStream baosOut;
    private ByteArrayOutputStream baosErr;

    public CommandLineTestSupport() {
        baosOut = new ByteArrayOutputStream();
        baosErr = new ByteArrayOutputStream();
    }

    protected void resetOut() {
        baosOut.reset();
        baosErr.reset();
        out = new PrintStream(baosOut);
        err = new PrintStream(baosErr);
    }

    protected String captureOut() {
        out.flush();
        return new String(baosOut.toByteArray());
    }
}
