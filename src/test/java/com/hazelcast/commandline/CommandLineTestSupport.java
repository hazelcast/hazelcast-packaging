package com.hazelcast.commandline;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CommandLineTestSupport {
    private ByteArrayOutputStream baosOut;
    private ByteArrayOutputStream baosErr;
    protected PrintStream out;
    protected PrintStream err;

    void resetOut() {
        baosOut = new ByteArrayOutputStream();
        baosErr = new ByteArrayOutputStream();
        out = new PrintStream(baosOut);
        err = new PrintStream(baosErr);
    }

    String captureOut() {
        out.flush();
        return new String(baosOut.toByteArray());
    }

}
