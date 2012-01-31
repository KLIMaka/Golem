package golem.lex;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericLexer {

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

    public static interface ILexerAction {
        public void invoke(GenericLexer lex);
    }

    private int                m_offset;
    private int                m_end;
    private String             m_value;
    private boolean            m_eoi      = false;
    private ArrayList<Matcher> m_matchers = new ArrayList<Matcher>();
    private ArrayList<Rule>    m_rules    = new ArrayList<Rule>();
    private ILexerAction       m_defaultAction;

    public GenericLexer() {
    }

    public void addRule(String pat, int id, boolean hidden, ILexerAction act) {
        m_rules.add(new Rule(Pattern.compile("^" + pat), id, hidden, act));
    }

    public void setDefaultAction(ILexerAction defaultAction) {
        m_defaultAction = defaultAction;
    }

    public void setInput(CharSequence cs) {
        m_matchers.clear();
        for (Rule r : m_rules) {
            m_matchers.add(r.pattern.matcher(cs));
        }
        m_end = cs.length();
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
                m.region(m_offset, m_end);
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
            m_offset = m.end();
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

        return m_rules.get(type).id;
    }

    public String getValue() {
        return m_value;
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

    public static void main(String[] args) {
        GenericLexer gl = new GenericLexer();
        final PositionTracker pt = new PositionTracker();
        gl.addRule("\n", 4, true, new ILexerAction() {
            @Override
            public void invoke(GenericLexer lex) {
                pt.newLine();
            }
        });

        gl.addRule("[ \t\r]+", 0, true, null);
        gl.addRule("[a-z_][a-z0-9_]*", 1, false, null);
        gl.addRule("/\\*(?:.|[\\n\\r])*?\\*/", 3, true, null);
        gl.addRule(".", 2, false, null);
        gl.setDefaultAction(new ILexerAction() {
            @Override
            public void invoke(GenericLexer lex) {
                pt.advance(lex.getValue().length());
            }
        });

        gl.setInput("foo\n bar baz\n bs2af\n /*11*/ gg\n asdas\n s23232");
        while (gl.next() != -1) {
            System.out.println(pt.line + ":" + pt.pos + " " + gl.getValue());
        }
    }
}
