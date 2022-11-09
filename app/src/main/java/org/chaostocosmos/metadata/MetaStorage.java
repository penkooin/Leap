package org.chaostocosmos.metadata;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MetadataContext
 * 
 * @author 9ins
 */
public class MetaStorage {
    /**
     * Metadata directory path
     */
    Path metadataDir;

    /**
     * Metadata Map
     */
    Map<Path, Metadata> metadataMap;   

    /**
     * Metadata listener list
     */
    List<MetaListener> metadataListeners;

    /**
     * Constructs with metadata directory path
     * @param metadataDir
     */
    public MetaStorage(String metadataDir) {
        this(Paths.get(metadataDir));
    }

    /**
     * Constructs with metadata directory Path object
     * @param metadataDir
     */
    public MetaStorage(Path metadataDir) {
        this.metadataDir = metadataDir;
        if(!metadataDir.toFile().isDirectory()) {
            throw new IllegalArgumentException("Metadata path must be directory!!!");
        }
        this.metadataListeners = new ArrayList<>();
        load();
    }

    /**
     * Load metadata files
     */
    public void load() {
        this.metadataMap = Stream.of(this.metadataDir.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String ext = pathname.getName().substring(pathname.getName().lastIndexOf(".")+1);
                return pathname.isFile() 
                    &&
                    ( ext.equalsIgnoreCase(META_EXT.CONF.name())
                    || ext.equalsIgnoreCase(META_EXT.CONFIG.name())
                    || ext.equalsIgnoreCase(META_EXT.JSON.name())
                    || ext.equalsIgnoreCase(META_EXT.PROPERTIES.name())
                    || ext.equalsIgnoreCase(META_EXT.YML.name())
                    || ext.equalsIgnoreCase(META_EXT.YAML.name())
                    );
            }            
        })).map( f -> new Object[]{f.toPath(), new Metadata(f)} ).collect(Collectors.toMap(k -> (Path)k[0], v -> (Metadata)v[1]));
    }

    /**
     * Get metadata value
     * @param <V>
     * @param filename
     * @param expr
     * @return
     */
    public <V> V getValue(String filename, String expr) {
        return getMetadata(filename).getValue(expr);
    }

    /**
     * Get metadata value
     * @param <V>
     * @param metaFile
     * @param expr
     * @return
     */
    public <V> V getValue(Path metaFile, String expr) {
        return getMetadata(metaFile).getValue(expr);
    }

    /**
     * Get metadata 
     * @param filename
     * @return
     */
    public Metadata getMetadata(String filename) {
        return getMetadata(this.metadataDir.resolve(filename));
    }

    /**
     * Get metadata
     * @param metaFile
     * @return
     */
    public Metadata getMetadata(Path metaFile) {
        return this.metadataMap.get(metaFile);
    }

    /**
     * Add metadata listener
     * @param listener
     */
    public void addMetaListener(MetaListener listener) {
        this.metadataListeners.add(listener);
    }        

    /**
     * Remove metadata listener
     * @param listener
     */
    public void removeListener(MetaListener listener) {
        this.metadataListeners.remove(listener);
    }
}
