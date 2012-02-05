package golem.symbol.leds;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import gnu.bytecode.ClassType;
import gnu.bytecode.Field;
import gnu.bytecode.Method;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.lex.Token;
import golem.parser.Parser;
import golem.symbol.ILvalue;
import golem.symbol.IRvalue;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.typesystem.Methods;
import golem.typesystem.TypeUtils;

import java.util.List;

class Member implements Iled, IRvalue, ILvalue {

    public static Member instance = new Member();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        if (p.current().token.type != Token.ID) {
            p.current().token.error("Identifier expected");
            return null;
        }

        Symbol field_name = p.ncurrent();
        gnu.bytecode.Member member = null;
        ClassType clazz = (ClassType) left.type;
        String name = field_name.toString();
        if (left.type instanceof ClassType) {
            member = TypeUtils.resolveMember(clazz, name);
            if (member instanceof Field) {
                self.type = ((Field) member).getType();
                self.lval = instance;
            }
            if (member instanceof Method) {
                self.tags.put("method", new Methods(clazz, name));
            }
        } else {
            self.token.error("Expected a class type.");
        }

        self.first = left;
        self.second = field_name;
        self.third = member;
        self.rval = instance;
        p.advance();

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        if (genResult) {

            if (self.third instanceof Field) {
                Field field = (Field) self.third;
                if (field.getStaticFlag()) {
                    g.getLocation().emitGetStatic(field);
                } else {
                    self.first().invokeRval(g, true);
                    g.getLocation().emitGetField(field);
                }
            }

            if (self.third instanceof Method) {
                Methods methods = (Methods) self.tag("method");
                List<Method> stat = filter(having(on(Method.class).getStaticFlag()), methods.get());
            }
        }

    }

    @Override
    public void invoke(Symbol self, Gen gen, Symbol val) throws GenException {

        Field field = (Field) self.third;
        if (!field.getStaticFlag()) {
            self.first().invokeRval(gen, true);
        }
        val.invokeRval(gen, true);
        if (field.getStaticFlag()) {
            gen.getLocation().emitPutStatic(field);
        } else {
            gen.getLocation().emitPutField(field);
        }
    }
}
