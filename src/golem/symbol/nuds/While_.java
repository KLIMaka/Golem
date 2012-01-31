package golem.symbol.nuds;

import gnu.bytecode.Label;
import gnu.bytecode.Variable;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class While_ implements Inud, Igen {

    public static While_ instance = new While_();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        p.advance("(");
        self.first = p.expression(0);
        p.advance(")");
        self.second = p.expression(0);
        self.type = self.second().type;
        self.gen = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        if (genResult) {
            Variable res = g.genVar();
            g.integer(0);
            g.genSore(res);
            Label check = g.getLabel();
            Label start = g.getLabel();
            g.jmp(check);
            start.define(g.getLocation());
            self.second().invokeGen(g, true);
            g.genSore(res);
            check.define(g.getLocation());
            self.first().invokeGen(g, true);
            g.ifn_(start);
            g.genLoad(res);
        } else {
            Label check = g.getLabel();
            Label start = g.getLabel();
            g.jmp(check);
            start.define(g.getLocation());
            self.second().invokeGen(g, false);
            check.define(g.getLocation());
            self.first().invokeGen(g, true);
            g.ifn_(start);
        }

    }

}
