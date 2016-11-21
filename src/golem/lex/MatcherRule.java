package golem.lex;

import java.util.regex.*;

import com.google.common.base.Predicate;

public class MatcherRule {

	public Pattern pattern;
	public String patt;
	public int id;
	public String name;
	public boolean hidden;
	public Matcher matcher;

	public MatcherRule(String patt, int i, String n, boolean h) {
		this.patt = patt;
		pattern = Pattern.compile("^" + patt);
		id = i;
		name = n;
		hidden = h;
	}

	@Override
	public String toString() {
		return name;
	}

	public Object convert(Matcher m) {
		return m.group();
	}

	public void action(GenericMatcher lex) {
	}

	public void setSource(CharSequence cs) {
		matcher = pattern.matcher(cs);
	}

	public static Predicate<MatcherRule> isHidden = new Predicate<MatcherRule>() {
		@Override
		public boolean apply(MatcherRule r) {
			return r.hidden;
		}
	};
}