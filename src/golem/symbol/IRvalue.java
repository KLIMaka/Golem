package golem.symbol;

import golem.generator.Gen;
import golem.generator.GenException;

public interface IRvalue {

	public void invoke(Symbol self, Gen g, boolean genResult) throws GenException;
}
