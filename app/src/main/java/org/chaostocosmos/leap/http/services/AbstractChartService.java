package org.chaostocosmos.leap.http.services;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
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
import org.chaostocosmos.leap.http.commons.DataStructureOpr;
import org.chaostocosmos.leap.http.services.model.ChartModel;

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
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    @Override
    @SuppressWarnings("unchecked")
    public Graph<Double, String, Double> createGraph(Map<String, Object> map) throws Exception {
        String id = (String) map.get("id");
        Graph<Double, String, Double> graph = graphMap.get(id);
        double limit = (double)map.get("limit");
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
            switch(type) {
                case LINE :
                    graph = new LineGraph<Double, String, Double>(graphElements, title, width, height);                    
                    break;
                case AREA :
                    graph = new AreaGraph<Double, String, Double>(graphElements, title, width, height);
                    break;
                case CIRCLE :
                    graph = new CircleGraph<Double, String, Double>(graphElements, title, width, height);
                    break;
                case BAR : 
                    graph = new BarGraph<Double, String, Double>(graphElements, title, width, height);
                    break;
                case BAR_RATIO :
                    graph = new BarRatioGraph<Double, String, Double>(graphElements, title, width, height);
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
            String label = m.get("label")+"";
            List<Integer> colorList = ((List<?>)m.get("color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList());
            Color elementColor = new Color((int)colorList.get(0), (int)colorList.get(1), (int)colorList.get(2));
            List<Double> valueList = (List<Double>)m.get("values");
            return new GraphElement<Double, String, Double>(elementName, elementColor, elementName, elementColor, valueList);
        }).filter(el -> el != null).collect(Collectors.toMap(k -> k.getElementName(), v -> v)); 
    }

    @Override
    public synchronized void saveImage(BufferedImage image, Path savePath, CODEC codec) throws Exception {       
        GraphUtility.saveBufferedImage(image, savePath.toFile(), CODEC.PNG);
    }
}
