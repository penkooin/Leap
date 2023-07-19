package org.chaostocosmos.leap.service;

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
import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.resource.TemplateBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;

/**
 * Leap monitoring service
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "")
public class SystemMonitorService extends AbstractChartService {

    /**
     * Gson object
     */
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @MethodMapper(method = REQUEST.GET, mappingPath = "/monitor")
    public void getMonitorWebpage(HttpRequest request, HttpResponse response) throws Exception {
        if(Context.get().server().<Boolean> isSupportMonitoring()) {
            String body = TemplateBuilder.buildMonitoringPage(request.getContextPath(), super.httpTransfer.getHost());
            response.setBody(body);
            response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType());
            response.setResponseCode(HTTP.RES200.code());
        } else {
            throw new LeapException(HTTP.RES503, "Monitoring page not available now. Currently monitoring option is off !!!");
        }
    } 

    @MethodMapper(method = REQUEST.GET, mappingPath = "/script/refreshImage.js")
    public void getRefreshImageScript(HttpRequest request, HttpResponse response) throws IOException {
        Host<?> host = super.getHost();
        String url = host.<String> getProtocol().toLowerCase()+"://"+host.getHost()+":"+host.getPort();
        String script = host.getResource().getTemplatePage("/script/refreshImage.js", Map.of("@interval", Context.get().server().getMonitoringInterval(), "@url", url));
        response.setBody(script);
        response.addHeader("Content-Type", MIME.TEXT_JAVASCRIPT.mimeType());
        response.setResponseCode(HTTP.RES200.code());
    }

    @MethodMapper(method = REQUEST.POST, mappingPath = "/monitor/chart/image")
    @SuppressWarnings("unchecked")
    public void getResources(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, Object> header = request.getReqHeader();
        String charset = header.get("Charset") != null ? header.get("Charset").toString() : super.getHost().charset();
        if(charset == null || charset.equals("")) {
            throw new LeapException(HTTP.RES404, "Request has no charset field in header.");
        }
        if(request.getContentType() != MIME.MULTIPART_FORM_DATA) {
            throw new LeapException(HTTP.RES415, "Requested content type is not allowed on this service.");
        }
        MultiPart multiPart = (MultiPart)request.getBodyPart();
        byte[] body = multiPart.getBody().get("chart");
        if(body == null) {
            throw new LeapException(HTTP.RES404, "Request has no body data. Chart service must have JSON chart data.");
        }
        String chartJson = new String(body, charset);
        //super.logger.debug(chartJson);
        Map<String, Object> jsonMap = gson.fromJson(chartJson, Map.class);        
        List<Map<String, Object>> chartMap = jsonMap.values().stream().map(m -> (Map<String, Object>) m ).collect(Collectors.toList());
        for(Host<?> host : Context.get().hosts().getAllHost()) {
            for(Map<String, Object> map : chartMap) {
                String savePath = (String) map.get("save-path"); 
                boolean inMemory = (boolean) map.get("in-memory"); 
                Graph<Double, String, Double> chart = super.createGraph(map);                
                if(chart == null) continue;
                chart.setLeftIndent(70);
                chart.setRightIndent(30);
                if(!inMemory) {
                    saveBufferedImage(chart.getBufferedImage(), host.getWebInf().resolve(savePath).toFile(), CODEC.PNG);
                    //super.logger.debug("[MONITOR] Chart image save to file: "+host.getStatic().resolve(savePath).toString());
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(chart.getBufferedImage(), CODEC.PNG.name(), out);
                    host.getResource().addResource(host.getWebInf().resolve(savePath), out.toByteArray(), true);
                    super.logger.debug("[MONITOR] Chart image add to In-Memory resource.");
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
    public void saveBufferedImage(BufferedImage image, File saveFile, CODEC codec) throws IOException, NotSuppotedEncodingFormatException {
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
    public Exception errorHandling(HttpResponse response, Exception throwable) {
        throwable.printStackTrace();
        return throwable;
    }
}
