package golem.symbol;

import golem.parser.Parser;

public interface Iled {

    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException;
}
