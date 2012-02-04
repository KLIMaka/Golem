package golem.frontend;

import gnu.bytecode.Access;
import gnu.bytecode.ArrayClassLoader;
import gnu.bytecode.ClassType;
import gnu.bytecode.ClassTypeWriter;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Label;
import gnu.bytecode.Method;
import gnu.bytecode.Type;

public class MetaHelloWorld {
    public static void main(String[] args) throws Exception {
        // "public class HelloWorld extends java.lang.Object".
        ClassType c = new ClassType("HelloWorld");
        c.setSuper("java.lang.Object");
        c.setModifiers(Access.PUBLIC);

        Runnable a;

        // "public static int add(int, int)".
        Method m = c.addMethod("add", "(II)I", Access.PUBLIC | Access.STATIC);
        CodeAttr code = m.startCode();
        code.pushScope();
        Label l2 = new Label(code);

        code.emitGoto(l2);
        code.emitLoad(code.getArg(0));
        code.emitLoad(code.getArg(1));
        code.emitAdd(Type.intType);
        code.emitPop(1);

        l2.define(code);
        code.emitPushInt(12);
        // Variable resultVar = code.addLocal(Type.intType, "result");
        // code.emitDup();
        // code.emitStore(resultVar);
        // CpoolValue1 cc = m.getConstants().addFloat(31.154f);
        code.emitGetStatic(ClassType.make("java.lang.System").getDeclaredField("out"));
        // code.emitPushConstant(cc.index, Type.floatType);
        code.emitPushFloat(new Float(12.1f));
        code.emitInvokeVirtual(ClassType.make("java.io.PrintStream")
                .getMethod("println", new Type[] { Type.floatType }));
        code.emitReturn();
        code.popScope();

        // Get a byte[] representing the class file.
        // We could write this to disk if we wanted.
        byte[] classFile = c.writeToArray();

        // Disassemble this class.
        // The output is similar to javap(1).
        ClassTypeWriter.print(c, System.out, 0);

        // Load the generated class into this JVM.
        // gnu.bytecode provides ArrayClassLoader, or you can use your own.
        ArrayClassLoader cl = new ArrayClassLoader();
        cl.addClass("HelloWorld", classFile);

        // Actual invocation is just the usual reflection code.
        Class<?> helloWorldClass = cl.loadClass("HelloWorld", true);
        Class<?>[] argTypes = new Class[] { int.class, int.class };
        int result = (Integer) helloWorldClass.getMethod("add", argTypes).invoke(null, 1, 2);
        // System.err.println(result);

    }
}
