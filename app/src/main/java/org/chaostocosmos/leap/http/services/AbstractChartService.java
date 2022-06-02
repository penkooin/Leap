package org.chaostocosmos.leap.http.services;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphConstants.GRAPH;
import org.chaostocosmos.chaosgraph.GraphElement;
import org.chaostocosmos.chaosgraph.GraphElements;
import org.chaostocosmos.chaosgraph.GraphUtility;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.chaosgraph.INTERPOLATE;
import org.chaostocosmos.chaosgraph.awt2d.AreaGraph;
import org.chaostocosmos.chaosgraph.awt2d.BarGraph;
import org.chaostocosmos.chaosgraph.awt2d.CircleGraph;
import org.chaostocosmos.chaosgraph.awt2d.LineGraph;
import org.chaostocosmos.leap.http.commons.DataStructureOpr;
import org.chaostocosmos.leap.http.services.model.ChartModel;

public abstract class AbstractChartService extends AbstractService implements ChartModel {

    public AbstractChartService() {
    }
    
    @Override
    public Graph lineChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph areaChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph barChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph circleChart(Map<String, Object> graphAttributes) throws Exception {
        return createGraph(graphAttributes);
    }

    @Override
    public Graph createGraph(Map<String, Object> graphAttributes) throws Exception {
        GRAPH graph = GRAPH.valueOf(graphAttributes.get("GRAPH")+"");
        final String interpolate = DataStructureOpr.<String>getValue(graphAttributes, "INTERPOLATE");
        final int width = DataStructureOpr.<Integer>getValue(graphAttributes, "WIDTH");
        final int height = DataStructureOpr.<Integer>getValue(graphAttributes, "HEIGHT");
        List<Object> xIndex = DataStructureOpr.<List<Object>>getValue(graphAttributes, "XINDEX");
        List<Double> yIndex = DataStructureOpr.<List<Double>>getValue(graphAttributes,"YINDEX");
        List<GraphElement> elementList = DataStructureOpr.<List<Map<String, Object>>>getValue(graphAttributes, "ELEMENTS").stream().map(a -> {
            String name = a.get("ELEMENT")+"";
            String label = a.get("LABEL")+"";
            List<Integer> colors = (List<Integer>)a.get("COLOR");
            Color color = new Color(colors.get(0), colors.get(1), colors.get(2));
            List<Double> values = (List<Double>)a.get("VALUES");
            return new GraphElement(name, color, label, color, values, INTERPOLATE.valueOf(interpolate));
        }).collect(Collectors.toList());
        GraphElements elements = new GraphElements(graph, elementList, xIndex, yIndex);
        return createGraph(graph, width, height, xIndex, yIndex, elementList);
    }

    @Override
    public Graph createGraph(GRAPH graphType, int width, int height, List<Object> xIndex, List<Double> yIndex, List<GraphElement> elements) throws Exception {
        GraphElements graphElements = new GraphElements(graphType, elements, xIndex, yIndex);
        switch(graphType.name()) {
            case "LINE":
                return new LineGraph(graphElements, width, height);
            case "AREA":
                return new AreaGraph(graphElements, width, height);
            case "BAR":
                return new BarGraph(graphElements, width, height);
            case "CIRCLE":
                return new CircleGraph(graphElements, width, height);
            default:
                throw new IllegalArgumentException("Not exist graph type: "+graphType.name());
        }
    }

    @Override
    public void saveImage(BufferedImage image, Path savePath, CODEC codec) throws Exception {
        GraphUtility.saveBufferedImage(image, savePath.toFile(), codec);
    }
}
