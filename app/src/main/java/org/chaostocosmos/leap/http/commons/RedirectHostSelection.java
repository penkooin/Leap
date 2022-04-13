package org.chaostocosmos.leap.http.commons;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Random selection of host in ratio Map<String, Integer> redirectMap
 * @author 9ins 
 */
public class RedirectHostSelection {

    Map<String, Integer> redirectMap;

    /**
     * Creates with redirectMap
     * @param redirectMap
     */
    public RedirectHostSelection(Map<String, Integer> redirectMap) {
        this.redirectMap = redirectMap;
    }

    /**
     * Get seleted host url
     * @return
     * @throws UnknownHostException
     */
    public String getSelectedHost() throws UnknownHostException {
        Random random = new Random();
        List<Integer> ratioList = this.redirectMap.values().stream().collect(Collectors.toList());
        int sum = ratioList.stream().reduce((a, b) -> Integer.sum(a, b)).get().intValue();
        int start = 0;
        int bound = 0;
        int ran = random.nextInt(sum);
        for (int i = 0; i < ratioList.size(); i++) {
            int ratio = ratioList.get(i);
            bound += ratio;
            if (ran >= start && ran < bound) {
                return this.redirectMap.keySet().toArray()[i]+"";
            }
            start += ratio;
        }
        throw new UnknownHostException("Host is not found in ratio Map.");
    }
}
