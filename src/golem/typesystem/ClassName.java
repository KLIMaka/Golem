package golem.typesystem;

import gnu.bytecode.ClassType;

public class ClassName extends QualifiedName {

    public ClassType clazz;

    public ClassName(ClassType clazz) {
        this.clazz = clazz;
    }

}
