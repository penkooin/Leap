package org.chaostocosmos.leap.resource.utils;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.resource.config.ResourceConfig;
import org.chaostocosmos.leap.resource.config.SIZE;
import org.chaostocosmos.leap.resource.config.SizeConstants;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

/**
 * ResourceUtils
 * 
 * @author 9ins
 */
public class ResourceUtils {

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
     * Get byte size from number string
     * @param numericString
     * @return
     */
    public static long fromString(String numericString) {
        Matcher matcher = SizeConstants.NUMBER_PATTERN.matcher(numericString.trim());
        if (matcher.matches()) {
            //String numberPart = matcher.group(1);  // Numeric part (can be integer or decimal)
            //String[] numberUnit = new String[] {numberPart, unitPart};
            String unitPart = matcher.group(2);    // Unit part (e.g., MB, GB, kb)
            return  SIZE.valueOf(unitPart.toUpperCase()).byteSize();
        } else {
            throw new IllegalArgumentException("Invalid size format: " + numericString);
        }        
    }
    
}
