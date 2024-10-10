package org.chaostocosmos.leap.resource.config;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

/**
 * ResourceUtils
 * 
 * @author 9ins
 */
public class ConfigUtils {    

    /**
     * Load
     * @param path
     * @return
     */
    public static Map<String, Object> loadConfig(Path path) {
        String metaName = path.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".") + 1);
        String metaString;
        try {
            metaString = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> configMap = null;
        if(metaType.equalsIgnoreCase("yml") || metaType.equalsIgnoreCase("yaml")) {
            configMap = new Yaml().<Map<String, Object>> load(metaString);
        } else if(metaType.equalsIgnoreCase("json")) {
            configMap = new Gson().<Map<String, Object>> fromJson(metaString, Map.class);
        } else if(metaType.equalsIgnoreCase("properites")) {
            configMap = Arrays.asList(metaString.split(System.lineSeparator()))
                            .stream().map(l -> new Object[]{l.substring(0, l.indexOf("=")).trim(), l.substring(l.indexOf("=")+1).trim()})
                            .collect(Collectors.toMap(k -> (String)k[0], v -> v[1]));
        } else {
            throw new RuntimeException("Meta file not supported: "+metaName);
        }
        return configMap;
    } 

    /**
     * Load
     * @param path
     * @return
     * @throws IOException
     */
    public static ResourceConfig<?> loadConfigObject(Path path) {
        String metaName = path.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".") + 1);
        //String metaString = Files.readString(path, StandardCharsets.UTF_8);
        ResourceConfig<?> configMap = null;
        try {
            if(metaType.equalsIgnoreCase("yml") || metaType.equalsIgnoreCase("yaml")) {
                FileInputStream fis = new FileInputStream(path.toFile());
                configMap = new Yaml().loadAs(fis, ResourceConfig.class);
            } else if(metaType.equalsIgnoreCase("json")) {
                FileReader fr = new FileReader(path.toFile());
                configMap = new Gson().fromJson(fr, ResourceConfig.class);
            } else {
                throw new IllegalArgumentException("Meta file not supported: "+metaName);
            }    
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return configMap;
    } 

    /**
     * Flat structured Map to flat Map
     * @param meta
     * @param parentKey
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> flattenMap (Map<String, Object> meta, String parentKey) {
        Map<String, Object> configMap = new HashMap<>();
        meta.forEach((key, value) -> {
            String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;
            if (value instanceof Map) {
                configMap.putAll(flattenMap((Map<String, Object>) value, fullKey));
            } else {
                configMap.put(fullKey, value);
            }
        });
        return configMap;
    }

    /**
     * Convert Properties to Map
     * @param configMap
     * @return
     */
    public static Properties maptoProperties(Map<String, Object> configMap) {
        Properties properties = new Properties();
        properties.putAll(configMap);
        return properties;
    }
}
