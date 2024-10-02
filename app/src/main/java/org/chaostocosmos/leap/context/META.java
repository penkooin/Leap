package org.chaostocosmos.leap.context;

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

import org.chaostocosmos.leap.common.data.DataStructureOpr;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.resource.config.ResourceProviderConfig;
import org.yaml.snakeyaml.Yaml;
import com.google.gson.Gson;

/**
 * Metadata enum
 * 
 * @author 9ins
 */
public enum META {
    SERVER(Context.get().getHome().resolve("config").resolve("server.yml")),
    HOSTS(Context.get().getHome().resolve("config").resolve("hosts.yml")),
    MESSAGE(Context.get().getHome().resolve("config").resolve("message.yml")),
    MIME(Context.get().getHome().resolve("config").resolve("mime.yml")),
    MONITOR(Context.get().getHome().resolve("config").resolve("monitor.yml")),
    RESOURCE(Context.get().getHome().resolve("config").resolve("resource-provider.yml"));    

    Path metaPath;
    Map<String, Object> metaMap;

    /**
     * Initializer
     * @param metaPath
     */
    META(Path metaPath) {
        this.metaPath = metaPath.toAbsolutePath().normalize();
        try {
            load(this.metaPath);
        } catch (IOException | NotSupportedException e) { 
            LoggerFactory.getLogger().throwable(e);
        }        
    }

    /**
     * Reload metadata from file
     * @throws NotSupportedException
     * @throws IOException
     */
    public void reload() throws IOException, NotSupportedException {
        load(this.metaPath);
    }

    /**
     * Load metadata from file
     * @param metaPath
     * @throws NotSupportedException
     * @throws IOException
     */
    public void load(Path metaPath) throws IOException, NotSupportedException {
        String metaName = metaPath.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".") + 1);
        String metaString = Files.readString(metaPath, StandardCharsets.UTF_8);
        if(metaType.equalsIgnoreCase("yml") || metaType.equalsIgnoreCase("yaml")) {
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
    }

    /**
     * Get meta data Map
     * @return
     */
    public Metadata<?> getMeta() {
        if(super.name().equals("SERVER")) {
            return new Server<Map<String, Object>>(this.metaMap);
        } else if(super.name().equals("HOSTS")) {            
            return new Hosts<Map<String, Object>>(this.metaMap);
        } else if(super.name().equals("MESSAGE")) {
            return new Message<Map<String, Object>>(this.metaMap);            
        } else if(super.name().equals("MIME")) {
            return new Mime<Map<String, Object>>(this.metaMap);            
        } else if(super.name().equals("MONITOR")) {
            return new Monitor<Map<String, Object>>(this.metaMap);
        } else if(super.name().equals("RESOURCE")) {
            return new ResourceProviderConfig<Map<String, Object>>(this.metaMap);
        } else {
            return new Metadata<>(this.metaMap);
        }
    }

    /**
     * Set metadata value
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
