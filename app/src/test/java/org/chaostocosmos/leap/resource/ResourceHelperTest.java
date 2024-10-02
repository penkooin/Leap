package org.chaostocosmos.leap.resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class ResourceHelperTest {

    @Test
    public void testExtractResource() throws IOException, URISyntaxException {
        Path tgt = Paths.get("/home/kooin/workspace/oss/Leap/tmp/");
        List<File> files = ResourceHelper.extractResource("org/", tgt, null);
        files.stream().forEach(f -> System.out.println(f.toString()));
    }
}
