package golem.lex;

import golem.generator.GenException;
import golem.symbol.ParseException;

public interface IToken {

	public static IToken EOI = new IToken() {
		@Override
		public int type() {
			return -1;
		}

		@Override
		public String value() {
			return null;
		}

		@Override
		public void error(String msg) throws ParseException {
		}

		@Override
		public void warning(String msg) {
		}

		@Override
		public void genError(String msg) throws GenException {
		}

		@Override
		public void genWarning(String msg) {
		}
	};

	int type();

	String value();

	void error(String msg) throws ParseException;

	void warning(String msg);

	void genError(String msg) throws GenException;

	void genWarning(String msg);
}
