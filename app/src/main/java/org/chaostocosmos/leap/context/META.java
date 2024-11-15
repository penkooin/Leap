package org.chaostocosmos.leap.context;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.common.data.DataStructureOpr;
import org.chaostocosmos.leap.enums.WAR_PATH;
import org.chaostocosmos.leap.resource.config.ResourceConfig;

/**
 * Metadata enum
 * 
 * @author 9ins
 */
public enum META implements Cloneable {
    SERVER(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("server.yml")),
    HOSTS(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("hosts.yml")),
    MESSAGE(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("message.yml")),
    MIME(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("mime.yml")),
    MONITOR(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("monitor.yml")),
    RESOURCE(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("resource.yml")),
    TRADEMARK(Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("trademark"));

    Path metaPath;
    Map<String, Object> metaMap;

    /**
     * Initializer
     * @param metaPath
     * @throws IOException 
     */
    META(Path metaPath) {
        this.metaPath = metaPath.toAbsolutePath().normalize();
        if(this.metaMap == null) {
            reload();
        }
    }

    /**
     * Reload metadata from file
     * @throws NotSupportedException
     * @throws IOException
     */
    public void reload() {
        try {
            this.metaMap = ContextUtils.load(this.metaPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            ContextUtils.save(this.metaPath, this.metaMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            return new ResourceConfig<Map<String, Object>>(this.metaMap);
        } else if(super.name().equals("TRADEMARK")) {
            return new Trademark<Map<String, Object>>(this.metaMap);
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
     * Get cloned object
     * @return
     * @throws CloneNotSupportedException
     */
    public Object getClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
