package golem.lex;

import static com.google.common.collect.Iterators.filter;
import static golem.lex.ComplexRule.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

import com.google.common.collect.Iterators;

import golem.lex.ComplexRule.*;
import golem.utils.Utils;

public class GenericMatcher {

	private int m_id;
	private MatcherRule m_rule;
	private Matcher m_value;
	private boolean m_hide = true;
	private boolean m_eoi = false;
	private ArrayList<MatcherRule> m_rules = new ArrayList<>();

	private Map<String, MatcherRule> m_rulesByPatt = new HashMap<>();
	private Map<String, MatcherRule> m_rulesByName = new HashMap<>();

	private StringBuilder m_source = new StringBuilder();
	private int m_offset = 0;
	private int m_end = 0;
	private Stack<LexerContext> m_conextStack = new Stack<>();
	private LexerContext m_current = null;

	public GenericMatcher() {
	}

	public MatcherRule addRule(String patt, String name, boolean hidden) {

		MatcherRule r = new MatcherRule(patt, 0, name, hidden);
		r.setSource(m_source);

		MatcherRule rule = m_rulesByName.get(name);
		if (rule == null) {
			r.id = m_rules.size();
			m_rules.add(r);
		} else {
			int idx = m_rules.indexOf(rule);
			r.id = idx;
			m_rules.set(idx, r);
		}
		m_rulesByName.put(name, r);
		m_rulesByPatt.put(patt, r);

		return r;
	}

	public MatcherRule addRule(String patt, boolean hidden) {

		MatcherRule r = new MatcherRule(patt, 0, patt, hidden);
		r.setSource(m_source);

		MatcherRule rule = m_rulesByPatt.get(patt);
		if (rule == null) {
			r.id = m_rules.size();
			m_rules.add(r);
		} else {
			int idx = m_rules.indexOf(rule);
			r.id = idx;
			m_rules.set(idx, r);
		}
		m_rulesByName.put(patt, r);
		m_rulesByPatt.put(patt, r);

		return r;
	}

	public void addRule(MatcherRule r) {
		r.setSource(m_source);

		MatcherRule rule = m_rulesByName.get(r.name);
		if (rule == null) {
			// r.id = m_rules.size();
			m_rules.add(r);
		} else {
			int idx = m_rules.indexOf(rule);
			r.id = idx;
			m_rules.set(idx, r);
		}
		m_rulesByName.put(r.name, r);
		m_rulesByPatt.put(r.patt, r);
	}

	public void addContext(CharSequence cs, String name) {
		m_conextStack.push(m_current);
		m_current = new LexerContext();
		m_source.insert(m_offset, cs);
		m_current.end = m_offset + cs.length();
		m_end += cs.length();
	}

	public Matcher next(String patt, boolean skipHidden) {
		if (skipHidden)
			skipHidden();
		MatcherRule r = new MatcherRule(patt, 0, null, false);
		r.setSource(m_source);
		return exec(Iterators.singletonIterator(r)).matcher;
	}

	protected void updateContext() {
		if (m_offset > m_current.end) {
			m_current = m_conextStack.pop();
			updateContext();
		}
	}

	protected void setOffset(int off) {
		m_offset = off;
		updateContext();
	}

	protected MatcherRule match(Iterator<MatcherRule> rules) {
		while (rules.hasNext()) {
			MatcherRule r = rules.next();
			Matcher m = r.matcher;
			m.region(m_offset, m_end);
			if (m.lookingAt()) {
				return r;
			}
		}
		return null;
	}

	protected MatcherRule exec(Iterator<MatcherRule> rules) {

		int len = 0;
		MatcherRule matched = null;
		MatcherRule rule = null;
		while (rules.hasNext()) {
			rule = rules.next();
			Matcher m = rule.matcher;
			m.region(m_offset, m_end);
			boolean succ = m.lookingAt();
			if (succ && len <= m.group().length()) {
				matched = rule;
				len = m.group().length();
			}
		}

		if (matched == null)
			return null;

		m_value = matched.matcher;
		m_rule = matched;
		setOffset(matched.matcher.end());

		matched.action(this);
		defaultAction();

		return matched;
	}

	protected void defaultAction() {
	}

	public int skipHidden() {
		int skipped = 0;
		while (exec(filter(m_rules.iterator(), MatcherRule.isHidden)) != null) {
			skipped++;
		}
		return skipped;
	}

	public int next() {

		if (m_eoi)
			return -1;

		if (m_hide)
			skipHidden();

		MatcherRule rule = exec(m_rules.iterator());
		if (rule == null) {
			m_eoi = true;
			return -1;
		}

		return m_id = rule.id;
	}

	public boolean eoi() {
		return m_eoi;
	}

	public Matcher nextByPatt(String patt) {
		skipHidden();
		MatcherRule rule = m_rulesByPatt.get(patt);
		if (rule == null) {
			rule = addRule(patt, false);
		}
		rule = exec(Iterators.singletonIterator(rule));

		return rule == null ? null : rule.matcher;
	}

