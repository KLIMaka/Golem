package golem.symbol.leds;

import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.TypeUtils;

public class Bin implements Iled, Igen {

    public static Bin instance = new Bin();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        Symbol right = p.expression(self.lbp);

        Type type = TypeUtils.arithmType(left.type, right.type);
        if (type == null) {
            self.token.error("Incompatible arguments.");
        }

        self.first = left;
        self.second = right;
        self.type = type;
        self.gen = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        self.first().invokeGen(g, genResult);
        if (genResult) {
            TypeUtils.fixType(self.first().type, self.type, g.getLocation());
        }
        self.second().invokeGen(g, genResult);
        if (genResult) {
            TypeUtils.fixType(self.second().type, self.type, g.getLocation());
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
