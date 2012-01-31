package golem.lex;

public class GolemLexer extends GenericMatcher {

    private String          m_name;
    private CharSequence    m_input;
    private int             m_line  = 1;
    private int             m_pos   = 1;
    private int             m_next  = 0;

    public static final int EOI     = -1;
    public static final int WS      = 1;
    public static final int NL      = 2;
    public static final int CPP_COM = 3;
    public static final int ID      = 4;
    public static final int FLAOT   = 5;
    public static final int INT     = 6;
    public static final int STRING  = 7;
    public static final int CHAR    = 8;
    public static final int COOP    = 9;
    public static final int OP      = 10;

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
}
