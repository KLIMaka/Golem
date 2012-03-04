package golem.symbol.leds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.ITypeResolver;
import golem.typesystem.TypeUtils;

public class Bin implements Iled, IRvalue {

    public static Bin instance = new Bin();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        Symbol right = p.expression(self.lbp);
        try {

            ITypeResolver type = TypeUtils.arithmType(left.type.get(), right.type.get());
            self.first = left;
            self.second = right;
            self.type = type;
            self.rval = instance;

        } catch (Exception e) {
            self.token.error("Incompatible arguments.");
        }

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        self.first().invokeRval(g, genResult);
        if (genResult) {
            TypeUtils.fixType(self.first().type.get(), self.type.get(), g.getLocation());
        }
        self.second().invokeRval(g, genResult);
        if (genResult) {
            TypeUtils.fixType(self.second().type.get(), self.type.get(), g.getLocation());
        }

        if (genResult) {
            if (self.toString().equals("+")) {
                g.add();
            } else if (self.toString().equals("-")) {
                g.sub();
            } else if (self.toString().equals("*")) {
                g.mul();
            } else if (self.toString().equals("/")) {
                g.div();
            } else if (self.toString().equals("%")) {
                g.mod();
            }
        }
    }
}
