package golem.generator;

import gnu.bytecode.Access;
import gnu.bytecode.ArrayClassLoader;
import gnu.bytecode.ClassType;
import gnu.bytecode.ClassTypeWriter;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Label;
import gnu.bytecode.Method;
import gnu.bytecode.PrimType;
import gnu.bytecode.Type;
import gnu.bytecode.Variable;
import golem.symbol.Symbol;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Gen {

    private Map<Symbol, Variable> m_locals = new HashMap<Symbol, Variable>();
    private ClassType             m_class;
    private Method                m_method;
    private CodeAttr              m_code;

    public Gen() {

        m_class = new ClassType("GolemExec");
        m_class.setSuper("java.lang.Object");
        m_class.setModifiers(Access.PUBLIC);

        m_method = m_class.addMethod("exec", "()V", Access.PUBLIC | Access.STATIC);
        m_code = m_method.startCode();
    }

    public void begin() {
        m_code.pushScope();
    }

    public void end() throws ClassNotFoundException, IllegalArgumentException, SecurityException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        m_code.emitReturn();
        m_code.popScope();

        ClassTypeWriter.print(m_class, System.out, 0);

        byte[] classFile = m_class.writeToArray();
        ArrayClassLoader cl = new ArrayClassLoader();
        cl.addClass("GolemExec", classFile);

        Class<?> helloWorldClass = cl.loadClass("GolemExec", true);
        Class<?>[] argTypes = new Class[] {};
        helloWorldClass.getMethod("exec", argTypes).invoke(null);
    }

    public void store(Symbol smb) {
        Variable var = m_locals.get(smb.proto);
        m_code.emitStore(var);
    }

    public void fetch(Symbol smb) {
        Variable var = m_locals.get(smb);
        m_code.emitLoad(var);
    }

    public void define(Symbol smb) {
        Variable var = m_code.addLocal(smb.type, smb.toString());
        m_locals.put(smb, var);
    }

    public Variable genVar() {
        return m_code.addLocal(Type.intType);
    }

    public void genLoad(Variable var) {
        m_code.emitLoad(var);
    }

    public void genSore(Variable var) {
        m_code.emitStore(var);
    }

    public Type resultType() {
        Type t1 = m_code.topType();
        Type t2 = m_code.stack_types[m_code.getSP() - 2];
        if (t1.compare(t2) >= 0) {
            return t1;
        } else {
            return t2;
        }
    }

    public void add() {
        PrimType res_Type = (PrimType) resultType();
        PrimType oper_type = (PrimType) res_Type.promote();
        m_code.emitAdd(oper_type);
        m_code.emitConvert(oper_type, res_Type);

    }

    public void sub() {
        PrimType res_Type = (PrimType) resultType();
        PrimType oper_type = (PrimType) res_Type.promote();
        m_code.emitSub(oper_type);
        m_code.emitConvert(oper_type, res_Type);
    }

    public void mul() {
        m_code.emitMul();
    }

    public void div() {
        m_code.emitDiv();
    }

    public void mod() {
        m_code.emitRem();
    }

    public void jmp(Label lab) {
        m_code.emitGoto(lab);
    }

    public void ifn_(Label lab) {
        m_code.emitGotoIfIntNeZero(lab);
    }

    public void ife_(Label lab) {
        m_code.emitGotoIfIntEqZero(lab);
    }

    public Label getLabel() {
        return new Label();
    }

    public CodeAttr getLocation() {
        return m_code;
    }

    public void dup() {
        m_code.emitDup();
    }

    public void pop(int i) {
        m_code.emitPop(i);
    }

    public void integer(int x) {
        m_code.emitPushInt(x);
    }

    public void float_(float f) {
        m_code.emitPushFloat(f);
    }

    public void string(String s) {
        m_code.emitPushString(s);
    }

    public void getStatic(String clazz, String field) {
        m_code.emitGetStatic(ClassType.make(clazz).getField(field));
    }

    public void invokeVirtual(String clazz, String method) {
        m_code.emitInvokeVirtual(ClassType.make(clazz).getMethod(method, new Type[] { Type.intType }));
    }
}
