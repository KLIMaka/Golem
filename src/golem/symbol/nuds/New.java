package golem.symbol.nuds;

import gnu.bytecode.ClassType;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Method;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.PlainOldTypeResilver;

import java.util.ArrayList;
import java.util.List;

public class New implements Inud, IRvalue {

    public static New instance = new New();

    @Override
    public Symbol invoke(Symbol self, Parser p) throws ParseException {

        if (!(p.current().nud instanceof ClassName)) {
            self.token.error("Class name expected.");
        }
        Symbol clazz = p.ncurrent();
        p.advance();
        p.advance("(");

        ArrayList<Symbol> args = new ArrayList<Symbol>();
        ArrayList<Type> types = new ArrayList<Type>();
        while (!p.current().toString().equals(")")) {
            Symbol expr = p.expression(0);
            args.add(expr);
            types.add(expr.type.get());

            if (p.current().toString().equals(")")) {
                break;
            }
            p.advance(",");
        }
        p.advance(); // ')'

        Type[] arg_types = types.toArray(new Type[types.size()]);
        ClassType type = (ClassType) p.scope().resolveImportExt(clazz.toString());
        Method ctor = type.getMethod("<init>", arg_types);
        if (ctor == null) {
            self.token.error("The constructor is undefined.");
        }

        self.first = clazz;
        self.second = args;
        self.third = ctor;
        self.type = new PlainOldTypeResilver(type);
        self.rval = instance;

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        CodeAttr code = g.getLocation();

        code.emitNew((ClassType) self.type);
        code.emitDup();

        @SuppressWarnings("unchecked")
        List<Symbol> args = (List<Symbol>) self.second;
        for (Symbol arg : args) {
            arg.invokeRval(g, true);
        }
        code.emitInvoke((Method) self.third);

        if (!genResult) {
            code.emitPop(1);
        }

    }

}
