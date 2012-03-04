package golem.symbol.nuds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.lex.Token;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.StaticFunctionTypeResolver;

public class Def implements Inud, IRvalue {

    public static Def instance = new Def();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        if (p.current().token.type != Token.ID) {
            p.current().token.error("Identifier expected");
        }

        Symbol name = p.ncurrent();
        p.advance();
        p.advance("=");
        Symbol expr = p.expression(0);

        p.scope().define(name);
        name.type = expr.type;
        self.type = name.type;
        self.first = name;
        self.second = expr;
        self.rval = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws ParseException, GenException {

        if (!(self.type instanceof StaticFunctionTypeResolver)) {
            self.second().invokeRval(g, true);
            g.define(self.first());
            if (genResult) {
                g.dup();
            }
            g.store(self.first());
        }
    }
}
