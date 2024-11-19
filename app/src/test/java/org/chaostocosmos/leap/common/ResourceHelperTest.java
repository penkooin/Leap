package org.chaostocosmos.leap.common;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.chaostocosmos.leap.resource.ResourceHelper;
import org.junit.Test;

public class ResourceHelperTest {

    @Test
    public void testExtractResource() throws IOException, URISyntaxException {
        Path tgt = Paths.get("/home/kooin/workspace/oss/Leap/tmp/");
        List<File> files = ResourceHelper.extractResource("org/", tgt, false);
        files.stream().forEach(f -> System.out.println(f.toString()));
    }
}