	public Matcher nextByName(String name) {
		skipHidden();
		MatcherRule rule = m_rulesByName.get(name);
		rule = exec(Iterators.singletonIterator(rule));

		return rule == null ? null : rule.matcher;
	}

	public String getValue() {
		return m_value.group();
	}

	public String getValue(int off) {
		return m_value.group(off);
	}

	public Object getObject() {
		return m_rule.convert(m_value);
	}

	public int getId() {
		return m_id;
	}

	public MatcherRule getRule() {
		return m_rule;
	}

	public void setHide(boolean mode) {
		m_hide = mode;
	}

	public boolean getHide() {
		return m_hide;
	}

	public static String escape(String str) {
		return str.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
	}

	public static void main(String[] args) throws IOException {

		final GenericMatcher dummy = new GenericMatcher();

		GenericMatcher lexParser = new GenericMatcher();

		lexParser.addRule(new MatcherRule("(!?)([A-Z_0-9]+)(\\*?)", 1, "ID", false) {
			int id = 0;

			@Override
			public void action(final GenericMatcher lex) {
				boolean isHidden = lex.getValue(1).equals("!");
				final String ruleName = lex.getValue(2);
				boolean isActive = lex.getValue(3).equals("*");
				lex.next();
				lex.next();
				String val = lex.getValue(1);
				if (ruleName.equals("MATCH")) {
					String s = lex.nextByName("string").group(1);
					dummy.addContext(s, "foo");
					String[] parts = val.split(" ");
					for (String part : parts) {
						if (part.startsWith("{")) {
							Matcher m = dummy.nextByPatt(part.substring(1, part.length() - 1));
							if (m == null) {
								System.out.println("fail");
								break;
							}
							System.out.println(m.group());
						} else {
							Matcher m = dummy.nextByName(part);
							if (m == null) {
								System.out.println("fail");
								break;
							}
							System.out.println(m.group());
						}
					}

				} else if (ruleName.equals("EXEC")) {
					dummy.addContext(val, "<EXEC>");
					while (dummy.next() != -1) {
						// System.out.print(dummy.getObject());
						// System.out.print("|");
						System.out.println(dummy.m_id);
					}
				} else {
					if (isActive) {
						MatcherRule r = new MatcherRule(escape(val), id++, ruleName, isHidden) {
							@Override
							public Object convert(Matcher m) {
								try {
									Method me = Convert.class.getMethod(ruleName, Matcher.class);
									return me.invoke(null, m);
								} catch (Exception e) {
									return m.group();
								}
							};
						};
						dummy.addRule(r);
					} else {
						dummy.addRule(escape(val), ruleName, isHidden);
					}
				}
			}
		});
		lexParser.addRule("[ \t\n\r]+", "WS", true);
		lexParser.addRule(":", ":", false);
		lexParser.addRule("'([^']+)'", "string", false);
		lexParser.addRule("\\{(.+)\\}", "PATT", false);

		lexParser.addContext(Utils.getFile("c.txt"), "c.txt");

		while (lexParser.next() != -1)
			;
		GenericMatcher m = new GenericMatcher();
		m.addRule("[ \t\n\r]+", "WS", true);
		m.addRule("[A-Za-z][A-Za-z0-9]*", "ID", false);
		m.addContext("foo(bar:Number, baz:String.out.printf, goo:Boo), bar(adasd:ggg)", "");
		Rule QNAME = AND(NAMED("parts", RULE("ID")), STAR(AND(PATTERN("\\."), NAMED("parts", RULE("ID")))));
		Rule ARG = AND(NAMED("name", RULE("ID")), PATTERN("\\:"), NAMED("type", QNAME));
		Object r = AND(
				NAMED("fname", RULE("ID")),
				PATTERN("\\("),
				QM(
						AND(NAMED("args", ARG),
								STAR(AND(PATTERN(","), NAMED("args", ARG))))),
				PATTERN("\\)")).exec(m);
		Object res = filter1(r);
		System.out.println(res);
	}

	private static Object filter1(Object in) {
		Map<String, Object> map = new HashMap<String, Object>();
		return filter1(in, map);
	}

	@SuppressWarnings("unchecked")
	private static Object filter1(Object in, Map<String, Object> map) {
		if (in instanceof String) {
			return in;
		} else if (in instanceof NamedResult) {
			NamedResult nr = (NamedResult) in;
			map.put(nr.name, filter1(nr.val));
		} else if (in instanceof ArrayList) {
			for (Object i : (ArrayList<Object>) in) {
				if (i instanceof NamedResult) {
					NamedResult nr = (NamedResult) i;
					Object prev = map.get(nr.name);
					if (prev == null) {
						map.put(nr.name, filter1(nr.val));
					} else if (prev instanceof ArrayList) {
						((ArrayList<Object>) prev).add(filter1(nr.val));
					} else {
						ArrayList<Object> arr = new ArrayList<Object>();
						arr.add(prev);
						arr.add(filter1(nr.val));
						map.put(nr.name, arr);
					}
				} else {
					filter1(i, map);
				}
			}
		}
		return map;
	}
}
