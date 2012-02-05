package golem.symbol.leds;

import gnu.bytecode.Method;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.Methods;
import golem.typesystem.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class Call implements Iled, IRvalue {

    public static Call instance = new Call();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        ArrayList<Symbol> args = new ArrayList<Symbol>();
        ArrayList<Type> at = new ArrayList<Type>();

        while (!p.current().toString().equals(")")) {
            Symbol arg = p.expression(0);
            args.add(arg);
            at.add(arg.type);

            if (p.current().toString().equals(")")) {
                break;
            }

            p.advanceSoft(",");
        }
        p.advance(); // ')'

        Type[] arg_types = at.toArray(new Type[at.size()]);
        Method method = ((Methods) left.tag("method")).match(arg_types);

        self.first = left;
        self.second = args;
        self.type = method.getReturnType();
        self.third = method;
        self.rval = instance;
        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        Symbol method_smb = self.first();
        method_smb.first().invokeRval(g, true);

        @SuppressWarnings("unchecked")
        List<Symbol> args = (List<Symbol>) self.second;
        Method method = (Method) self.third;
        Type[] param_types = method.getParameterTypes();
        for (int i = 0; i < args.size(); i++) {
            Symbol arg = args.get(i);
            Type formal_type = param_types[i];

            arg.invokeRval(g, true);
            TypeUtils.fixType(arg.type, formal_type, g.getLocation());
        }

        g.getLocation().emitInvoke(method);
        if (!genResult && !method.getReturnType().isVoid()) {
            g.pop(1);
        } else if (method.getReturnType().isVoid() && genResult) {
            g.integer(0);
        }
    }
}
