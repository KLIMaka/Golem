package golem.symbol.nuds;

import golem.parser.Parser;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Null_nud implements Inud {

	public static Null_nud instance = new Null_nud();

	@Override
	public Symbol invoke(Symbol self, Parser p) throws ParseException {
		self.token.error("Symbol " + self.token.value() + " undefined.");
		return null;
	}

}
