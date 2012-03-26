package golem.symbol.leds;

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
import golem.symbol.nuds.ClassName;
import golem.typesystem.Methods;
import golem.typesystem.PlainOldTypeResolver;
import golem.typesystem.StaticFunctionTypeResolver;
import golem.typesystem.TypeUtils;

public class Member implements Iled, IRvalue, ILvalue {

    public static Member instance = new Member();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        if (p.current().token.type != Token.ID) {
            p.current().token.error("Identifier expected");
            return null;
        }

        Symbol field_name = p.ncurrent();
        gnu.bytecode.Member member = null;
        ClassType clazz = (ClassType) left.type.get();
        String name = field_name.toString();

        if (left.type.get() instanceof ClassType) {
            member = TypeUtils.resolveMember(clazz, name);
            if (member instanceof Field) {
                self.type = new PlainOldTypeResolver(((Field) member).getType());
                self.lval = instance;
            }
            if (member instanceof Method) {
                Methods methods = new Methods(clazz, name);
                self.type = new StaticFunctionTypeResolver(methods);
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

        if (!genResult)
            return;

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
            if (!(self.first().nud instanceof ClassName)) {
                // ClassType cl = (ClassType) self.type.get();
                // g.getLocation().emitNew(cl);
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
