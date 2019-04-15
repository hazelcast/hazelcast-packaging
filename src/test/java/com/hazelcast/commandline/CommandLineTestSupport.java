package com.hazelcast.commandline;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CommandLineTestSupport{

    static {
        HazelcastCommandLine.HAZELCAST_HOME = System.getProperty("user.home") + "/.hazelcastTest";
    }

    private ByteArrayOutputStream baosOut;
    private ByteArrayOutputStream baosErr;
    protected PrintStream out;
    protected PrintStream err;

    public CommandLineTestSupport() {
        baosOut = new ByteArrayOutputStream();
        baosErr = new ByteArrayOutputStream();
    }

    public void resetOut() {
        baosOut.reset();
        baosErr.reset();
        out = new PrintStream(baosOut);
        err = new PrintStream(baosErr);
    }

    public String captureOut() {
        out.flush();
        return new String(baosOut.toByteArray());
    }

}
