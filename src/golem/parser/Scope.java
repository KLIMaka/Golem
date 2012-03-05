package golem.parser;

import gnu.bytecode.Type;
import golem.symbol.Symbol;
import golem.symbol.leds.Null_led;
import golem.symbol.nuds.ClassName;
import golem.symbol.nuds.Itself;
import golem.symbol.nuds.Null_nud;
import golem.typesystem.ITypeResolver;
import golem.typesystem.PlainOldTypeResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scope {

    private Scope               m_parent;
    private Map<String, Symbol> m_defs    = new HashMap<String, Symbol>();
    private ArrayList<String>   m_imports = new ArrayList<String>();

    protected static Symbol     define    = getDefine();
    protected static Symbol     className = getClassName();

    protected static Symbol getDefine() {

        Symbol smb = new Symbol();
        smb.nud = Null_nud.instance;
        smb.led = Null_led.instance;
        return smb;
    }

    protected static Symbol getClassName() {

        Symbol smb = new Symbol();
        smb.led = Null_led.instance;
        smb.nud = ClassName.instance;
        return smb;
    }

    public Symbol find(String name) {

        Symbol f = m_defs.get(name);
        if (f != null) {
            return f;
        }

        if (resolveImport(name) != null) {
            return className;
        }

        if (m_parent != null) {
            return m_parent.find(name);
        }

        return define;
    }

    public void addImport(String imp) {
        m_imports.add(imp);
    }

    public ITypeResolver resolveImport(String name) {

        for (String imp : m_imports) {
            final Pattern pat = Pattern.compile("^(([a-z0-9_]+\\.)+)(.+)");
            Matcher m = pat.matcher(imp);
            m.find();
            String first = m.group(1);
            try {
                Class.forName(first + name);
                return new PlainOldTypeResolver(Type.getType(first + name));
            } catch (Exception e) {
            }
        }

        return null;
    }

    public ITypeResolver resolveImportExt(String name) {

        ITypeResolver ret = resolveImport(name);
        if (ret != null) {
            return ret;
        }

        if (m_parent != null) {
            return m_parent.resolveImportExt(name);
        }

        return null;
    }

    public Symbol define(Symbol smb) {

        smb.nud = Itself.instance;
        smb.proto = smb;
        return m_defs.put(smb.token.val, smb);
    }

    public Scope getParent() {
        return m_parent;
    }

    public void setParent(Scope parent) {
        m_parent = parent;
    }

}
