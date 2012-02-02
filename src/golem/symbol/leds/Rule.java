package golem.symbol.leds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Iled;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Rule implements Iled, Igen {

    public static Rule instance = new Rule();

    @Override
    public Symbol invoke(Symbol self, Parser p, Symbol left) throws ParseException {

        return null;
    }

    @Override
    public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

    }

}
