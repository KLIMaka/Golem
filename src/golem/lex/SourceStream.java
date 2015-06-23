package golem.lex;

import java.util.Stack;
import java.util.regex.Matcher;

public class SourceStream {

	public static class Context {

		public int start;
		public int end;
		public String name;

	}

	private StringBuilder m_source = new StringBuilder();
	private Stack<Context> m_contexts = new Stack<SourceStream.Context>();
	private int m_offset = 0;
	private int m_size = 0;

	public SourceStream() {
	}

	public void addContext(CharSequence cs, String name, int m_offset) {
		m_source.insert(m_offset, cs);
		Context ctx = new Context();
		ctx.start = m_offset;
		ctx.end = m_offset + cs.length();
		ctx.name = name;
		m_contexts.push(ctx);
		m_size += cs.length();
	}

	protected void updateContext() {
		if (m_contexts.peek().end < m_offset) {
			m_contexts.pop();
			updateContext();
		}
	}

	public void updateOffset(int off) {
		m_offset = off;
	}

	public String getName() {
		return m_contexts.peek().name;
	}

	public boolean match(Matcher m) {
		m.region(m_offset, m_size);
		return m.lookingAt();
	}

}
