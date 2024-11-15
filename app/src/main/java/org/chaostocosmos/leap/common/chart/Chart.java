package org.chaostocosmos.leap.common.chart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.awt.Color;
import java.awt.image.BufferedImage;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphConstants;
import org.chaostocosmos.chaosgraph.GraphConstants.GRAPH;
import org.chaostocosmos.chaosgraph.GraphConstants.GRID;
import org.chaostocosmos.chaosgraph.GraphElement;
import org.chaostocosmos.chaosgraph.GraphElements;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.chaosgraph.INTERPOLATE;
import org.chaostocosmos.chaosgraph.awt2d.AreaGraph;
import org.chaostocosmos.chaosgraph.awt2d.BarGraph;
import org.chaostocosmos.chaosgraph.awt2d.BarRatioGraph;
import org.chaostocosmos.chaosgraph.awt2d.CircleGraph;
import org.chaostocosmos.chaosgraph.awt2d.LineGraph;
import org.chaostocosmos.leap.common.data.DataStructureOpr;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.common.utils.ImageUtils;

/**
 * Chart object
 * 
 * @author 9ins
 */
public class Chart {

    /**
     * Chart Map
     */
    Map<String, Object> chartMap;

    /**
     * Graph type
     */
    GRAPH graphType;

    /**
     * Graph object
     */
    Graph<Double, String, Double> graph;

    /**
     * Save Path
     */
    Path savePath;

    /**
     * Constructs with chart Map and save Path
     * @param chartMap
     */
    public Chart(Map<String, Object> chartMap, Path savePath) {
        this.graph = createGraph(chartMap);
        this.graphType = this.graph.getGraphType();
        this.savePath = savePath;
    }

    /**
     * Create Graph object
     * @param map
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Graph<Double, String, Double> createGraph(Map<String, Object> map) {
        String id = (String) map.get("id");
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
        graphElements = new GraphElements<Double, String, Double>(type, xIndex, yIndex);            
        graphElements.setGraphElementMap(createGraphElements((List<Object>)map.get("elements")));    	
        Color legendColor = getColor(((List<?>)map.get("legend-color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
        Color imgBgColor = getColor(((List<?>)map.get("bg-color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
        Color graphBgColor = getColor(((List<?>)map.get("graph-bg-color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
        Graph<Double, String, Double> graph = null;
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
        if(graph != null) {
            graph.setShowGraphXY(false);
            graph.setInterpolateType(INTERPOLATE.valueOf(map.get("interpolate")+""));
            graph.setGridStyle(GRID.DOT);
            graph.setGraphBorderSize(2f);
        } else {
            throw new RuntimeException("Graph object is null!!!");
        }       
        graph.setLeftIndent(70);
        graph.setRightIndent(30);
        graph.setLimit(graphElements.getMaximum() * 1.3);        
        graph.setUnit(unit);        
        graph.setGraphAlpha(Float.valueOf(map.get("alpha")+""));
        graph.setTitleFontAlpha(0.3f);                    
        return graph;
    }       

    /**
     * Create GraphElement object
     * @param elements
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Map<Object, GraphElement<Double, String, Double>> createGraphElements(List<Object> elements) {
        return elements.stream().map(o -> (Map<String, Object>)o).map(m -> {
            String elementName = m.get("element")+"";
            //String label = m.get("label")+"";
            Color elementColor = getColor(((List<?>)m.get("color")).stream().map(v -> Double.valueOf(v+"").intValue()).collect(Collectors.toList()));
            List<Double> valueList = (List<Double>)m.get("values");
            return new GraphElement<Double, String, Double>(elementName, elementColor, elementName, elementColor, valueList);
        }).filter(el -> el != null).collect(Collectors.toMap(k -> k.getElementName(), v -> v)); 
    }

    /**
     * Get graph object
     * @return
     */
    public Graph<Double, String, Double> getGraph() {
        return this.graph;
    }

    /**
     * Get graph as BufferedImage object
     * @return
     */
    public BufferedImage getGraphBufferedImage() {
        return this.graph.getBufferedImage();
    }

    /**
     * Get bytes of Graph 
     * @return
     * @throws ImageWriteException
     * @throws IOException
     */
    public byte[] getGraphBytes() throws ImageWriteException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Imaging.writeImage(this.graph.getBufferedImage(), out, ImageFormats.PNG, null);
        LoggerFactory.getLogger().debug("[MONITOR] Chart image is packed to bytes.");        
        return out.toByteArray();
    }

    /**
     * Save chart 
     * @throws ImageWriteException
     * @throws IOException
     */
    public void save() throws ImageWriteException, IOException {
        save(this.savePath);
    }

    /**
     * Save chart to Path
     * @param savePath
     * @throws ImageWriteException
     * @throws IOException
     */
    public void save(Path savePath) throws ImageWriteException, IOException {
        ImageUtils.saveBufferedImage(this.graph.getBufferedImage(), savePath.toFile(), ImageFormats.PNG, null);
        LoggerFactory.getLogger().debug("[MONITOR] Chart image save to file: "+savePath.toAbsolutePath().toString());
    }

    /**
     * Save BufferedImage object to Path
     * @param image
     * @param savePath
     * @param codec
     * @throws IOException 
     * @throws ImageWriteException 
     */
    public void saveImage(BufferedImage image, Path savePath, CODEC codec) throws ImageWriteException, IOException {       
        ImageUtils.saveBufferedImage(image, savePath.toFile(), ImageFormats.valueOf(codec.name()) , null);
    }

    /**
     * Get Color from rgb int list
     * @param rgb
     * @return
     */
    public Color getColor(List<Integer> rgb) {
        return new Color(rgb.get(0), rgb.get(1), rgb.get(2));
    }        
}
