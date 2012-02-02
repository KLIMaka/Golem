package golem.parser;

import golem.lex.GolemLexer;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.symbol.nuds.Itself;

import java.util.HashMap;
import java.util.Map;

public class Parser1 extends Parser {

    private GolemLexer          m_lex;
    private Map<String, Symbol> m_symbols = new HashMap<String, Symbol>();
    private Symbol              m_current;

    public Parser1(String src) {
        super(src);
        m_lex = new GolemLexer(src, "null");
    }

    public void advance() {
        m_lex.next();
    }

    public void advance(String lit) throws ParseException {
        if (!m_lex.getValue().equals(lit)) {
            m_lex.error("Expected '" + lit + "'");
        }
        m_lex.next();
    }

    public void advance(int id) throws ParseException {
        if (m_lex.getId() != id) {
            m_lex.error("Expected '" + id + "'");
        }
        m_lex.next();
    }

    private static Symbol itself;

    protected Symbol itselfSymbol() {
        if (itself == null) {
            itself = new Symbol();
            itself.nud = Itself.instance;
        }
        return itself;
    }

    private void updateCurrent(Symbol proto) {

        if (proto != null) {
            m_current.lbp = proto.lbp;
            m_current.nud = proto.nud;
            m_current.led = proto.led;
            m_current.gen = proto.gen;
            m_current.proto = proto;
        }
    }

    public void resolveSymbol() {
        Symbol f = m_symbols.get(m_lex.getValue());
        if (f == null) {
            updateCurrent(itselfSymbol());
        } else {
            updateCurrent(f);
        }
    }

    public Symbol current() {
        return m_current;
    }

    public Symbol ncurrent() {

        Symbol smb = m_current.clone();
        smb.token1 = m_lex.ntok();
        return smb;
    }

    public Symbol expr(int rbp) {
        Symbol t = ncurrent();
        advance();
        Symbol left = t.nud.invoke(t, this);
        while (rbp < current().lbp) {
            t = ncurrent();
            advance();
            left = t.led.invoke(t, this, left);
        }
        return left;
    }

}
