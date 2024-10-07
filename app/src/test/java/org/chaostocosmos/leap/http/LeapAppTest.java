package org.chaostocosmos.leap.http;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.chaostocosmos.leap.Leap;
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
        Leap leap = new Leap(args);
        leap.start();
    }
}
    