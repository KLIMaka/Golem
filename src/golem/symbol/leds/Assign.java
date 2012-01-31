package golem.symbol.leds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Assign implements Iled, Igen {

    public static Assign instance = new Assign();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        Symbol right = p.expression(self.lbp - 1);
        self.first = left;
        self.second = right;
        self.type = right.type;
        self.gen = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        self.second().invokeGen(g, true);
        if (genResult) {
            g.dup();
        }
        g.store(self.first().proto);
    }

}
