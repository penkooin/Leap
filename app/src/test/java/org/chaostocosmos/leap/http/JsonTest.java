package org.chaostocosmos.leap.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonTest {

    static ObjectMapper om = new ObjectMapper();
    static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    static HashMap<String, Object> map = new HashMap<>() {{ 
        put("CPU", new HashMap<String, Object>() {{
            put("GRAPH", "LINE");
            put("INTERPOLATE", "SPLINE");
            put("WIDTH", 800);
            put("HEIGHT", 600);
            put("XINDEX", Arrays.asList("0", "", "2", "", "3", "", "4", "", "5", "", "6", "", "7", "", "8", "", "9", "", "10"));
            put("YINDEX", Arrays.asList(50, 80, 500));
            put("ELEMENTS", new HashMap<String, Object>() {{
                put("PROCESS", new HashMap<String, Object>() {{ 
                    put("ELEMENT", "Leap CPU load");
                    put("LABEL", "Leap CPU load");
                    put("COLOR", Arrays.asList(180,130,130));
                    put("VALUES", new ArrayList());
                }});
                put("SYSTEM", new HashMap<String, Object>() {{ 
                    put("ELEMENT", "System CPU load");
                    put("LABEL", "System CPU load");
                    put("COLOR", Arrays.asList(180,180,140));
                    put("VALUES", new ArrayList());
                }});                  
            }});
        }});
    }};

    public static void testGson() throws InterruptedException {
        String json = gson.toJson(map, new TypeToken<HashMap<String, Object>>() {}.getType());
        System.out.println(json);        
    }

    public static void testJackson() throws JsonProcessingException {
        String json = om.writeValueAsString(map);
        System.out.println(json);
    }    

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        testGson();
        testJackson();
    }
}
