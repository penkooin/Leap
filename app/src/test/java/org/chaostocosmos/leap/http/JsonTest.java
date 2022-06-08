package org.chaostocosmos.leap.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonTest {

    static ObjectMapper om = new ObjectMapper();
    static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    private static Map<String, Object> buildMonitorSchema() { 
        return new HashMap<>() {{ 
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
            put("MEMORY", new HashMap<String, Object>() {{
                put("GRAPH", "AREA");
                put("INTERPOLATE", "SPLINE");
                put("WIDTH", 800);
                put("HEIGHT", 600);
                put("XINDEX", Arrays.asList("0", "", "2", "", "3", "", "4", "", "5", "", "6", "", "7", "", "8", "", "9", "", "10"));
                put("YINDEX", Arrays.asList(1024*1000*500, 1024*1000*1000, 1024*1000*5000, 1024*1000*10000, 1024*1000*15000, 1024*1000*30000));
                put("ELEMENTS", new HashMap<String, Object>() {{
                    put("PHYSICAL", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Physical memory");
                        put("LABEL", "Physical memory");
                        put("COLOR", Arrays.asList(180,130,130));
                        put("VALUES", new ArrayList());
                    }});
                    put("USED", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Physical used");
                        put("LABEL", "Physical used");
                        put("COLOR", Arrays.asList(150,200,158));
                        put("VALUES", new ArrayList());
                    }});
                    put("PROCESS", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Process used");
                        put("LABEL", "Physical used");
                        put("COLOR", Arrays.asList(150,200,158));
                        put("VALUES", new ArrayList());
                    }});
                }});
            }});            
            put("THREAD", new HashMap<String, Object>() {{
                put("GRAPH", "LINE");
                put("INTERPOLATE", "SPLINE");
                put("WIDTH", 800);
                put("HEIGHT", 600);
                put("XINDEX", Arrays.asList("0", "", "2", "", "3", "", "4", "", "5", "", "6", "", "7", "", "8", "", "9", "", "10"));
                put("YINDEX", Arrays.asList(50, 0));
                put("ELEMENTS", new HashMap<String, Object>() {{
                    put("MAX", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread max");
                        put("LABEL", "max");
                        put("COLOR", Arrays.asList(180,130,130));
                        put("VALUES", new ArrayList());
                    }});
                    put("CORE", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread core");
                        put("LABEL", "core");
                        put("COLOR", Arrays.asList(150,200,158));
                        put("VALUES", new ArrayList());
                    }});
                    put("ACTIVE", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread active");
                        put("LABEL", "active");
                        put("COLOR", Arrays.asList(150,130,158));
                        put("VALUES", new ArrayList());
                    }});
                    put("QUEUED", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread queued");
                        put("LABEL", "queued");
                        put("COLOR", Arrays.asList(130,180,110));
                        put("VALUES", new ArrayList());
                    }});
                }});
            }});            
        }};        
    }

    public static void testGson() throws InterruptedException {
        String json = gson.toJson(buildMonitorSchema(), new TypeToken<HashMap<String, Object>>() {}.getType());
        System.out.println(json);        
    }

    public static void testJackson() throws JsonProcessingException {
        String json = om.writeValueAsString(buildMonitorSchema());
        System.out.println(json);
    }    

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        testGson();
        testJackson();
    }
}
