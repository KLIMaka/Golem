package golem.typesystem;

import gnu.bytecode.Method;

public interface IMethodResolver {

	ITypeResolver type();

	Method get();

}
