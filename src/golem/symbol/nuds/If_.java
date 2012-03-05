package golem.symbol.nuds;

import gnu.bytecode.Label;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.PlainOldTypeResolver;
import golem.typesystem.TypeUtils;

public class If_ implements Inud, IRvalue {

    public static If_ instance = new If_();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        p.advance("(");
        self.first = p.expression(0);
        p.advance(")");
        self.second = p.expression(0);

        if (p.current().token.val.equals("else")) {
            p.advance();
            self.third = p.expression(0);
        }

        Type type = self.second().type.get();
        if (self.third != null) {
            type = TypeUtils.widerType(self.second().type.get(), self.third().type.get());
            if (type == null) {
                self.token.error("Incompatible types.");
            }
        }

        self.type = new PlainOldTypeResolver(type);
        self.rval = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        self.first().invokeRval(g, true);
        Label lab = g.getLabel();
        g.ife_(lab);
        self.second().invokeRval(g, genResult);
        TypeUtils.fixType(self.second().type.get(), self.type.get(), g.getLocation());

        if (self.third != null) {
            Label lab1 = g.getLabel();
            g.jmp(lab1);
            lab.define(g.getLocation());
            self.third().invokeRval(g, genResult);
            TypeUtils.fixType(self.third().type.get(), self.type.get(), g.getLocation());
            lab1.define(g.getLocation());
        } else {
            lab.define(g.getLocation());
        }

    }

}
