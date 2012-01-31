package golem.symbol.nuds;

import golem.parser.Parser;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Parentheses implements Inud {

    public static Parentheses instance = new Parentheses();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        Symbol expr = p.expression(0);
        p.advance(")");
        return expr;
    }

}
