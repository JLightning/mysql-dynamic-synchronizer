package com.jhl.mds.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    public static List<String> findAllStringMatches(String subject, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(subject);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

    public static List<List<String>> findAllStringSubmatches(String subject, String regex) {
        List<List<String>> matches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(subject);
        while (m.find()) {
            List<String> currentMatches = new ArrayList<>();
            for (int i = 0; i <= m.groupCount(); i++) {
                currentMatches.add(m.group(i));
            }
            matches.add(currentMatches);
        }
        return matches;
    }
}
