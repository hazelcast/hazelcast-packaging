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

    static {
        HazelcastCommandLine.HAZELCAST_HOME = System.getProperty("user.home") + "/.hazelcastTest";
    }

}
