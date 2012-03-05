package golem.typesystem;

import gnu.bytecode.Method;
import gnu.bytecode.Type;

public class StaticFunctionTypeResolver implements IFunctionTypeResolver {

    private Methods m_methods;

    public StaticFunctionTypeResolver(Methods meths) {
        m_methods = meths;
    }

    @Override
    public Type get() {
        return null;
    }

    @Override
    public Method match(Type[] types) {
        return m_methods.match(types);
    }

    @Override
    public String getName() {
        return "static function " + m_methods.getClazz().getName() + "." + m_methods.getName();
    }

}
