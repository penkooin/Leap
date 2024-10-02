package org.chaostocosmos.leap.service;

import java.util.Date;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.service.abstraction.AbstractService;
import org.chaostocosmos.leap.spring.entity.SystemMemoryData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ServiceMapper(mappingPath = "/chatgpt")
public class SystemMemoryController extends AbstractService {

    @MethodMapper(mappingPath = "/memory", method = REQUEST.GET)
    public void getSystemMemoryData(HttpRequest request, HttpResponse response) throws JsonProcessingException {
        // Get the Runtime instance
        Runtime runtime = Runtime.getRuntime();

        // Get memory information
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        System.out.println("Total Memory: " + SIZE.GB.get(totalMemory));
        System.out.println("Used Memory: " + SIZE.GB.get(usedMemory));
        System.out.println("Free Memory: " + SIZE.GB.get(freeMemory));
        System.out.println("Max Memory: " + SIZE.GB.get(maxMemory));

        ObjectMapper om = new ObjectMapper();        
        SystemMemoryData memoryData = new SystemMemoryData();
        Date date = new Date();
        memoryData.setLabels(new String[] { date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()});
        memoryData.setUsage(new double[] { SIZE.GB.get(usedMemory, 2)});
        memoryData.setFree(new double[] { SIZE.GB.get(freeMemory, 2)});
        String json = om.writeValueAsString(memoryData);
        response.setBody(json);
    }

    @Override
    public Exception errorHandling(HttpResponse response, Exception e) throws Exception {
        e.printStackTrace();
        return e;
    }
}
