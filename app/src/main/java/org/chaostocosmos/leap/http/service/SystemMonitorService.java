package org.chaostocosmos.leap.http.service;

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.chaosgraph.NotSuppotedEncodingFormatException;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.annotation.MethodMapper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.resource.TemplateBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;

@ServiceMapper(path = "/monitor")
public class SystemMonitorService extends AbstractChartService {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @MethodMapper(mappingMethod = REQUEST_TYPE.GET, path = "")
    public void getMonitorWebpage(Request request, Response response) throws Exception {
        if(Context.getServer().<Boolean> isSupportMonitoring()) {
            String body = TemplateBuilder.buildMonitoringPage(request.getContextPath(), super.httpTransfer.getHost());
            response.setBody(body);
            response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.mimeType());
            response.setResponseCode(RES_CODE.RES200.code());
        } else {
            throw new HTTPException(RES_CODE.RES503, "Monitoring page not available now. Currently monitoring option is off !!!");
        }
    } 

    @MethodMapper(mappingMethod = REQUEST_TYPE.POST, path = "/chart/image")
    @SuppressWarnings("unchecked")
    public void getResources(Request request, Response response) throws Exception {
        Map<String, String> header = request.getReqHeader();
        String charset = header.get("charset");
        if(charset == null || charset.equals("")) {
            throw new HTTPException(RES_CODE.RES404, "Request has no charset field in header.");
        }
        if(request.getContentType() != MIME_TYPE.MULTIPART_FORM_DATA) {
            throw new HTTPException(RES_CODE.RES415, "Requested content type is not allowed on this service.");
        }
        MultiPart multiPart = (MultiPart)request.getBodyPart();
        byte[] body = multiPart.getBody().get("chart");
        if(body == null) {
            throw new HTTPException(RES_CODE.RES404, "Request has no body data. Chart service must have JSON chart data.");
        }
        String chartJson = new String(body, charset);
        //super.logger.debug(chartJson);
        Map<String, Object> jsonMap = gson.fromJson(chartJson, Map.class);
        List<Map<String, Object>> chartMap = jsonMap.values().stream().map(m -> (Map<String, Object>) m ).collect(Collectors.toList());
        for(Host<?> host : Context.getHosts().getAllHost()) {
            for(Map<String, Object> map : chartMap) {
                String savePath = (String) map.get("save-path"); 
                boolean inMemory = (boolean) map.get("in-memory"); 
                Graph<Double, String, Double> chart = super.createGraph(map);
                if(chart == null) continue;
                if(!inMemory) {
                    saveBufferedImage(chart.getBufferedImage(), host.getStatic().resolve(savePath).toFile(), CODEC.PNG);
                    //super.logger.debug("[MONITOR] Chart image save to file: "+host.getStatic().resolve(savePath).toString());
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(chart.getBufferedImage(), CODEC.PNG.name(), out);
                    host.getResource().addResource(host.getStatic().resolve(savePath), out.toByteArray(), true);
                    //super.logger.debug("[MONITOR] Chart image add to In-Memory resource.");
                }
            }
        }
    }

    /**
     * Save monitoring image to each host directory
     * @param image
     * @param saveFile
     * @param codec
     * @throws IOException
     * @throws NotSuppotedEncodingFormatException
     */
    public synchronized void saveBufferedImage(BufferedImage image, File saveFile, CODEC codec) throws IOException, NotSuppotedEncodingFormatException {
    	String ext = saveFile.getName().substring(saveFile.getName().lastIndexOf(".") + 1);
    	if(!Stream.of(CODEC.values()).anyMatch(c -> c.name().equalsIgnoreCase(ext))) {
    		throw new NotSuppotedEncodingFormatException("Given file extention isn't exist in supported codec list.");
    	}
        ParameterBlock pb = new ParameterBlock();
        pb.add(image);
        PlanarImage tPlanarImage = (PlanarImage)JAI.create("awtImage", pb );
        try(FileOutputStream out = new FileOutputStream(saveFile)) {
            ImageEncoder tEncoder = ImageCodec.createImageEncoder(codec.name(), out,  null); 
            tEncoder.encode(tPlanarImage); 
        }
    }

    @Override
    public Exception errorHandling(Response response, Exception throwable) {
        throwable.printStackTrace();
        return throwable;
    }
}
