package golem.lex;

import golem.generator.GenException;
import golem.symbol.ParseException;

public class GolemToken implements Cloneable, IToken {

	public int line;
	public int pos;
	public int type;
	public String val;

	@Override
	public GolemToken clone() {
		GolemToken tok = new GolemToken();
		tok.line = line;
		tok.pos = pos;
		tok.type = type;
		tok.val = val;

		return tok;
	}

	public void error(String msg) throws ParseException {
		System.err.println("Error(" + line + ":" + pos + ") : " + msg);
		throw new ParseException(msg);
	}

	public void warning(String msg) {
		System.out.println("Warning(" + line + ":" + pos + ") : " + msg);
	}

	public void genError(String msg) throws GenException {
		System.err.println("Codegen error(" + line + ":" + pos + ") : " + msg);
		throw new GenException(msg);
	}

	public void genWarning(String msg) {
		System.out.println("Codegen warning(" + line + ":" + pos + ") : " + msg);
	}

	@Override
	public int type() {
		return type;
	}

	@Override
	public String value() {
		return val;
	}

}
