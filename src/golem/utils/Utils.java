package golem.utils;

import gnu.bytecode.Access;
import gnu.bytecode.ArrayClassLoader;
import gnu.bytecode.ClassType;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Method;
import gnu.bytecode.Type;
import golem.typesystem.Methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ch.lambdaj.Lambda;

public class Utils {

    public static String getFile(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int len = 0;

        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }

        reader.close();
        return sb.toString();
    }

    public static ClassType createFunctionType(Methods methods, ArrayClassLoader cl) throws ClassNotFoundException {

        ClassType clazz = new ClassType("_" + methods.getName());
        clazz.setSuper("java.lang.Object");
        clazz.setModifiers(Access.PUBLIC);

        for (gnu.bytecode.Method m : methods.get()) {

            String params = "";
            List<Type> types = Arrays.asList(m.getParameterTypes());
            if (types.size() != 0)
                params = Lambda.joinFrom(types).getSignature();

            Method mm = clazz.addMethod(methods.getName(), "(" + params + ")" + m.getReturnType().getSignature(),
                    Access.PUBLIC | Access.STATIC);
            CodeAttr code = mm.startCode();
            code.pushScope();

            for (int i = 0; i < types.size(); i++)
                code.emitLoad(code.addLocal(types.get(i).getRealType()));

            code.emitInvoke(m);
            code.emitReturn();
            code.popScope();
        }

        byte[] classFile = clazz.writeToArray();
        cl.addClass("_" + methods.getName(), classFile);

        return (ClassType) ClassType.make(cl.loadClass("_" + methods.getName(), true));
    }
}
