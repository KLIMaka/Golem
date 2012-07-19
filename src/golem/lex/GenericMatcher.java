package golem.lex;

import golem.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericMatcher {

    private static class Rule {

        public Pattern      pattern;
        public int          id;
        public String       name;
        public boolean      hidden;
        public ILexerAction action;

        public Rule(Pattern pat, int i, String n, boolean h, ILexerAction act) {
            pattern = pat;
            id = i;
            name = n;
            hidden = h;
            action = act;
        }

        @Override
        public String toString() {
            return name;
        }

        public Object convert(Matcher m) {
            return m.group();
        }
    }

    public static class Context {

        public int          offset;
        public int          end;
        public CharSequence cs;

    }

    public static interface ILexerAction {
        public void invoke(GenericMatcher lex);
    }

    private int                m_id;
    private Rule               m_rule;
    private Matcher            m_value;
    private boolean            m_hide        = true;
    private boolean            m_eoi         = false;
    private ArrayList<Matcher> m_matchers    = new ArrayList<Matcher>();
    private ArrayList<Rule>    m_rules       = new ArrayList<Rule>();
    private ILexerAction       m_defaultAction;

    private Stack<Context>     m_conextStack = new Stack<Context>();
    private Context            m_current     = null;

    public GenericMatcher() {
    }

    public void addRule(String pat, int id, String name, boolean hidden, ILexerAction act) {
        m_rules.add(new Rule(Pattern.compile("^" + pat), id, name, hidden, act));
    }

    public void addRule(Rule r) {
        m_rules.add(r);
    }

    public void setDefaultAction(ILexerAction defaultAction) {
        m_defaultAction = defaultAction;
    }

    public void addContext(String str) {
        m_conextStack.push(m_current);
        m_current = new Context();
        m_current.cs = str;
        m_matchers.clear();
        for (Rule r : m_rules)
            m_matchers.add(r.pattern.matcher(m_current.cs));
        m_current.end = m_current.cs.length();
        m_eoi = false;
    }

    protected void setInput(CharSequence cs) {
        m_matchers.clear();
        for (Rule r : m_rules) {
            m_matchers.add(r.pattern.matcher(cs));
        }
        m_eoi = false;
    }

    public Matcher next(Pattern patt) {

        Matcher m = patt.matcher(m_current.cs);
        m.region(m_current.offset, m_current.end);
        if (m.lookingAt()) {
            m_value = m;
            m_current.offset = m.end();
            m_rule = null;
            if (m_defaultAction != null) {
                m_defaultAction.invoke(this);
            }
            return m;
        } else {
            return null;
        }

    }

    public Matcher next(String patt) {
        return next(Pattern.compile(patt));
    }

    public int next() {

        int type;
        for (;;) {

            if (m_eoi) {
                return -1;
            }

            type = 0;
            Matcher m = null;

            for (; type < m_matchers.size(); type++) {
                m = m_matchers.get(type);
                m.region(m_current.offset, m_current.end);
                boolean success = m.lookingAt();
                if (success) {
                    break;
                }
            }

            if (type == m_matchers.size()) {
                m_eoi = true;
                return -1;
            }

            m_value = m;
            m_current.offset = m.end();
            Rule rule = m_rules.get(type);
            m_rule = rule;

            if (rule.action != null) {
                rule.action.invoke(this);
            }

            if (m_defaultAction != null) {
                m_defaultAction.invoke(this);
            }

            if (rule.hidden && m_hide) {
                continue;
            }

            break;
        }

        return m_id = m_rules.get(type).id;
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

    public Rule getRule() {
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

        lexParser.addRule("(!?)([A-Z_]+)(\\*?)", 1, "ID", false, new ILexerAction() {
            int id = 0;

            @Override
            public void invoke(final GenericMatcher lex) {
                boolean isHidden = lex.getValue(1).equals("!");
                final String ruleName = lex.getValue(2);
                boolean isActive = lex.getValue(3).equals("*");
                lex.next();
                lex.next();
                String val = lex.getValue(1);
                if (ruleName.equals("EXEC")) {
                    dummy.addContext(val);
                    while (dummy.next() != -1) {
                        System.out.print(dummy.getObject());
                    }
                } else {
                    if (isActive) {
                        Rule r = new Rule(Pattern.compile("^" + escape(val)), id++, ruleName, isHidden, null) {
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
                        dummy.addRule(escape(val), id++, ruleName, isHidden, null);
                    }
                }
            }
        });
        lexParser.addRule("[ \t\n\r]+", 2, "WS", true, null);
        lexParser.addRule(":", 3, ":", false, null);
        lexParser.addRule("'([^']+)'", 4, "string", false, null);

        lexParser.addContext(Utils.getFile("b.txt"));

        while (lexParser.next() != -1)
            ;
    }
}
