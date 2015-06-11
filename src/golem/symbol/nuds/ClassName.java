package golem.symbol.nuds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

public class ClassName implements Inud, IRvalue {

	public static ClassName instance = new ClassName();

	@Override
	public Symbol invoke(Symbol self, Parser p) throws ParseException {
		self.rval = instance;
		self.type = p.scope().resolveImportExt(self.toString());
		return self;
	}

	@Override
	public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {
	}

}
