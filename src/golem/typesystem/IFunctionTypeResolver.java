package golem.typesystem;

import gnu.bytecode.Method;
import gnu.bytecode.Type;

public interface IFunctionTypeResolver extends ITypeResolver {

    public Method match(Type[] types);

    public String getName();
}
