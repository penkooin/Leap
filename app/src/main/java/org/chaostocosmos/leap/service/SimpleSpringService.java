package org.chaostocosmos.leap.service;

import org.springframework.stereotype.Service;

@Service
public class SimpleSpringService {

    public SimpleSpringService() {
        System.out.println("SimpleSpringService injected.......");
    }

    public String helloLeap() {
        return "Hello Leap?";
    }
}
