package org.chaostocosmos.leap.common.file;

import org.junit.jupiter.api.Test;
import java.io.File;

public class FileUtilsTest {

    File file = new File("/home/kooin/workspace/oss/Leap/LICENSE");

    @Test
    void testGetMp4DurationSeconds() {

    }

    @Test
    void testReadFile() {        
        byte[] bytes = FileUtils.readFile(file, 1024);
        System.out.println(new String(bytes)+" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }

    @Test
    void testReadFilePart() {
        byte[] bytes = FileUtils.readFilePart(file, 1, 13);
        System.out.println(new String(bytes));        
    }

    @Test
    void testSaveBinary() {

    }

    @Test
    void testSaveBinary2() {

    }

    @Test
    void testSaveText() {

    }

    @Test
    void testSearchFiles() {

    }
}
