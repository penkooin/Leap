package org.chaostocosmos.leap.service;

import org.springframework.stereotype.Service;

/**
 * SimpleSpringService
 * 
 * @author 9ins
 */
@Service
public class SimpleSpringService {

    /**
     * Default constructor
     */
    public SimpleSpringService() {
        System.out.println("SimpleSpringService injected.......");
    }

    /**
     * Return Hello Leap?
     * @return
     */
    public String helloLeap() {
        return "Hello Leap?";
    }
}
