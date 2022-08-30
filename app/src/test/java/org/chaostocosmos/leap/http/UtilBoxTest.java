/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


class UtilBoxTest {
    
    private static final Map<String, String> date_format_regexps = new HashMap<String, String>() {{
		put("^\\d{4}\\d{1,2}\\d{1,2}$", "yyyyMMdd");
		put("^\\d{1,2}-\\d{1,2}-\\d{2}$", "yy-MM-dd");
		put("^\\d{1,2}\\d{1,2}\\d{1,2}$", "yyMMdd");	
		put("^\\d{1,2}/\\d{1,2}/\\d{2}$", "yy/MM/dd");
		put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
		put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
		put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
		put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
		put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
		put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
		put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
		put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
		put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
		put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
		put("^\\d{4}\\d{1,2}\\d{1,2}\\d{1,2}\\d{2}$", "yyyyMMddHHmm");
		put("^\\d{4}\\d{1,2}\\d{1,2}\\d{1,2}\\d{2}\\d{2}$", "yyyyMMddHHmmss");
		put("^\\d{4}\\d{1,2}\\d{1,2}\\s\\d{1,2}\\d{2}$", "yyyyMMdd HHmm");
		put("^\\d{4}\\d{1,2}\\d{1,2}\\s\\d{1,2}\\d{2}\\d{2}$", "yyyyMMdd HHmmss");
		put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
		put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
		put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
		put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
		put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s[가-힝]{2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd a KK:mm:ss");
		put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s[가-힝]{2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd a KK:mm:ss");    
	}};    

    @Test
    public void testSomting() throws UnknownHostException {
        String host = "localhost";
        InetAddress ia = InetAddress.getByName(host);
        System.out.println(ia.getHostName());
    }

    @Test
    public void testInstantiate() throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String qualifiedClassName = "com.nhn.was.servlet.TimeServletImpl";
        Class<?> clazz = classLoader.loadClass(qualifiedClassName);
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        constructor.newInstance();
        System.out.println("ok");
    }

    @Test
    public void testIO() throws IOException {
        String str = "a\r\nb\r\n\r\nc";
        BufferedReader reader = new BufferedReader(new StringReader(str));
        for(String line; (line=reader.readLine()) != null; ) {
            System.out.println(line.length());
        }
    }

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static void testGson() {
        Map<String, Object> map = new HashMap<>() {{ 
            //put("CPU", new HashMap<String, Object>() {{
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
                        put("VALUES", new ArrayList<>());
                    }});
                    put("SYSTEM", new HashMap<String, Object>() {{ 
                        put("ELEMENT", "System CPU load");
                        put("LABEL", "System CPU load");
                        put("COLOR", Arrays.asList(180,180,140));
                        put("VALUES", new ArrayList<>());
                    }});                  
                }});
            //}});
        }};
        String json = gson.toJson(map, Map.class);
        System.out.println(json);
    }

    public static void main(String[] args) {
        int val = 1 / 12;
        System.out.println(val);
    }

}
