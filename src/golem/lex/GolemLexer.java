package golem.lex;

import golem.symbol.ParseException;

public class GolemLexer extends GenericMatcher implements ILexer {

	// private String m_name;
	private CharSequence m_input;
	private int m_line = 1;
	private int m_pos = 1;
	private int m_next = 0;
	private GolemToken m_current = new GolemToken();

	public static final int WS = 0;
	public static final int NL = 1;
	public static final int CPP_COMM = 2;
	public static final int ID = 3;
	public static final int FLOAT = 4;
	public static final int INT = 5;
	public static final int STRING = 6;
	public static final int CHAR = 7;
	public static final int COP = 8;
	public static final int OP = 9;
	public static final int EOI = 10;

	public GolemLexer(String text, String name) {
		// m_name = "null";
		m_input = text;

		addRule(new Rule(".", OP, "OP", false));
		addRule(new Rule("\n", NL, "NL", true) {
			@Override
			public void action(GenericMatcher lex) {
				m_line++;
				m_pos = 0;
			}
		});
		addRule(new Rule("[ \t\r]+", WS, "WS", true));
		addRule(new Rule("\\/\\/[^\n]*", CPP_COMM, "CPP_COM", true));
		addRule(new Rule("[a-zA-Z_][a-zA-Z0-9_]*", ID, "ID", false));
		addRule(new Rule("[0-9]+(\\.[0-9]*)([eE][\\+\\-]?[0-9]+)?", FLOAT, "FLOAT", false));
		addRule(new Rule("[0-9]+", INT, "INT", false));
		addRule(new Rule("^\\\"[^\\\"]*\\\"", STRING, "STRING", false));
		addRule(new Rule("'.'", CHAR, "CHAR", false));
		addRule(new Rule("[=!<>&\\|][=&\\|]+", COP, "COOP", false));

		addContext(m_input, name);
	}

	@Override
	protected void defaultAction() {
		m_pos += m_next;
		m_next = getValue().length();
	}

	@Override
	public int next() {
		m_current.type = super.next();
		if (m_current.type != -1) {
			m_current.line = m_line;
			m_current.pos = m_pos;
			m_current.val = getValue();
		}

		return m_current.type;
	}

	@Override
	public GolemToken tok() {
		return m_current;
	}

	@Override
	public GolemToken ntok() {
		return m_current.clone();
	}

	public void error(String msg) throws ParseException {
		System.err.println("Error(" + m_line + ":" + m_pos + ") : " + msg);
		throw new ParseException(msg);
	}
}
