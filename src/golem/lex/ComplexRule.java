package golem.lex;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class ComplexRule {

	public static class NamedResult {
		public final String name;
		public final Object val;

		public NamedResult(String name, Object val) {
			this.name = name;
			this.val = val;
		}
	}

	public interface Rule {
		Object exec(GenericMatcher matcher);
	}

	public static Rule NAMED(final String name, final Rule rule) {
		return new Rule() {
			@Override
			public Object exec(GenericMatcher matcher) {
				Object m = rule.exec(matcher);
				return m == null ? null : new NamedResult(name, m);
			}
		};
	}

	public static Rule RULE(final String name) {
		return new Rule() {
			@Override
			public Object exec(GenericMatcher matcher) {
				Matcher m = matcher.nextByName(name);
				return m == null ? null : m.group(0);
			}
		};
	}

	public static Rule PATTERN(final String pattern) {
		return new Rule() {
			@Override
			public Object exec(GenericMatcher matcher) {
				Matcher m = matcher.nextByPatt(pattern);
				return m == null ? null : m.group(0);
			}
		};
	}

	public static Rule AND(final Rule... rules) {
		return new Rule() {
			@Override
			public Object exec(GenericMatcher matcher) {
				ArrayList<Object> list = new ArrayList<Object>();
				for (Rule rule : rules) {
					Object ret = rule.exec(matcher);
					if (ret == null)
						return null;
					list.add(ret);
				}
				return list;
			}
		};
	}

	public static Rule OR(final Rule... rules) {
		return new Rule() {
			@Override
			public Object exec(GenericMatcher matcher) {
				for (Rule rule : rules) {
					Object ret = rule.exec(matcher);
					if (ret != null)
						return ret;
				}
				return null;
			}
		};
	}

	public static Rule COUNT(final Rule rule, final int from, final int to) {
		return new Rule() {
			@Override
			public Object exec(GenericMatcher matcher) {
				ArrayList<Object> result = new ArrayList<Object>();
				int i = 0;
				for (;;) {
					Object m = rule.exec(matcher);
					if (m == null) {
						if (from == -1 || i >= from)
							return result;
						return null;
					}
					result.add(m);
					i++;
					if (to != -1 && i == to)
						return result;
				}
			}
		};
	}

	public static Rule STAR(Rule rule) {
		return COUNT(rule, -1, -1);
	}

	public static Rule PLUS(Rule rule) {
		return COUNT(rule, 1, -1);
	}

	public static Rule QM(Rule rule) {
		return COUNT(rule, -1, 1);
	}

}