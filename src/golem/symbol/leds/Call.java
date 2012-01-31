package golem.symbol.leds;

import gnu.bytecode.ClassType;
import gnu.bytecode.Method;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class Call implements Iled, Igen {

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

        String method_name = left.second().toString();
        Type[] arg_types = at.toArray(new Type[at.size()]);
        ClassType ct = (ClassType) left.first().type;

        Method method = TypeUtils.searchMethod(ct, method_name, arg_types);

        if (method == null) {
            self.token.error("Undefined method '" + left.second().toString() + "' in "
                    + ct.getName());
        }

        self.first = left;
        self.second = args;
        self.type = method.getReturnType();
        self.third = method;
        self.gen = instance;
        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        Symbol method_smb = self.first();
        method_smb.first().invokeGen(g, true);

        @SuppressWarnings("unchecked")
        List<Symbol> args = (List<Symbol>) self.second;
        Method method = (Method) self.third;
        Type[] param_types = method.getParameterTypes();
        for (int i = 0; i < args.size(); i++) {
            Symbol arg = args.get(i);
            Type formal_type = param_types[i];

            arg.invokeGen(g, true);
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
