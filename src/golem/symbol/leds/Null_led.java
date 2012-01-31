package golem.symbol.leds;

import golem.parser.Parser;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Null_led implements Iled {

    static public Null_led instance = new Null_led();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        self.token.error("Operator expected.");
        return null;
    }

}
