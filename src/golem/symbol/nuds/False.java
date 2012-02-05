package golem.symbol.nuds;

import gnu.bytecode.CodeAttr;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class False implements IRvalue, Inud {

    public static False instance = new False();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        self.type = Type.booleanType;
        self.rval = instance;
        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        CodeAttr code = g.getLocation();
        if (genResult) {
            code.emitPushInt(0);
        }
    }

}
