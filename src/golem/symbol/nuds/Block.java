package golem.symbol.nuds;

import golem.generator.Gen;
import golem.generator.GenException;
import golem.parser.Parser;
import golem.symbol.IRvalue;
import golem.symbol.Inud;
import golem.symbol.ParseException;
import golem.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class Block implements Inud, IRvalue {

	public static Block instance = new Block();

	@Override
	public Symbol invoke(Symbol self, Parser p) throws ParseException {

		ArrayList<Symbol> lst = new ArrayList<Symbol>();
		p.pushScope();

		while (!p.current().token.val.equals("}")) {
			lst.add(p.expression(0));
			p.advanceSoft(";");
		}
		p.advance(); // '}'

		p.popScope();
		self.first = lst;
		self.type = lst.get(lst.size() - 1).type;
		self.rval = instance;

		return self;
	}

	@Override
	public void invoke(Symbol self, Gen g, boolean genReresult) throws GenException {

		@SuppressWarnings("unchecked")
		List<Symbol> lst = (List<Symbol>) self.first;
		int size = lst.size();
		for (Symbol smb : lst) {
			size--;
			if (size == 0 && genReresult) {
				smb.invokeRval(g, true);
			} else {
				smb.invokeRval(g, false);
			}
		}
	}

}
