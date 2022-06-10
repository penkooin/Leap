package org.chaostocosmos.leap.http.services.model;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphElement;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;

/**
 * Chart Mode interface
 * 
 * @author 9ins
 */
public interface ChartModel {
    /**
     * Create line chart
     * @return
     */
    public Graph lineChart(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create area chart
     * @return
     */
    public Graph areaChart(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create bar chart
     * @return
     */
    public Graph barChart(Map<String, Object> graphAttributes) throws Exception; 
    /**
     * Create PI chart
     */
    public Graph circleChart(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create chart with chart attributes Map
     * @param graphAttributes
     * @return
     * @throws Exception
     */
    public Graph createGraph(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create Graph object
     * @param graphType
     * @param title
     * @param width
     * @param height
     * @param xIndex
     * @param yIndex
     * @param elements
     * @return
     * @throws Exception
     */
    public Map<Object, GraphElement> createGraphElements(List<Object> elements) throws Exception;
    /**
     * Save image
     * @param image
     */
    public void saveImage(BufferedImage image, Path savePath, CODEC code) throws Exception;    
}
