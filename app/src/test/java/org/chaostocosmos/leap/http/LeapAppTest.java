package org.chaostocosmos.leap.http;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.chaostocosmos.leap.LeapApp;
import org.junit.Before;
import org.junit.Test;    
    
public class LeapAppTest {

    @Before
    public void setup(){

    }
        
    @Test
    public void test() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        Files.walk(Paths.get("./config")).sorted().forEach(p -> p.toFile().delete());
        LeapApp leap = new LeapApp(args);
        leap.start();
    }
}
    