package golem.symbol.leds;

import gnu.bytecode.ClassType;
import gnu.bytecode.Field;
import gnu.bytecode.Method;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.lex.Token;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Member implements Iled, Igen {

    public static Member instance = new Member();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        if (p.current().token.type != Token.ID) {
            p.current().token.error("Identifier expected");
            return null;
        }

        Symbol field_name = p.ncurrent();
        Type type = null;
        Field field = null;
        Method method = null;
        if (left.type instanceof ClassType) {
            ClassType left_class = (ClassType) left.type;
            field = left_class.getField(field_name.toString());
            if (field == null) {
                method = left_class.getDeclaredMethod(field_name.toString(), null);
                if (method == null && left_class.isInterface()) {
                    method = ClassType.objectType.getDeclaredMethod(field_name.toString(), null);
                }
                if (method == null) {
                    self.token.error("Undefined field '" + field_name.toString() + "' in "
                            + left_class.getName());
                } else {
                    type = method.getReturnType();
                }
            } else {
                type = field.getType();
            }
        } else {
            self.token.error("Expected a class type.");
        }

        self.first = left;
        self.second = field_name;
        self.third = field != null ? field : method;
        self.type = type;
        self.gen = instance;
        p.advance();

        return self;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

        Type left_type = self.first().type;

        if (left_type instanceof ClassType) {
            if (genResult) {
                Field field = (Field) self.third;
                g.getLocation().emitGetStatic(field);
            }
        } else {
            self.token.genError("Expected a class type.");
        }

    }
}
