package org.chaostocosmos.leap.common.chart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;

/**
 * Chart object
 * 
 * @author 9ins
 */
public class Charts {

    /**
     * Graphs Map
     */
    Map<String, Chart> graphsMap;

    /**
     * Constructs with Chart Map
     * @param chartsMap
     */
    public Charts(Map<String, Map<String, Object>> chartsMap) {
        this.graphsMap = chartsMap.entrySet().stream().map(e -> {
                                    String chartId = e.getKey();
                                    Map<String, Object> chartMap = e.getValue();
                                    Path savePath = Paths.get(chartMap.get("save-path")+"");
                                    Chart chart = new Chart(e.getValue(), savePath);
                                    return new Object[] { chartId, chart };
                                }).collect(Collectors.toMap(k -> (String) k[0], v -> (Chart)v[1]));
    }

    /**
     * Get Chart object
     * @param chartId
     * @return
     */
    public Chart getChart(String chartId) {
        if(this.graphsMap.containsKey(chartId)) {
            return this.graphsMap.get(chartId);
        }
        throw new RuntimeException("Specified chart ID is not found in "+this.graphsMap.keySet().stream().collect(Collectors.joining(", ")));
    }

    /**
     * Get chart BufferedImage object
     * @param chartId
     * @return
     */
    public BufferedImage getBufferedImage(String chartId) {
        return getChart(chartId).getGraphBufferedImage();
    }

    /**
     * Get chart byte[]
     * @param chartId
     * @return
     * @throws ImageWriteException
     * @throws IOException
     */
    public byte[] getChartBytes(String chartId) throws ImageWriteException, IOException {
        return getChart(chartId).getGraphBytes();
    }

}
