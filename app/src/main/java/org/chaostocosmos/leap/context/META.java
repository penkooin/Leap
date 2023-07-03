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

import org.chaostocosmos.leap.common.DataStructureOpr;
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
    MESSAGES(Context.get().getHome().resolve("config").resolve("messages.yml")),
    MIME(Context.get().getHome().resolve("config").resolve("mime.yml")),
    CHART(Context.get().getHome().resolve("config").resolve("chart.yml"));    

    Path metaPath;
    Map<String, Object> metaMap;

    /**
     * Initializer
     * @param metaPath
     * @throws IOException
     * @throws NotSupportedException
     */
    META(Path metaPath) {
        this.metaPath = metaPath.toAbsolutePath().normalize();
        try {
            load(this.metaPath);
        } catch (IOException | NotSupportedException e) { 
            e.printStackTrace();
        }        
    }

    /**
     * Reload metadata from file
     * @throws NotSupportedException
     * @throws IOException
     */
    public synchronized void reload() throws NotSupportedException, IOException {
        load(this.metaPath);
    }

    /**
     * Load metadata from file
     * @param metaPath
     * @throws NotSupportedException
     * @throws IOException
     */
    public synchronized void load(Path metaPath) throws NotSupportedException, IOException {
        String metaName = metaPath.toFile().getName();
        String metaType = metaName.substring(metaName.lastIndexOf(".") + 1);
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
    }

    /**
     * Get meta data Map
     * @return
     */
    public Metadata<?> getMeta() {
        if(this.name().equals("SERVER")) {
            return new Server<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("HOSTS")) {
            return new Hosts<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("MESSAGES")) {
            return new Messages<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("MIME")) {
            return new Mime<Map<String, Object>>(this.metaMap);
        } else if(this.name().equals("CHART")) {
            return new Chart<Map<String, Object>>(this.metaMap);
        } else {
            return new Metadata<Map<String, Object>>(this.metaMap);
        }            
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
