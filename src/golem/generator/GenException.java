package golem.generator;

@SuppressWarnings("serial")
public class GenException extends Exception {

	public GenException(String msg) {
		super(msg);
	}

	public GenException(String msg, Throwable e) {
		super(msg, e);
	}

}
