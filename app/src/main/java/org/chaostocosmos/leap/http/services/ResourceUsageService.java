package org.chaostocosmos.leap.http.services;


import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Stream;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.chaosgraph.NotSuppotedEncodingFormatException;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.DataStructureOpr;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.part.MultiPart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;

@ServiceMapper(path = "/monitor")
public class ResourceUsageService extends AbstractChartService {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path = "/chart/image")
    @SuppressWarnings("unchecked")
    public void getResources(Request request, Response response) throws Exception {
        Map<String, String> header = request.getReqHeader();
        String charset = header.get("charset");
        if(charset == null || charset.equals("")) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Request has no charset field in header.");
        }
        if(request.getContentType() != MIME_TYPE.MULTIPART_FORM_DATA) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES415.code(), "Requested content type is not allowed on this service.");
        }
        MultiPart multiPart = (MultiPart)request.getBodyPart();
        byte[] body = multiPart.getBody().get("CHART");
        if(body == null) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Request has no body data. Chart service must have JSON chart data.");
        }
        String chartJson = new String(body, charset);
        Map<String, Object> map = gson.fromJson(chartJson, Map.class);
        Graph cpuChart = super.lineChart((Map<String, Object>)map.get("CPU"));
        Graph memoryChart = super.areaChart((Map<String, Object>)map.get("MEMORY"));
        Graph threadChart = super.lineChart((Map<String, Object>)map.get("THREAD"));
        String cpuPath = DataStructureOpr.<String>getValue(map, "CPU.SAVE_PATH");
        String memoryPath = DataStructureOpr.<String>getValue(map, "MEMORY.SAVE_PATH");
        String threadPath = DataStructureOpr.<String>getValue(map, "THREAD.SAVE_PATH");

        if(cpuChart != null) saveBufferedImage(cpuChart.getBufferedImage(), super.serviceManager.getHost().getStatic().resolve(cpuPath).toFile(), CODEC.PNG);    
        if(memoryChart != null) saveBufferedImage(memoryChart.getBufferedImage(), super.serviceManager.getHost().getStatic().resolve(memoryPath).toFile(), CODEC.PNG);
        if(threadChart != null) saveBufferedImage(threadChart.getBufferedImage(), super.serviceManager.getHost().getStatic().resolve(threadPath).toFile(), CODEC.PNG);
    }

    /**
     * Save monitoring image to each host directory
     * @param image
     * @param saveFile
     * @param codec
     * @throws IOException
     * @throws NotSuppotedEncodingFormatException
     */
    public void saveBufferedImage(BufferedImage image, File saveFile, CODEC codec) throws IOException, NotSuppotedEncodingFormatException {
        Enumeration enu = ImageCodec.getCodecs();
    	String ext = saveFile.getName().substring(saveFile.getName().lastIndexOf(".")+1);
    	if(!Stream.of(CODEC.values()).anyMatch(c -> c.name().equalsIgnoreCase(ext))) {
    		throw new NotSuppotedEncodingFormatException("Given file extention isn't exist in supported codec list.");
    	}
        ParameterBlock pb = new ParameterBlock();
        pb.add(image);
        PlanarImage tPlanarImage = (PlanarImage)JAI.create("awtImage", pb );
        ImageCodec ic = ImageCodec.getCodec(codec.name());
        try(FileOutputStream out = new FileOutputStream(saveFile)) {
            ImageEncoder tEncoder = ic.createImageEncoder(codec.name(), out,  null);
            tEncoder.encode(tPlanarImage);    
        }
    }

    @Override
    public Throwable errorHandling(Response response, Throwable throwable) throws Throwable {
        throwable.printStackTrace();
        return throwable;
    }
}
