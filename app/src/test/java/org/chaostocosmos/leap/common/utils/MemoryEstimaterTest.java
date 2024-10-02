package org.chaostocosmos.leap.common.utils;

import org.chaostocosmos.leap.service.ConfigService;
import org.github.jamm.MemoryMeter;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.util.ObjectUtils;
import org.openjdk.jol.vm.VM;

public class MemoryEstimaterTest {

    String str = new String("jsdlkjf;asdlfj;dlsajf;lsdjfk;lsdjf;ls");

    @Test
    void testEstimateObjectSize() throws IllegalArgumentException, IllegalAccessException {
        System.out.println(ClassLayout.parseInstance(this).toPrintable());
        MemoryMeter meter = MemoryMeter.builder().build();
        long len = meter.measureDeep(this);        
        System.out.println(len);
        len = meter.measure(this);
        System.out.println(len);
    }
    //18588715136
}
