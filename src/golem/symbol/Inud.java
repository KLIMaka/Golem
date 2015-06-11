package golem.symbol;

import golem.parser.Parser;

public interface Inud {

	public Symbol invoke(Symbol self, Parser p) throws ParseException;
}
