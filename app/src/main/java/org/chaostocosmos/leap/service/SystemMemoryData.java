package org.chaostocosmos.leap.service;

/**
 * System memory data object
 * 
 * @author Kooin-Shin
 */
public class SystemMemoryData {

    /**
     * Labels array
     */
    private String[] labels;

    /**
     * Usage memory array
     */
    private double[] usage;

    /**
     * Free memory array
     */
    private double[] free;
    
    /**
     * Get labels
     * @return
     */
    public String[] getLabels() {
        return labels;
    }

    /**
     * Set labels
     * @param labels
     */
    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    /**
     * Get memory usages
     * @return
     */
    public double[] getUsage() {
        return usage;
    }

    /**
     * Set memory usages
     * @param usage
     */
    public void setUsage(double[] usage) {
        this.usage = usage;
    }

    /**
     * Get memory free
     * @return
     */
    public double[] getFree() {
        return this.free;
    }

    /**
     * Set memory free
     * @param free
     */
    public void setFree(double[] free) {
        this.free = free;
    }
}
