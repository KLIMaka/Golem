package golem.parser;

import gnu.bytecode.ArrayClassLoader;
import golem.lex.ILexer;
import golem.lex.Token;
import golem.symbol.Iled;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;
import golem.symbol.leds.Assign;
import golem.symbol.leds.Bin;
import golem.symbol.leds.Call;
import golem.symbol.leds.Member;
import golem.symbol.nuds.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parser {

	private ILexer m_lex;
	private Symbol m_current = new Symbol();
	private Map<String, Symbol> m_symbols = new HashMap<String, Symbol>();
	private Scope m_scope = new Scope();
	private ArrayClassLoader m_classLoader = new ArrayClassLoader();

	protected Symbol symbol(String smb, int bp) {

		Symbol ref = m_symbols.get(smb);
		if (ref == null) {
			ref = new Symbol();
			m_symbols.put(smb, ref);
		}

		if (bp > ref.lbp) {
			ref.lbp = bp;
		}

		return ref;
	}

	protected Symbol symbol(String smb) {
		return symbol(smb, 0);
	}

	protected Symbol infix(String smb, int bp, Iled led) {

		Symbol ref = symbol(smb, bp);
		ref.led = led;
		return ref;
	}

	protected Symbol prefix(String smb, Inud nud) {

		Symbol ref = symbol(smb);
		ref.nud = nud;
		return ref;
	}

	public Parser(ILexer lex) throws ParseException {

		m_lex = lex;
		m_current.token = m_lex.tok();
		m_current.scope = m_scope;
		m_scope.addImport("java.lang.*");

		symbol("{literal}").nud = Itself.instance;
		symbol(")");
		symbol(",");
		symbol("}");
		symbol("else");
		symbol(";");

		infix("+", 50, Bin.instance);
		infix("-", 50, Bin.instance);
		infix("*", 60, Bin.instance);
		infix("/", 60, Bin.instance);
		infix("=", 10, Assign.instance);
		infix(".", 80, Member.instance);
		infix("(", 80, Call.instance);

		prefix("(", Parentheses.instance);
		prefix("{", Block.instance);
		prefix("def", Def.instance);
		prefix("if", If_.instance);
		prefix("while", While_.instance);
		prefix("typeof", Typeof.instance);
		prefix("import", Import.instance);
		prefix("new", New.instance);
		prefix("null", Null.instance);
		prefix("true", True.instance);
		prefix("false", False.instance);

		advance();
	}

	public void advanceSoft(String expected) {

		if (expected != null && !expected.equals(m_lex.tok().value())) {
			m_lex.tok().warning("Expecting '" + expected + "'");
			return;
		}

		m_lex.next();
		resolveSymbol();
	}

	public void advance(String expected) {

		if (expected != null && !expected.equals(m_lex.tok().value())) {
			m_lex.tok().error("Expecting '" + expected + "'");
			return;
		}

		m_lex.next();
		resolveSymbol();
	}

	public void advance() throws ParseException {
		advance(null);
	}

	public void resolveSymbol() {

		switch (m_lex.tok().type()) {

		case Token.INT:
		case Token.FLOAT:
		case Token.CHAR:
		case Token.STRING:
			updateCurrent(m_symbols.get("{literal}"));
			break;

		case Token.ID: {
			Symbol smb = m_symbols.get(m_lex.tok().value());
			if (smb == null) {
				smb = m_scope.find(m_lex.tok().value());
			}
			updateCurrent(smb);
			break;
		}

		case Token.COP:
		case Token.OP: {
			Symbol smb = m_symbols.get(m_lex.tok().value());
			if (smb == null) {
				m_lex.tok().error("Unexpected token.");
				return;
			}
			updateCurrent(smb);
			break;
		}

		default:
			break;
		}
	}

	private void updateCurrent(Symbol proto) {

		if (proto != null) {
			m_current.lbp = proto.lbp;
			m_current.nud = proto.nud;
			m_current.led = proto.led;
			m_current.rval = proto.rval;
			m_current.lval = proto.lval;
			m_current.proto = proto;
		}
	}

	public Scope scope() {
		return m_scope;
	}

	public void pushScope() {
		Scope nscope = new Scope();
		nscope.setParent(m_scope);
		m_scope = nscope;
	}

	public void popScope() {
		m_scope = m_scope.getParent();
	}

	public Symbol current() {
		return m_current;
	}

	public Symbol ncurrent() {

		Symbol smb = m_current.clone();
		smb.token = m_lex.ntok();
		return smb;
	}

	public boolean eoi() {
		return m_lex.eoi();
	}

	public Symbol expression(int rbp) throws ParseException {

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

	public Symbol program() throws ParseException {

		try {
			ArrayList<Symbol> prog = new ArrayList<Symbol>();
			while (!m_lex.eoi()) {
				prog.add(expression(0));
				advanceSoft(";");
			}

			Symbol p = new Symbol();
			p.first = prog;
			p.rval = Block.instance;
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			m_current.token.error("Java exception: " + e);
		}
		return null;
	}

	public ArrayClassLoader getClassLoader() {
		return m_classLoader;
	}
}
