package golem.lex;

import golem.symbol.ParseException;

public class GolemLexer extends GenericMatcher {

    private String          m_name;
    private CharSequence    m_input;
    private int             m_line    = 1;
    private int             m_pos     = 1;
    private int             m_next    = 0;
    private Token1          m_current = new Token1();

    public static final int EOI       = -1;
    public static final int WS        = 1;
    public static final int NL        = 2;
    public static final int CPP_COM   = 3;
    public static final int ID        = 4;
    public static final int FLAOT     = 5;
    public static final int INT       = 6;
    public static final int STRING    = 7;
    public static final int CHAR      = 8;
    public static final int COOP      = 9;
    public static final int OP        = 10;

    public GolemLexer(String text, String name) {
        m_name = "null";
        m_input = text;

        setDefaultAction(new ILexerAction() {
            @Override
            public void invoke(GenericMatcher lex) {
                m_pos += m_next;
                m_next = lex.getValue().length();
            }
        });

        addRule("\n", NL, true, new ILexerAction() {
            @Override
            public void invoke(GenericMatcher lex) {
                m_line++;
                m_pos = 0;
            }
        });
        addRule("[ \t\r]+", WS, true, null);
        addRule("\\/\\/[^\n]*", CPP_COM, true, null);
        addRule("[a-zA-Z_][a-zA-Z0-9_]*", ID, false, null);
        addRule("[0-9]+(\\.[0-9]*)([eE][\\+\\-]?[0-9]+)?", FLAOT, false, null);
        addRule("[0-9]+", INT, false, null);
        addRule("^\\\"[^\\\"]*\\\"", STRING, false, null);
        addRule("'.'", CHAR, false, null);
        addRule("[=!<>&\\|][=&\\|]+", COOP, false, null);
        addRule(".", OP, false, null);

        setInput(m_input);
    }

    @Override
    public int next() {
        m_current.type = super.next();
        m_current.line = m_line;
        m_current.pos = m_pos;
        m_current.val = getValue();

        return m_current.type;
    }

    public Token1 tok() {
        return m_current;
    }

    public Token1 ntok() {
        return m_current.clone();
    }

    public void error(String msg) throws ParseException {
        System.err.println("Error(" + m_line + ":" + m_pos + ") : " + msg);
        throw new ParseException(msg);
    }
}
