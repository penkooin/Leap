package org.chaostocosmos.leap.http.context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.common.DataStructureOpr;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

/**
 * Metadata enum
 * 
 * @author 9ins
 */
public enum META {

    SERVER(Context.getHomePath().resolve("config").resolve("server.yml")),
    HOSTS(Context.getHomePath().resolve("config").resolve("hosts.yml")),
    MESSAGES(Context.getHomePath().resolve("config").resolve("messages.yml")),
    MIME(Context.getHomePath().resolve("config").resolve("mime.yml")),
    CHART(Context.getHomePath().resolve("config").resolve("chart.yml"));    

    Path metaPath;
    Map<String, Object> metaMap;
    Object meta;

    /**
     * Initializer
     * @param metaPath
     * @throws IOException
     * @throws NotSupportedException
     */
    META(Path metaPath) {
        this.metaPath = metaPath;
        try {
            load();
        } catch (IOException | NotSupportedException e) {            
            e.printStackTrace();
        }        
    }

    /**
     * Load metadata from file
     * @throws NotSupportedException
     * @throws IOException
     */
    public void load() throws NotSupportedException, IOException {
        String metaName = metaPath.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".")+1);
        String metaString = Files.readString(metaPath, StandardCharsets.UTF_8);            
        if(metaType.equalsIgnoreCase("yml")) {
            this.metaMap = new Yaml().<Map<String, Object>> load(metaString);
        } else if(metaType.equalsIgnoreCase("json")) {
            this.metaMap = new Gson().<Map<String, Object>> fromJson(metaString, Map.class);
        } else if(metaType.equalsIgnoreCase("properites")) {
            this.metaMap = Arrays.asList(metaString.split(System.lineSeparator()))
                                 .stream().map(l -> new Object[]{l.substring(0, l.indexOf("=")).trim(), l.substring(l.indexOf("=")+1).trim()})
                                 .collect(Collectors.toMap(k -> (String)k[0], v -> v[1]));
        } else {
            throw new NotSupportedException("Meta file not supported: "+metaName);
        }    
        if(this.name().equals("SERVER")) {
            this.meta = new Server<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("HOSTS")) {
            this.meta = new Hosts<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("MESSAGES")) {
            this.meta = new Messages<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("MIME")) {
            this.meta = new Mime<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("CHART")) {
            this.meta = new Chart<Map<String, Object>>(this.metaMap);
        } else {
            this.meta = this.metaMap;
        }            
    }

    /**
     * Get meta object
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getMeta() {
        return (T) this.meta;
    }

    /**
     * Get meta data Map
     * @return
     */
    public Map<String, Object> getMetaMap() {
        return this.metaMap;
    }

    /**
     * Set meta data value
     * @param pathExpr
     * @param value
     */
    public <T> void setMetaValue(String pathExpr, T value) {
        DataStructureOpr.<T>setValue(this.metaMap, pathExpr, value);
    }

    /**
     * Get meta data value
     */
    public <T> T getMetaValue(String pathExpr) {
        return DataStructureOpr.<T>getValue(this.metaMap, pathExpr);
    }

    /**
     * Get meta data Path
     * @return
     */
    public Path getMetaPath() {
        return this.metaPath;
    }

    /**
     * Save meta data
     */
    public void save() {
        String metaName = this.metaPath.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".")+1);
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(metaPath.toFile()), StandardCharsets.UTF_8)) {
            if(metaType.equalsIgnoreCase("yml")) {
                new Yaml().dump(this.metaMap, osw);
            } else if(metaType.equalsIgnoreCase("json")) {
                new Gson().toJson(this.metaMap, osw);
            } else if(metaType.equalsIgnoreCase("properites")) {
                Properties prop = new Properties();
                prop.store(osw, null);
            } else {
                throw new NotSupportedException("Meta file not supported: "+metaName);
            }    
        } catch(Exception e) {
            throw new RuntimeException(e);
        }        
    }
}
