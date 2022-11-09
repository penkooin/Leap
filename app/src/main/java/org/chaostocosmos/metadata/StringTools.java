package org.chaostocosmos.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * String utility & tools 
 * 
 * @author 9ins
 */
public class StringTools {
    /**
     * Substring with excluding start, end charcter and getting first element of matching results
     * @param original
     * @param start
     * @param end
     * @return
     */
    public static String substringBetweenFirst(String original, char start, char end) {
        return substringBetweenIndex(original, start, end, 0);
    }

    /**
     * Substring with excluding start, end charcter and getting first element of matching results
     * @param original
     * @param start
     * @param end
     * @return
     */
    public static String substringBetweenLast(String original, char start, char end) {
        List<String> matches = substringBetween(original, start, end);
        return matches.size() > 0 ? matches.get(matches.size()-1) : null;
    }

    /**
     * Substring with excluding start, end character and getting of specified index position
     * @param original
     * @param start
     * @param end
     * @param idx
     * @return
     */
    public static String substringBetweenIndex(String original, char start, char end, int idx) {
        List<String> matches = substringBetween(original, start, end);
        return matches.size() > 0 ? matches.get(idx) : null;
    }

    /**
     * Substring with excluding start and end character.
     * @param original
     * @param start
     * @param end
     * @return
     */
    public static List<String> substringBetween(String original, char start, char end) {
        List<String> results = substring(original, start, end);
        return results.stream().filter(s -> !s.equals("") && s.charAt(0) == start && s.charAt(s.length()-1) == end).map(s -> s.substring(s.indexOf(start)+1, s.lastIndexOf(end))).collect(Collectors.toList());
    }

    /**
     * Substring last element by start, end character.
     * @param original
     * @param start
     * @param end
     * @return
     */
    public static String substringLast(String original, char start, char end) {
        List<String> matches = substring(original, start, end);
        return matches.size() > 0 ? matches.get(matches.size()-1) : null;
    }

    /**
     * Substring fast element by start, end character.
     * @param original
     * @param start
     * @param end
     * @return
     */
    public static String substringFirst(String original, char start, char end) {
        return substringIndex(original, start, end, 0);
    }

    /**
     * Substring with excluding start and end character.
     * @param original
     * @param start
     * @param end
     * @param idx
     * @return
     */
    public static String substringIndex(String original, char start, char end, int idx) {
        List<String> matches = substring(original, start, end);
        return matches.size() > 0 ? matches.get(idx) : null;
    }

    /**
     * Substring with including start and end character.
     * @param original
     * @param start
     * @param end
     * @return
     */
    public static List<String> substring(String original, char start, char end) {
        String result = "";
        boolean between = false;
        List<String> matches = new ArrayList<>();
        for(int i=0; i<original.length(); i++) {
            char ch = original.charAt(i);
            if(ch == start || between == true) {
                result += ch;
                between = true;
            } 
            if(ch == end) {
                matches.add(result);
                result = "";
                between = false;
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println(substringBetweenIndex("ab[1]cd[2]efgh[3]ijklmn[4]ojsi[5]dle]sdjflkjdf[md]j", '[', ']', 4));
    }    
}
