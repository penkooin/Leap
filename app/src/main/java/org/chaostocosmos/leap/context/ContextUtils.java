package org.chaostocosmos.leap.context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

public class ContextUtils {

    /**
     * Load metadata from file
     * @param metaPath
     * @throws IOException
     * @return
     */
    public static Map<String, Object> load(Path metaPath) throws IOException {
        Map<String, Object> metaMap = new HashMap<>();
        String metaName = metaPath.toFile().getName();
        String metaString = Files.readString(metaPath, StandardCharsets.UTF_8);
        if(metaName.endsWith(".yml") || metaName.endsWith(".yaml")) {
            metaMap = new Yaml().<Map<String, Object>> load(metaString);
        } else if(metaName.endsWith(".json")) {
            metaMap = new Gson().<Map<String, Object>> fromJson(metaString, Map.class);
        } else if(metaName.endsWith(".properites")) {
            metaMap = Arrays.asList(metaString.split(System.lineSeparator()))
                                 .stream().map(l -> new Object[]{l.substring(0, l.indexOf("=")).trim(), l.substring(l.indexOf("=")+1).trim()})
                                 .collect(Collectors.toMap(k -> (String)k[0], v -> v[1]));
        } else if(metaName.equals("trademark")) {
            metaMap = new HashMap<>() {{
                put("trademark", Files.readString(metaPath));
            }};
        } else {
            throw new IOException("Meta file not supported: "+metaName);
        } 
        return metaMap;
    }

    /**
     * Save Metadata
     * @param metaPath
     * @param metaMap
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void save(Path metaPath, Map<String, Object> metaMap) throws FileNotFoundException, IOException {
        String metaName = metaPath.toFile().getName();
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(metaPath.toFile()), StandardCharsets.UTF_8)) {
            if(metaName.endsWith(".yml")) {
                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Block style for pretty formatting
                options.setIndent(4); // Number of spaces for indentation
                options.setPrettyFlow(true); // Enables pretty flow style
                options.setIndicatorIndent(2); 
                new Yaml(options).dump(metaMap, osw);
            } else if(metaName.endsWith(".json")) {
                new Gson().toJson(metaMap, osw);
            } else if(metaName.endsWith(".properites")) {
                Properties prop = new Properties();
                prop.store(osw, null);
            } else if(metaName.equals("trademark")) {
                osw.write(metaMap.get("trademark")+"");
            } else {
                throw new IOException("Meta file not supported: "+metaName);
            }    
        }        
    }    
}
