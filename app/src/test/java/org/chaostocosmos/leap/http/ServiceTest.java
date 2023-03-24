package org.chaostocosmos.leap.http;

import org.chaostocosmos.leap.service.TimeServiceImpl;

public class ServiceTest {


    public static void main(String[] args) throws Exception {
        TimeServiceImpl time = new TimeServiceImpl();
        time.cloneTestString = "aaa";
        TimeServiceImpl time1 = (TimeServiceImpl) time.clone();
        time1.cloneTestString = "bbb";
        System.out.println(time.cloneTestString+"  "+time1.cloneTestString);
        System.out.println(time.toString());
        System.out.println(time1.toString());
    }  
}
