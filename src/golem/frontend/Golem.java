package golem.frontend;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Symbol;
import golem.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Golem {

	public static void main(String[] args) throws GenException, IllegalArgumentException, SecurityException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			IOException {

		String expr = Utils.getFile(new File("test.gol"));
		Parser p = new Parser(expr);
		Symbol res = p.program();
		System.out.println(res);
		Gen g = new Gen(p.getClassLoader());
		g.begin();
		res.invokeRval(g, false);
		g.end();
		// TypeUtils.resolveName("java.lang.System.out.println");
	}
}
