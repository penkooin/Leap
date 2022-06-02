package org.chaostocosmos.leap.http.services;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import org.chaostocosmos.chaosgraph.GraphUtility.CODEC;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

@ServiceMapper(path = "/monitor")
public class ResourceUsageService extends AbstractChartService {

    Gson gson = new Gson();

    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path = "/chart/image")
    @SuppressWarnings("unchecked")
    public void getResources(Request request, Response response) throws Exception {
        Map<String, String> header = request.getReqHeader();
        String charset = header.get("charset");
        if(charset == null || charset.equals("")) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Request has no charset field in header.");
        }
        byte[] body = request.getReqBody();
        if(body == null) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Request has no body data. Chart service must have JSON chart data.");
        }
        String chartJson = new String(body, charset);
        Map<String, Object> chartMap = (Map<String, Object>)gson.fromJson(chartJson, HashMap.class);

        Map<String, Object> cpuMap = (Map<String, Object>)chartMap.get("CPU");
        super.saveImage(super.lineChart(cpuMap).getBufferedImage(), Paths.get("SAVE_PATH"), CODEC.valueOf(cpuMap.get("CODEC")+""));
    
        Map<String, Object> memoryMap = (Map<String, Object>)chartMap.get("MEMORY");
        super.saveImage(super.areaChart(memoryMap).getBufferedImage(), Paths.get("SAVE_PATH"), CODEC.valueOf(cpuMap.get("CODEC")+""));
        
        Map<String, Object> threadMap = (Map<String, Object>)chartMap.get("THREAD");        
        super.saveImage(super.areaChart(threadMap).getBufferedImage(), Paths.get("SAVE_PATH"), CODEC.valueOf(cpuMap.get("CODEC")+""));
    }

    @Override
    public Throwable errorHandling(Response response, Throwable throwable) throws Throwable {
        throwable.printStackTrace();
        return throwable;
    }
}
