package org.chaostocosmos.leap.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphConstants;
import org.chaostocosmos.chaosgraph.GraphConstants.GRID;
import org.chaostocosmos.chaosgraph.GraphElement;
import org.chaostocosmos.chaosgraph.GraphElements;
import org.chaostocosmos.chaosgraph.GraphUtility;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.chaosgraph.INTERPOLATE;
import org.chaostocosmos.chaosgraph.awt2d.AreaGraph;
import org.chaostocosmos.chaosgraph.awt2d.BarGraph;
import org.chaostocosmos.chaosgraph.awt2d.BarRatioGraph;
import org.chaostocosmos.chaosgraph.awt2d.CircleGraph;
import org.chaostocosmos.chaosgraph.awt2d.LineGraph;
import org.chaostocosmos.leap.common.DataStructureOpr;
import org.chaostocosmos.leap.service.model.ChartModel;

/**
 * AbstractChartService
 * 
 * Abstraction class for Chart service.
 * In case of monitoring of Leap, Using this abstraction for sample monitoring dashboard.
 * 
 * @author 9ins
 */
public abstract class AbstractChartService extends AbstractService implements ChartModel {
    /**
     * Chart Map
     */
    Map<String, Graph<Double, String, Double>> graphMap;

    /**
     * Default Constructor
     */
    public AbstractChartService() {
        this.graphMap = new HashMap<String, Graph<Double, String, Double>>();
    }
    
    @Override
    public Graph<Double, String, Double> lineChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph<Double, String, Double> areaChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph<Double, String, Double> barChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph<Double, String, Double> circleChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    /**
     * Create graph object with given map object
     * @param map
     * @return
     * @throws Exception 
     */
    @Override
    @SuppressWarnings("unchecked")
    public Graph<Double, String, Double> createGraph(Map<String, Object> map) throws Exception {
        String id = (String) map.get("id");
        Graph<Double, String, Double> graph = graphMap.get(id);
        //double limit = (double)map.get("limit");
        String unit = map.get("unit")+"";
        if(DataStructureOpr.<List<Double>>getValue(map, "elements.0.values").size() < 3) {
            return null;                        
        }
        GraphConstants.GRAPH type = GraphConstants.GRAPH.valueOf(map.get("graph")+"");            
        String title = map.get("title")+"";
        int width = (int)Double.parseDouble(map.get("width")+"");
        int height = (int)Double.parseDouble(map.get("height")+"");
        List<String> xIndex = (List<String>)map.get("x-index");  
        List<Double> yIndex = (List<Double>)map.get("y-index");  
        GraphElements<Double, String, Double> graphElements = null;
        if(graph == null) {
            graphElements = new GraphElements<Double, String, Double>(type, xIndex, yIndex);            
            graphElements.setGraphElementMap(createGraphElements((List<Object>)map.get("elements")));    	
            Color legendColor = getColor(((List<?>)map.get("legend-color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
            Color imgBgColor = getColor(((List<?>)map.get("bg-color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
            Color graphBgColor = getColor(((List<?>)map.get("graph-bg-color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
            switch(type) {
                case LINE :
                    LineGraph<Double, String, Double> line = new LineGraph<>(graphElements, title, width, height);
                    line.setLabelBgColor(legendColor);
                    line.setImgBgColor(imgBgColor);
                    line.setGraphBgColor(graphBgColor);
                    graph = line;
                    break;
                case AREA :
                    AreaGraph<Double, String, Double> area = new AreaGraph<>(graphElements, title, width, height);
                    area.setLabelBgColor(legendColor);
                    area.setImgBgColor(imgBgColor);
                    area.setGraphBgColor(graphBgColor);
                    graph = area;
                    break;
                case CIRCLE :
                    CircleGraph<Double, String, Double> pi = new CircleGraph<>(graphElements, title, width, height);
                    pi.setLabelBgColor(legendColor);
                    pi.setImgBgColor(imgBgColor);
                    pi.setGraphBgColor(graphBgColor);
                    graph = pi;
                    break;
                case BAR : 
                    BarGraph<Double, String, Double> bar = new BarGraph<>(graphElements, title, width, height);
                    bar.setLabelBgColor(legendColor);
                    bar.setImgBgColor(imgBgColor);
                    bar.setGraphBgColor(graphBgColor);
                    graph = bar;
                    break;
                case BAR_RATIO :
                    BarRatioGraph<Double, String, Double> barRatio = new BarRatioGraph<Double, String, Double>(graphElements, title, width, height);
                    barRatio.setLabelBgColor(legendColor);
                    barRatio.setImgBgColor(imgBgColor);
                    barRatio.setGraphBgColor(graphBgColor);
                    graph = barRatio;
                    break;
            }
            graph.setShowGraphXY(false);
            graph.setInterpolateType(INTERPOLATE.valueOf(map.get("interpolate")+""));
            graph.setGridStyle(GRID.DOT);
            graph.setGraphBorderSize(2f);
            graphMap.put(id, graph);
        } else {
            graphElements = graph.getGraphElements();
            graphElements.setXIndex(xIndex);
            graphElements.setYIndex(yIndex);
            graphElements.setGraphElementMap(createGraphElements((List<Object>)map.get("elements")));
        }       
        graph.setLimit(graphElements.getMaximum() * 1.3);        
        graph.setUnit(unit);        
        graph.setGraphAlpha(Float.valueOf(map.get("alpha")+""));
        graph.setTitleFontAlpha(0.3f);                    
        return graph;
    }       

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, GraphElement<Double, String, Double>> createGraphElements(List<Object> elements) throws Exception {
        return elements.stream().map(o -> (Map<String, Object>)o).map(m -> {
            String elementName = m.get("element")+"";
            //String label = m.get("label")+"";
            Color elementColor = getColor(((List<?>)m.get("color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
            List<Double> valueList = (List<Double>)m.get("values");
            return new GraphElement<Double, String, Double>(elementName, elementColor, elementName, elementColor, valueList);
        }).filter(el -> el != null).collect(Collectors.toMap(k -> k.getElementName(), v -> v)); 
    }

    @Override
    public void saveImage(BufferedImage image, Path savePath, CODEC codec) throws Exception {       
        GraphUtility.saveBufferedImage(image, savePath.toFile(), CODEC.PNG);
    }

    /**
     * Get Color from rgb int list
     */
    public Color getColor(List<Integer> rgb) {
        return new Color(rgb.get(0), rgb.get(1), rgb.get(2));
    }
}
