package golem.lex;

public interface ILexer {

	int next();

	boolean eoi();

	IToken tok();

	IToken ntok();
}
