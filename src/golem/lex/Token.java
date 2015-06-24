package golem.lex;

import golem.generator.GenException;
import golem.symbol.ParseException;

public interface Token {

	int type();

	String value();

	void error(String msg) throws ParseException;

	void warning(String msg);

	void genError(String msg) throws GenException;

	void genWarning(String msg);
}
