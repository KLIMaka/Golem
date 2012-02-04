package golem.frontend;

import golem.frontend.B.C;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Symbol;
import golem.typesystem.TypeUtils;
import golem.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

class B {
    public static class C {
        void f() {
        };
    };
}

public class Golem {

    public static class A {}

    public static void main(String[] args) throws GenException, IllegalArgumentException, SecurityException,
            ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            IOException {

        String expr = Utils.getFile(new File("test.gol"));
        Parser p = new Parser(expr);
        Symbol res = p.program();
        System.out.println(res);
        Gen g = new Gen();
        g.begin();
        res.invokeGen(g, false);
        g.end();
        C c;
        ArrayList<String> imps = new ArrayList<String>();
        imps.add("golem.frontend");
        imps.add("golem.frontend.B");
        System.out.println("-" + TypeUtils.resolveClass("B$C", imps));

        TypeUtils.resolveName("java.lang.System.out.println");
    }
}
