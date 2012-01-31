package golem.symbol.nuds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.lex.Token;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Import implements Inud, Igen {

    public static Import instance = new Import();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        String imp = "";
        for (;;) {
            if (p.current().token.type != Token.ID && !p.current().toString().equals("*")) {
                self.token.error("Error in import.");
            }

            imp += p.current().toString();
            p.advance();

            if (!p.current().toString().equals(".")) {
                break;
            }

            p.advance();
            imp += ".";
        }

        p.scope().addImport(imp);
        self.first = imp;
        self.gen = instance;
        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {
    }

}
