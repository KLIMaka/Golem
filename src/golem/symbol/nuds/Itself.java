package golem.symbol.nuds;

import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.lex.Token;
import golem.parser.Parser;
import golem.symbol.ILvalue;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.PlainOldTypeResilver;

public class Itself implements Inud, IRvalue, ILvalue {

    public static Itself instance = new Itself();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        switch (self.token.type) {
        case Token.ID:
            self.type = self.proto.type;
            self.lval = instance;
            break;
        case Token.CHAR:
            self.type = new PlainOldTypeResilver(Type.charType);
            break;
        case Token.INT:
            self.type = new PlainOldTypeResilver(Type.intType);
            break;
        case Token.FLOAT:
            self.type = new PlainOldTypeResilver(Type.floatType);
            break;
        case Token.STRING:
            self.type = new PlainOldTypeResilver(Type.javalangStringType);
            break;

        default:
            self.type = new PlainOldTypeResilver(Type.errorType);
            break;
        }

        self.rval = instance;
        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        try {
            if (genResult) {
                switch (self.token.type) {
                case Token.ID:
                    g.fetch(self.proto);
                    break;

                case Token.INT:
                    g.integer(Integer.parseInt(self.toString()));
                    break;

                case Token.FLOAT:
                    g.float_(Float.parseFloat(self.toString()));
                    break;

                case Token.STRING:
                    g.string(self.toString().substring(1, self.toString().length() - 1));
                    break;

                default:
                    g.integer(0);
                    break;
                }
            }

        } catch (Exception e) {
            g.integer(0);
        }
    }

    @Override
    public void invoke(Symbol self, Gen gen, Symbol val) throws GenException {
        val.invokeRval(gen, true);
        gen.store(self.proto);
    }
}
