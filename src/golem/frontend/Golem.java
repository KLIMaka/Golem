package golem.frontend;

import golem.generator.Gen;
import golem.lex.GolemLexer;
import golem.parser.Parser;
import golem.symbol.Symbol;
import golem.utils.Utils;

import java.io.File;

public class Golem {

	public static void main(String[] args) throws Exception {
		String expr = Utils.getFile(new File("test.gol"));
		Parser p = new Parser(new GolemLexer(expr, "test.gol"));
		Symbol res = p.program();
		System.out.println(res);
		Gen g = new Gen();
		g.begin();
		res.invokeRval(g, false);
		g.end();
	}
}
