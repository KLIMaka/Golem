package golem.lex;

import java.util.regex.Matcher;

public class Convert {

	static int level = 0;
	static boolean nled = true;

	public static Object NUM(Matcher m) {
		// return Integer.valueOf(m.group());
		return "0x" + m.group();
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

	public static Object BR(Matcher m) {
		if (m.group().equals("{")) {
			level++;
			return m.group();
		} else {
			level--;
			return m.group();
		}
	}

	public static Object RBR(Matcher m) {
		String sp = "\n";
		for (int i = 0; i < level - 1; i++)
			sp += "  ";
		level--;
		return sp + "}";
	}

	public static Object NL(Matcher m) {
		String sp = "";
		for (int i = 0; i < level; i++)
			sp += "  ";
		return "\n" + sp;
	}

}
