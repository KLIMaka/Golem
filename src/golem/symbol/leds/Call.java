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
import golem.typesystem.IFunctionTypeResolver;
import golem.typesystem.IMethodResolver;
import golem.typesystem.ITypeResolver;
import golem.typesystem.TypeUtils;
import golem.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import ch.lambdaj.Lambda;

public class Call implements Iled, IRvalue {

    public static Call instance = new Call();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        ArrayList<Symbol> args = Utils.list(p, ",", ")");
        List<ITypeResolver> types = Lambda.extract(args, Lambda.on(Symbol.class).type());

        IMethodResolver method = ((IFunctionTypeResolver) left.type).match(types);
        if (method == null) {
            self.token.error("Cannot match method signature");
        }

        self.first = left;
        self.second = args;
        self.type = method.type();
        self.third = method;
        self.rval = instance;
        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        @SuppressWarnings("unchecked")
        List<Symbol> args = (List<Symbol>) self.second;
        Method method = ((IMethodResolver) self.third).get();

        if (method.getStaticFlag() == false) {
            Symbol method_smb = self.first();
            method_smb.first().invokeRval(g, true);
        }

        Type[] param_types = method.getParameterTypes();
        for (int i = 0; i < args.size(); i++) {
            Symbol arg = args.get(i);
            Type formal_type = param_types[i];

            arg.invokeRval(g, true);
            TypeUtils.fixType(arg.type.get(), formal_type, g.getLocation());
        }

        g.getLocation().emitInvoke(method);
        if (!genResult && !method.getReturnType().isVoid()) {
            g.pop(1);
        } else if (method.getReturnType().isVoid() && genResult) {
            g.getLocation().emitPushNull();
        }
    }
}
