package golem.symbol;

import golem.generator.Gen;
import golem.generator.GenException;

public interface ILvalue {

	public void invoke(Symbol self, Gen gen, Symbol val) throws GenException;

}
