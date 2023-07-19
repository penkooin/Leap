package org.chaostocosmos.leap.service;

public class SystemMemoryData {

    private String[] labels;
    private double[] usage;
    private double[] free;

    // Getters and setters

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public double[] getUsage() {
        return usage;
    }

    public void setUsage(double[] usage) {
        this.usage = usage;
    }


    public double[] getFree() {
        return this.free;
    }

    public void setFree(double[] free) {
        this.free = free;
    }

}
