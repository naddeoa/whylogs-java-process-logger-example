package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import py4j.ClientServer;
import py4j.GatewayServer;

public class Main {
    public static void main(String[] args) {
        ClientServer clientServer = new ClientServer(null);
        // We get an entry point from the Python side
        WhylogsLogger logger = (WhylogsLogger ) clientServer.getPythonServerEntryPoint(new Class[] { WhylogsLogger .class });
        // Java calls Python without ever having been called from Python

        List<String> columns = new ArrayList<>();
        columns.add("iicol1");
        columns.add("iicol2");
        columns.add("iicol3");

        List<List<Object>> data = new ArrayList<>();

        List<Object> row1 = new ArrayList<>();
        row1.add(1);
        row1.add("FOO");
        row1.add(7.8);
        data.add(row1);

        List<Object> row2 = new ArrayList<>();
        row2.add(4);
        row2.add("BAR");
        row2.add(77.8);
        data.add(row2);

        List<Object> row3 = new ArrayList<>();
        row3.add(3);
        row3.add("BAZ");
        row3.add(25.8);
        data.add(row3);

        // Use whylogs
        logger.log(columns, data);

        clientServer.shutdown();
    }
}