package golem.lex;

import golem.generator.GenException;
import golem.symbol.ParseException;

public class Token1 implements Cloneable {

    public int        line;
    public int        pos;
    public int        type;
    public String     val;
    public GolemLexer context;

    @Override
    public Token1 clone() {
        Token1 tok = new Token1();
        tok.line = line;
        tok.pos = pos;
        tok.type = type;
        tok.val = val;
        tok.context = context;

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

}
