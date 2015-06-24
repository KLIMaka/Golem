package golem.lex;

public interface Lexer {

	int next();

	boolean eoi();

	Token tok();

	Token ntok();
}
