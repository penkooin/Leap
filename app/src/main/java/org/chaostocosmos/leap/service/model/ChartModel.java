package org.chaostocosmos.leap.service.model;

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
    public Graph<Double, String, Double> lineChart(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create area chart
     * @return
     */
    public Graph<Double, String, Double> areaChart(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create bar chart
     * @return
     */
    public Graph<Double, String, Double> barChart(Map<String, Object> graphAttributes) throws Exception; 
    /**
     * Create PI chart
     */
    public Graph<Double, String, Double> circleChart(Map<String, Object> graphAttributes) throws Exception;
    /**
     * Create chart with chart attributes Map
     * @param graphAttributes
     * @return
     * @throws Exception
     */
    public Graph<Double, String, Double> createGraph(Map<String, Object> graphAttributes) throws Exception;
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
    public Map<Object, GraphElement<Double, String, Double>> createGraphElements(List<Object> elements) throws Exception;
    /**
     * Save image
     * @param image
     */
    public void saveImage(BufferedImage image, Path savePath, CODEC code) throws Exception;    
}
