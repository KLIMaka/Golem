package golem.typesystem;

import gnu.bytecode.ClassType;
import gnu.bytecode.Field;

public class StaticFieldName extends QualifiedName {

    public ClassType clazz;
    public Field[]   fieldsChain;

    public StaticFieldName(ClassType clazz, Field[] fieldsChain) {
        this.clazz = clazz;
        this.fieldsChain = fieldsChain;
    }

}
