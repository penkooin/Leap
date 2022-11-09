package org.chaostocosmos.metadata;

import java.io.File;
import java.nio.file.Path;
import java.util.EventObject;

/**
 * MetadataEvent
 * 
 * @author 9ins
 */
public class MetaEvent <T> extends EventObject {

    /**
     * Metadata file Path
     */
    Path metadataFile;

    /**
     * Metadata expression
     */
    String expr;

    /**
     * Metadata value
     */
    T metadataValue;

    /**
     * Constructs with event source object, metadata file path, expression, metadata value
     * @param eventSource
     * @param metadataFile
     * @param expr
     * @param metadataValue
     */
    public MetaEvent(Object eventSource, Path metadataFile, String expr, T metadataValue) {
        super(eventSource);
        this.expr = expr;
        this.metadataFile = metadataFile;
        this.metadataValue = metadataValue;        
    }

    /**
     * Get metadata File
     * @return
     */
    public File getMetaFile() {
        return this.metadataFile.toFile();
    }

    /**
     * Get metadata expression
     * @return
     */
    public String getExpression() {
        return this.expr;
    }

    /**
     * Get metadata value
     * @return
     */
    public T getMetaValue() {
        return this.metadataValue;
    }    

    @Override
    public String toString() {
        return "{" +
            " metadataFile='" + metadataFile + "'" +
            ", expr='" + expr + "'" +
            ", metadataValue='" + metadataValue + "'" +
            "}";
    }
}
