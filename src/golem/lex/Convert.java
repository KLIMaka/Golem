package golem.lex;

import java.util.regex.Matcher;

public class Convert {

    public static Object NUM(Matcher m) {
        // return Integer.valueOf(m.group());
        return "0x" + m.group();
    }

    public static Object WS(Matcher m) {
        return " ";
    }

    public static Object ID(Matcher m) {
        String id = m.group();
        String[] parts = id.split("_");
        String res = parts[0];
        for (int i = 1; i < parts.length; i++) {
            String first = parts[i].substring(0, 1).toUpperCase();
            String rest = parts[i].substring(1, parts[i].length());
            res += first + rest;
        }
        return res;
    }

}
