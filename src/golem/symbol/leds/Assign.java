package golem.symbol.leds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Assign implements Iled, IRvalue {

    public static Assign instance = new Assign();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        if (left.lval == null) {
            self.token.error("The left-hand side of an assignment must be a variable");
            throw new ParseException("The left-hand side of an assignment must be a variable");
        }

        Symbol right = p.expression(self.lbp - 1);
        self.first = left;
        self.second = right;
        self.type = right.type;
        self.rval = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {
        self.first().invokeLval(g, self.second());
    }
}
