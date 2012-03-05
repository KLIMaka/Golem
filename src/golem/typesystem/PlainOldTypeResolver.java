package golem.typesystem;

import gnu.bytecode.Type;

public class PlainOldTypeResolver implements ITypeResolver {

    private Type m_type;

    public PlainOldTypeResolver(Type type) {
        m_type = type;
    }

    @Override
    public Type get() {
        return m_type;
    }

}
