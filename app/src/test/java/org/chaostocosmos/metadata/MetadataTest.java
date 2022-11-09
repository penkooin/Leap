package org.chaostocosmos.metadata;

import java.io.IOException;
import java.nio.file.Paths;

import javax.transaction.NotSupportedException;

import org.junit.jupiter.api.Test; 

public class MetadataTest {

    @Test
    public void loadTest() throws NotSupportedException, IOException {
        Metadata metadata = new Metadata(Paths.get("D:/0.github/Leap/config/hosts.yml"));
        System.out.println(metadata.toString());
    }    
}
