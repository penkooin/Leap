package org.chaostocosmos.metadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.common.DataStructureOpr;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * MetadataStorage
 * 
 * @author 9ins
 */
public class Metadata {
    /**
     * Metadata file path
     */
    File metaFile;

    /**
     * Metadata Map object
     */
    Map<String, Object> metadata;    

    /**
     * Constructs with meta path string
     * @param metaFile
     */
    public Metadata(String metaFile) {
        this(Paths.get(metaFile));
    }

    /**
     * Constructs with meta file
     * @param metaFile
     */
    public Metadata(Path metaFile) {
        this(metaFile.toFile());
    }

    /**
     * Constructs with meta file path object
     * @param metaFile
     */
    public Metadata(File metaFile) {
        if(metaFile.isDirectory()) {
            throw new IllegalArgumentException("Metadata file cannot be directory!!!");
        } else if(!metaFile.exists()) {
            throw new IllegalArgumentException("Metadata file not exist!!!");
        }
        this.metaFile = metaFile;        
        this.metadata = load(metaFile);
    }

    /**
     * Load metadata from file
     * @throws NotSupportedException
     * @throws IOException
     */
    public Map<String, Object> load(File metaFile) {
        String metaName = metaFile.getName();
        String metaExt = metaName.substring(metaName.lastIndexOf(".")+1);
        String metaString;
        try {
            metaString = Files.readString(metaFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(metaExt.equalsIgnoreCase(META_EXT.YAML.name()) 
                || metaExt.equalsIgnoreCase(META_EXT.YML.name())) {
            return new Yaml().<Map<String, Object>> load(metaString);
        } else if(metaExt.equalsIgnoreCase(META_EXT.JSON.name())) {
            return new Gson().<Map<String, Object>> fromJson(metaString, Map.class);
        } else if(metaExt.equalsIgnoreCase(META_EXT.PROPERTIES.name()) 
                || metaExt.equalsIgnoreCase(META_EXT.CONFIG.name()) 
                || metaExt.equalsIgnoreCase(META_EXT.CONF.name())) {
            return Arrays.asList(metaString.split(System.lineSeparator()))
                         .stream().map(l -> new Object[]{l.substring(0, l.indexOf("=")).trim(), l.substring(l.indexOf("=")+1).trim()})
                         .collect(Collectors.toMap(k -> (String)k[0], v -> v[1]));
        } else {
            throw new IllegalArgumentException("Metadata file extention not supported: "+metaName);
        }    
    }

    /**
     * Save metadata to file
     * @param metaFile
     * @throws IOException
     */
    public void save(File metaFile) throws IOException {
        String metaName = metaFile.getName();
        String metaExt = metaName.substring(metaName.lastIndexOf(".")+1);
        try(FileWriter writer = new FileWriter(metaFile)) {
            if(metaExt.equalsIgnoreCase(META_EXT.YAML.name()) || metaExt.equalsIgnoreCase(META_EXT.YML.name())) {
                new Yaml().dump(this.metadata, writer);
            } else if(metaExt.equalsIgnoreCase(META_EXT.JSON.name())) {                
                new GsonBuilder().setPrettyPrinting().create().toJson(this.metadata, writer);
            } else if(metaExt.equalsIgnoreCase(META_EXT.PROPERTIES.name()) || metaExt.equalsIgnoreCase(META_EXT.CONFIG.name()) || metaExt.equalsIgnoreCase(META_EXT.CONF.name())) {
                Properties properties = new Properties();
                properties.putAll(this.metadata);
                properties.store(writer, null);
            } else {
                throw new IllegalArgumentException("Metadata file extention not supported: "+metaName);
            }                    
        }
    }

    /**
     * Get metadata file
     * @return
     */
    public File getMetaFile() {
        return this.metaFile;
    }
    
    /**
     * Get value by expression
     * @param <V> value type
     * @param expr metadata path expression
     * @return
     */
    public <V> V getValue(String expr) {
        V value = MetaStructureOpr.<V> getValue(this.metadata, expr);
        if(value != null) {
            return value;
        } else {
            throw new IllegalArgumentException("There isn't exist value on specified expression: "+expr);
        }
    }

    /**
     * Set value on specified position
     * @param <V>
     * @param expr
     * @param value
     */
    public <V> void setValue(String expr, V value) {
        MetaStructureOpr.<V> setValue(this.metadata, expr, value);
        //System.out.println(this.getClass().getCanonicalName()+"="+Chart.class.getCanonicalName());
    }

    /**
     * Add metadata value placed on expression
     * @param expr
     * @param value
     */
    @SuppressWarnings("unchecked")
    public <V> void addValue(String expr, V value) {
        Object parent = MetaStructureOpr.<V> getValue(this.metadata, expr.substring(0, expr.lastIndexOf(".")));
        if(parent == null) {
            throw new IllegalArgumentException("Specified expression's parent is not exist: "+expr);
        } else if(parent instanceof List) {
            ((List<Object>) parent).add(value);
        } else if(parent instanceof Map) {
            ((Map<String, Object>) parent).put(expr.substring(expr.lastIndexOf(".")+1), value);
        } else {
            throw new RuntimeException("Parent  type is wrong. Metadata structure failed: "+parent);
        }
    }

    /**
     * Remove metadata value with the expression
     * @param expr
     */
    @SuppressWarnings("unchecked")
    public <V> V removeValue(String expr) {
        Object parent = DataStructureOpr.<V> getValue(this.metadata, expr.substring(0, expr.lastIndexOf(".")));
        V value = null;
        if(parent == null) {
            throw new IllegalArgumentException("Specified expression's parent is not exist: "+expr);
        } else if(parent instanceof List) {
            int idx = Integer.valueOf(expr.substring(expr.lastIndexOf(".")+1));
            value = (V) ((List<Object>) parent).remove(idx);
        } else if(parent instanceof Map) {
            value = (V) ((Map<String, Object>) parent).remove(expr.substring(expr.lastIndexOf(".")+1));
        } else {
            throw new RuntimeException("Parent data type is wired. Context data structure failed: "+parent);
        }
        return value;
    }

    /**
     * Exists context value by specified expression
     */
    public boolean exists(String expr) {
        Object value = DataStructureOpr.getValue(this.metadata, expr);
        if(value != null) {
            return true;
        } else {
            return false;
        }
    }    

    /**
     * Get metadata Map
     * @return
     */
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    @Override
    public String toString() {
        return this.metaFile.toString()+"\n"+metadata.toString();
    }    
}
