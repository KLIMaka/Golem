package golem.typesystem;

import gnu.bytecode.ClassType;
import gnu.bytecode.Method;

public class StaticMethodName extends QualifiedName {

    public ClassType clazz;
    public Method    method;

    public StaticMethodName(ClassType clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

}
