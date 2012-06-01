package golem.lex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericMatcher {

    private static class Rule {

        public Pattern      pattern;
        public int          id;
        public boolean      hidden;
        public ILexerAction action;

        public Rule(Pattern pat, int i, boolean h, ILexerAction act) {
            pattern = pat;
            id = i;
            hidden = h;
            action = act;
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
    private String             m_value;
    private boolean            m_eoi         = false;
    private ArrayList<Matcher> m_matchers    = new ArrayList<Matcher>();
    private ArrayList<Rule>    m_rules       = new ArrayList<Rule>();
    private ILexerAction       m_defaultAction;

    private Stack<Context>     m_conextStack = new Stack<Context>();
    private Context            m_current     = null;

    public GenericMatcher() {}

    public void addRule(String pat, int id, boolean hidden, ILexerAction act) {
        m_rules.add(new Rule(Pattern.compile("^" + pat), id, hidden, act));
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

    public void setInput(CharSequence cs) {
        m_matchers.clear();
        for (Rule r : m_rules) {
            m_matchers.add(r.pattern.matcher(cs));
        }
        m_eoi = false;
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

            m_value = m.group();
            m_current.offset = m.end();
            Rule rule = m_rules.get(type);

            if (rule.action != null) {
                rule.action.invoke(this);
            }

            if (m_defaultAction != null) {
                m_defaultAction.invoke(this);
            }

            if (rule.hidden) {
                continue;
            }

            break;
        }

        return m_id = m_rules.get(type).id;
    }

    public String getValue() {
        return m_value;
    }

    public int getId() {
        return m_id;
    }

    static class PositionTracker {
        public int  line = 1;
        public int  pos  = 1;
        private int next = 0;

        public void newLine() {
            line++;
            pos = 0;
            next = 0;
        }

        public void advance(int i) {
            pos += next;
            next = i;
        }
    }

    public static String escape(String str) {
        return str.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }

    public static void main(String[] args) throws IOException {

        final GenericMatcher dummy = new GenericMatcher();

        GenericMatcher lexParser = new GenericMatcher();
        lexParser.addRule("[A-Z_]+", 0, false, new ILexerAction() {
            int id = 0;

            @Override
            public void invoke(GenericMatcher lex) {
                String val = lex.getValue();
                lex.next();
                lex.next();
                if (val.equals("EXEC")) {

                } else {
                    dummy.addRule(escape(lex.getValue()), id++, false, null);
                }
            }
        });
        lexParser.addRule("[ \t]+", 1, true, null);
        lexParser.addRule(":", 3, false, null);
        lexParser.addRule("'.+'", 4, false, null);
    }
}
