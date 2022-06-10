package org.chaostocosmos.leap.http.services;

import java.util.Map;

import org.chaostocosmos.chaosgraph.Graph;
import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.DataStructureOpr;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.part.MultiPart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

        if(cpuChart != null) super.saveImage(cpuChart.getBufferedImage(), Context.getHomePath().resolve(cpuPath), CODEC.PNG);    
        if(memoryChart != null) super.saveImage(memoryChart.getBufferedImage(), Context.getHomePath().resolve(memoryPath), CODEC.PNG);
        if(threadChart != null) super.saveImage(threadChart.getBufferedImage(), Context.getHomePath().resolve(threadPath), CODEC.PNG);
    }

    @Override
    public Throwable errorHandling(Response response, Throwable throwable) throws Throwable {
        throwable.printStackTrace();
        return throwable;
    }
}
