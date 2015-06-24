package golem.symbol.nuds;

import static golem.lex.GolemLexer.*;
import gnu.bytecode.Type;
import golem.generator.Gen;
import golem.generator.GenException;
import golem.lex.GolemLexer;
import golem.parser.Parser;
import golem.symbol.*;
import golem.typesystem.PlainOldTypeResolver;

public class Itself implements Inud, IRvalue, ILvalue {

	public static Itself instance = new Itself();

	@Override
	public Symbol invoke(Symbol self, Parser p) throws ParseException {

		switch (self.token.type()) {
		case ID:
			self.type = self.proto.type;
			self.lval = instance;
			break;
		case CHAR:
			self.type = new PlainOldTypeResolver(Type.charType);
			break;
		case GolemLexer.INT:
			self.type = new PlainOldTypeResolver(Type.intType);
			break;
		case FLOAT:
			self.type = new PlainOldTypeResolver(Type.floatType);
			break;
		case STRING:
			self.type = new PlainOldTypeResolver(Type.javalangStringType);
			break;

		default:
			self.type = new PlainOldTypeResolver(Type.errorType);
			break;
		}

		self.rval = instance;
		return self;
	}

	@Override
	public void invoke(Symbol self, Gen g, boolean genResult) throws GenException {

		try {
			if (genResult) {
				switch (self.token.type()) {
				case ID:
					g.fetch(self.proto);
					break;

				case INT:
					g.integer(Integer.parseInt(self.toString()));
					break;

				case FLOAT:
					g.float_(Float.parseFloat(self.toString()));
					break;

				case STRING:
					g.string(self.toString().substring(1, self.toString().length() - 1));
					break;

				default:
					g.integer(0);
					break;
				}
			}

		} catch (Exception e) {
			g.integer(0);
		}
	}

	@Override
	public void invoke(Symbol self, Gen gen, Symbol val) throws GenException {
		val.invokeRval(gen, true);
		gen.store(self.proto);
	}
}
