package golem.typesystem;

import gnu.bytecode.ClassType;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Field;
import gnu.bytecode.Member;
import gnu.bytecode.Method;
import gnu.bytecode.PrimType;
import gnu.bytecode.Type;

import java.util.ArrayList;
import java.util.List;

public class TypeUtils {

    public static Type arithmType(Type lh, Type rh) {

        int cmp = rh.compare(lh);
        if (cmp > 0) {
            return rh;
        } else if (cmp == 0) {
            if (rh instanceof PrimType) {
                return rh;
            }
            return lh;
        } else if (cmp == -1) {

            return lh;
        } else {
            return null;
        }
    }

    public static Type widerType(Type lh, Type rh) {

        int cmp = rh.compare(lh);
        if (cmp > 0) {
            if (lh == Type.nullType && rh instanceof PrimType) {
                return ((PrimType) rh).boxedType();
            } else {
                return rh;
            }
        } else if (cmp == 0) {
            if (rh instanceof ClassType) {
                return rh;
            }
            return lh;
        } else if (cmp == -1) {
            if (rh == Type.nullType && lh instanceof PrimType) {
                return ((PrimType) lh).boxedType();
            } else {
                return lh;
            }

        } else {
            Type ret = Type.lowestCommonSuperType(lh, rh);
            if (ret != null) {
                return ret;
            }
            return Type.objectType;
        }
    }

    public static void fixType(Type from, Type to, CodeAttr code) {

        // boxing
        if (from instanceof PrimType && to instanceof ClassType) {
            PrimType from_prim = (PrimType) from;
            Method valueOf = from_prim.boxedType().getMethod("valueOf", new Type[] { from });
            code.emitInvoke(valueOf);
        }

        // unboxing
        else if (from instanceof ClassType && to instanceof PrimType) {

            Method val = null;
            switch (to.getSignature().charAt(0)) {
            case 'I':
                val = Type.intValue_method;
                break;
            case 'F':
                val = Type.floatValue_method;
                break;
            case 'S':
                val = ClassType.make("java.lang.Number").getMethod("shortValue", Type.typeArray0);
                break;
            default:
                break;
            }

            code.emitInvoke(val);
        }

        // converting
        else if (from instanceof PrimType && to instanceof PrimType) {
            code.emitConvert(from, to);
        }

    }

    protected static boolean isMethodMatch(Method method, String name, Type[] args) {

        if (!method.getName().equals(name)) {
            return false;
        }

        Type[] formals = method.getParameterTypes();
        if (formals.length != args.length) {
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            Type fact = args[i];
            Type formal = formals[i];
            int cmp = formal.compare(fact);
            if (cmp < 0) {
                return false;
            }
        }

        return true;
    }

    public static String arrToName(String[] arr, int start, int len) {
        String ret = "";
        for (int i = start; i < start + len; i++)
            ret += "." + arr[i];
        return ret.substring(1);
    }

    public static Class<?> resolveClass(String name, List<String> imports) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            if (imports != null) {
                for (String prefix : imports) {
                    try {
                        return Class.forName(prefix + "." + name);
                    } catch (Exception e1) {
                    }
                }
            }
        }
        return null;
    }

    public static Member resolveName(String name) {

        String[] parts = name.split("\\.");
        int len = parts.length;

        ClassType ct = null;
        int i;
        for (i = len - 1; i > 0; i--) {
            try {
                String className = arrToName(parts, 0, i);
                Class.forName(className);
                ct = ClassType.make(className);
                break;
            } catch (Exception e) {
            }
        }

        System.out.println("class: " + ct.toString());
        for (;; i++) {
            if (ct != null) {
                if (i != len - 1) {
                    String fieldName = parts[i];
                    Field field = ct.getField(fieldName);
                    System.out.println("field: " + fieldName);

                    if (field.getType().isSubtype(Type.objectType)) {
                        ct = (ClassType) field.getType();
                    }
                } else {
                    System.out.println("last: " + parts[i]);
                    break;
                }
            }
        }

        return null;
    }

    public static Method searchMethod(ClassType ct, String name, Type[] args) {

        ArrayList<Method> methods = new ArrayList<Method>();
        for (ClassType clazz = ct; clazz != null; clazz = clazz.getSuperclass()) {
            Method method = clazz.getMethods();
            for (; method != null; method = method.getNext()) {
                if (isMethodMatch(method, name, args) == true) {
                    methods.add(method);
                }
            }
            for (ClassType interf : clazz.getInterfaces()) {
                Method interf_method = interf.getMethods();
                for (; interf_method != null; interf_method = interf_method.getNext()) {
                    if (isMethodMatch(interf_method, name, args) == true) {
                        methods.add(interf_method);
                    }
                }
            }
        }
        if (ct.isInterface()) {
            Method obj_method = ClassType.objectType.getMethods();
            for (; obj_method != null; obj_method = obj_method.getNext()) {
                if (isMethodMatch(obj_method, name, args)) {
                    methods.add(obj_method);
                }
            }
        }

        if (methods.size() > 0) {
            return methods.get(0);
        } else {
            return null;
        }

    }

}
