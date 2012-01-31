package golem.symbol.nuds;

import gnu.bytecode.CodeAttr;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.Igen;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class Null implements Igen, Inud {

	public static Null instance = new Null();

	@Override
	public Symbol invoke(Symbol self, Parser p) throws ParseException {

		self.type = Type.nullType;
		self.gen = instance;
		return self;
	}

	@Override
	public void invoke(Symbol self, Gen g, boolean genResult)
			throws GenException {

		CodeAttr code = g.getLocation();
		if (genResult) {
			code.emitPushNull();
		}
	}

}
