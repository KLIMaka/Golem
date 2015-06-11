package golem.lex;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	private Token m_current = new Token();
	private int m_line = 1;
	private int m_pos = 1;
	private boolean m_eoi = false;
	private int m_offset = 0;
	private ArrayList<Matcher> m_matchers = new ArrayList<Matcher>();
	private int m_end;

	public Lexer(CharSequence cs) {

		Pattern ws = Pattern.compile("^[ \t\r]+");
		Pattern nl = Pattern.compile("^\n");
		Pattern cppcom = Pattern.compile("^\\/\\/[^\n]*");
		Pattern id = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
		Pattern float_ = Pattern.compile("^[0-9]+(\\.[0-9]*)([eE][\\+\\-]?[0-9]+)?");
		Pattern int_ = Pattern.compile("^[0-9]+");
		Pattern string = Pattern.compile("^\\\"[^\\\"]*\\\"");
		Pattern char_ = Pattern.compile("^'.'");
		Pattern com_op = Pattern.compile("^[=!<>&\\|][=&\\|]+");
		Pattern any = Pattern.compile("^.");

		m_matchers.add(ws.matcher(cs));
		m_matchers.add(nl.matcher(cs));
		m_matchers.add(cppcom.matcher(cs));
		m_matchers.add(id.matcher(cs));
		m_matchers.add(float_.matcher(cs));
		m_matchers.add(int_.matcher(cs));
		m_matchers.add(string.matcher(cs));
		m_matchers.add(char_.matcher(cs));
		m_matchers.add(com_op.matcher(cs));
		m_matchers.add(any.matcher(cs));

		m_end = cs.length();
	}

	public int next() {

		if (m_eoi) {
			return Token.EOI;
		}

		int type = 0;
		Matcher m = null;

		m_current.pos = m_pos;
		m_current.line = m_line;

		for (; type < Token.EOI; type++) {
			m = m_matchers.get(type);
			m.region(m_offset, m_end);
			boolean success = m.lookingAt();
			if (success) {
				break;
			}
		}

		if (type == Token.EOI) {
			m_eoi = true;
			return type;
		}

		m_pos += m.end() - m_offset;
		m_offset = m.end();

		switch (type) {

		case Token.WS:
		case Token.CPP_COMM:
			return next();

		case Token.NL:
			m_line++;
			m_pos = 1;
			return next();

		default:
			m_current.type = type;
			m_current.val = m.group();
			break;
		}

		return type;
	}

	public boolean eoi() {
		return m_eoi;
	}

	public Token tok() {
		return m_current;
	}

	public Token ntok() {
		return m_current.clone();
	}
}
