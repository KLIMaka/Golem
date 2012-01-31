package golem.lex;

import golem.generator.GenException;
import golem.symbol.ParseException;

public class Token implements Cloneable {

    public static final int WS       = 0;
    public static final int NL       = 1;
    public static final int CPP_COMM = 2;
    public static final int ID       = 3;
    public static final int FLOAT    = 4;
    public static final int INT      = 5;
    public static final int STRING   = 6;
    public static final int CHAR     = 7;
    public static final int COP      = 8;
    public static final int OP       = 9;
    public static final int EOI      = 10;

    public int              line;
    public int              pos;
    public int              type;
    public String           val;

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
    public Token clone() {
        Token tok = new Token();
        tok.line = line;
        tok.pos = pos;
        tok.type = type;
        tok.val = val;

        return tok;
    }
}
