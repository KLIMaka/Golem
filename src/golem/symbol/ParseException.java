package golem.symbol;

@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
	public ParseException(String msg, Throwable e) {
		super(msg, e);
	}

	public ParseException(String msg) {
		super(msg);
	}
}
