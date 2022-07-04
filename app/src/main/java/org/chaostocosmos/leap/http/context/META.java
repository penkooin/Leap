package org.chaostocosmos.leap.http.context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.commons.DataStructureOpr;
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

    /**
     * Initializer
     * @param metaPath
     */
    @SuppressWarnings("unchecked")
    META(Path metaPath) {
        this.metaPath = metaPath;
        String metaName = this.metaPath.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".")+1);
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(metaPath.toFile()), StandardCharsets.UTF_8)) {
            if(metaType.equalsIgnoreCase("yml")) {
                this.metaMap = (Map<String, Object>)new Yaml().load(isr);
            } else if(metaType.equalsIgnoreCase("json")) {
                this.metaMap = (Map<String, Object>)new Gson().fromJson(isr, Map.class);
            } else if(metaType.equalsIgnoreCase("properites")) {
                Properties prop = new Properties();
                prop.load(isr);
                this.metaMap = prop.entrySet().stream().collect(Collectors.toMap(e -> (String)e.getKey(), e -> e.getValue()));
            } else {
                throw new NotSupportedException("Meta file not supported: "+metaName);
            }    
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
     * Set meta data value
     * @param metaMap
     */
    public void setMetaMap(Map<String, Object> metaMap) {
        this.metaMap = metaMap;
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
