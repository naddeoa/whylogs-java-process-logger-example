package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Example {

    static void logger(){
        try (Logger logger = new Logger()) {

            Logger.LogData logData1 = new Logger.LogData(new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("col1", 1);
                    put("col2", "foo");
                    put("col3", 0.5);
                }});
                add(new HashMap<String, Object>() {{
                    put("col1", 2);
                    put("col2", "bar");
                    put("col3", 1.0);
                }});
                // Add more rows as necessary...
            }}, 1682459962000L);

            Logger.LogData logData2 = new Logger.LogData(new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("col1", 1);
                    put("col2", "foo");
                    put("col3", 0.5);
                }});
                add(new HashMap<String, Object>() {{
                    put("col1", 2);
                    put("col2", "bar");
                    put("col3", 1.0);
                }});
                // Add more rows as necessary...
            }}, 1682200790000L);

            logger.log(logData1);
            logger.log(logData2);
        }
    }
}
