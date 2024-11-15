package org.chaostocosmos.leap.context.utils;

/**
 * Context diffrence object
 * 
 * @author 9ins
 */
public class Diff {

    /**
     * Meta context path
     */
    private String path;

    /**
     * Original value
     */
    private Object originalValue;

    /**
     * Changed value
     */
    private Object modifiedValue;

    /**
     * Constructor
     * @param path
     * @param originalValue
     * @param modifiedValue
     */
    public Diff(String path, Object originalValue, Object modifiedValue) {
        this.path = path;
        this.originalValue = originalValue;
        this.modifiedValue = modifiedValue;
    }

    /**
     * Get meta path
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Get original value
     * @return
     */
    public Object getOriginalValue() {
        return originalValue;
    }

    /**
     * Get modified value
     * @return
     */
    public Object getModifiedValue() {
        return modifiedValue;
    }

    @Override
    public String toString() {
        return "Path: " + path + "\n" +
               "Original Value: " + originalValue + "\n" +
               "Modified Value: " + modifiedValue + "\n";
    }
}
