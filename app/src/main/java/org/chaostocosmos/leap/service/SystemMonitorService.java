package org.chaostocosmos.leap.service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.common.utils.ImageUtils;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.enums.TEMPLATE;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.service.abstraction.AbstractChartService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    /**
     * Get monitor page
     * @param request
     * @param response
     * @throws Exception
     */
    @MethodMapper(method = REQUEST.GET, mappingPath = "/monitor")
    public void getMonitorWebpage(HttpRequest request, HttpResponse response, Host<?> host) throws Exception {
        if(Context.get().server().isSupportMonitoring()) {
            String serverName = host.getId();
            String url = host.getProtocol().name().toLowerCase()+"://"+host.getHost()+":"+host.getPort();
            String body = super.httpTransfer.resolvePlaceHolder(TEMPLATE.MONITOR.loadTemplatePage(host.getId()), Map.of("@serverName", serverName, "@url", url));
            response.setBody(body);
            response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType());
            response.setResponseCode(HTTP.RES200.code());
        } else {
            throw new LeapException(HTTP.RES503, "Monitoring page not available now. Currently monitoring option is off !!!");
        }
    } 

    /**
     * Get resources
     * @param request
     * @param response
     * @throws Exception
     */
    @MethodMapper(method = REQUEST.POST, mappingPath = "/monitor/chart/image")
    @SuppressWarnings("unchecked")
    public void getResources(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, List<?>> header = request.getReqHeader();
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
        for(Host<?> host : Context.get().hosts().getHosts()) {
            for(Map<String, Object> map : chartMap) {
                String savePath = (String) map.get("save-path"); 
                boolean inMemory = (boolean) map.get("in-memory"); 
                Graph<Double, String, Double> chart = super.createGraph(map);                
                if(chart == null) {
                    continue;
                }                
                chart.setLeftIndent(70);
                chart.setRightIndent(30);
                Path homePath = Context.get().getHome();
                Path monitorImgPath = Context.get().server().getMonitoringImagePath();
                Path saveDir = homePath.resolve(monitorImgPath);
                if(!inMemory) {
                    ImageUtils.saveBufferedImage(chart.getBufferedImage(), saveDir.toFile(), ImageFormats.PNG, null);
                    //super.logger.debug("[MONITOR] Chart image save to file: "+host.getStatic().resolve(savePath).toString());
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Imaging.writeImage(chart.getBufferedImage(), out, ImageFormats.PNG, null);
                    host.getResource().addResource(host.getWebInf().resolve(savePath), out.toByteArray(), true);
                    super.logger.debug("[MONITOR] Chart image add to In-Memory resource.");
                }
            }
        }
    }

    /**
     * Get refresh image script
     * @param request
     * @param response
     * @throws Exception 
     */
    //@MethodMapper(method = REQUEST.GET, mappingPath = "/scripts/js/refreshImage.js")
    public void getRefreshImageScript(HttpRequest request, HttpResponse response, Host<?> host) throws Exception {
        String url = host.getProtocol().name().toLowerCase()+"://"+host.getHost()+":"+host.getPort();
        String script = host.getResource().getContextResource("/scripts/js/refreshImage.js").toString();        
        response.setBody(script);
        response.addHeader("Content-Type", MIME.TEXT_JAVASCRIPT.mimeType());
        response.setResponseCode(HTTP.RES200.code());
    }

    /**
     * Process error handling
     * @param response
     * @param throwable
     */
    @Override
    public Exception errorHandling(HttpResponse response, Exception throwable) {
        throwable.printStackTrace();
        return throwable;
    }
}
