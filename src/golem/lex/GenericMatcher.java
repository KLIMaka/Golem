package golem.lex;

import golem.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
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
        public Matcher      matcher;

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

        public void setSource(CharSequence cs) {
            matcher = pattern.matcher(cs);
        }
    }

    public static class Context {

        public int    end;
        public String name;
    }

    public static interface ILexerAction {
        public void invoke(GenericMatcher lex);
    }

    private int             m_id;
    private Rule            m_rule;
    private Matcher         m_value;
    private boolean         m_hide        = true;
    private boolean         m_eoi         = false;
    private ArrayList<Rule> m_rules       = new ArrayList<Rule>();
    private ILexerAction    m_defaultAction;

    private StringBuilder   m_source      = new StringBuilder();
    private int             m_offset      = 0;
    private int             m_end         = 0;
    private Stack<Context>  m_conextStack = new Stack<Context>();
    private Context         m_current     = null;

    public GenericMatcher() {}

    public void addRule(String pat, int id, String name, boolean hidden, ILexerAction act) {
        Rule r = new Rule(Pattern.compile("^" + pat), id, name, hidden, act);
        r.setSource(m_source);
        m_rules.add(r);

    }

    public void addRule(Rule r) {
        r.setSource(m_source);
        m_rules.add(r);
    }

    public void setDefaultAction(ILexerAction defaultAction) {
        m_defaultAction = defaultAction;
    }

    public void addContext(CharSequence cs, String name) {
        m_conextStack.push(m_current);
        m_current = new Context();
        m_source.insert(m_offset, cs);
        m_current.end = m_offset + cs.length();
        m_end += cs.length();
    }

    public Matcher next(Pattern patt) {

        Matcher m = patt.matcher(m_source);
        m.region(m_offset, m_end);
        if (m.lookingAt()) {
            m_value = m;
            m_offset = m.end();
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

    protected void updateContext() {
        if (m_offset > m_current.end) {
            m_current = m_conextStack.pop();
            updateContext();
        }
    }

    protected Rule match(Iterator<Rule> rules) {
        while (rules.hasNext()) {
            Rule r = rules.next();
            Matcher m = r.matcher;
            m.region(m_offset, m_end);
            if (m.lookingAt()) {
                return r;
            }
        }
        return null;
    }

    public int next() {

        Rule rule = null;
        for (;;) {

            if (m_eoi) {
                return -1;
            }

            rule = match(m_rules.iterator());

            if (rule == null) {
                m_eoi = true;
                return -1;
            }

            m_value = rule.matcher;
            m_offset = rule.matcher.end();
            updateContext();
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

        return m_id = rule.id;
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
                    dummy.addContext(val, "<EXEC>");
                    while (dummy.next() != -1) {
                        System.out.print(dummy.getObject());
                        // System.out.print("|");
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

        lexParser.addContext(Utils.getFile("b.txt"), "b.txt");

        while (lexParser.next() != -1);
    }
}
