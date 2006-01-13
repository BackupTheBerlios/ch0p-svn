/*
 * Created on 04.10.2005
 */
package ch0p.datastrutures.mapping;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ch0p.Const;
import ch0p.datastrutures.AnyContext;
import ch0p.datastrutures.Context;
import ch0p.datastrutures.Derivation;
import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.Nonterminal;
import ch0p.datastrutures.Rule;
import ch0p.datastrutures.Symbol;
import ch0p.datastrutures.SymbolSequence;
import ch0p.datastrutures.Terminal;
import ch0p.datastrutures.Word;

/**
 * @author Tom Gelhausen
 */
public class StringMapper {

	private static SoftReference<Map<Grammar, SoftReference<StringMapper>>> cache;

	private List<Nonterminal> nonterminals;

	private List<Terminal> terminals;

	private List<Rule> rules;

	protected StringMapper(Grammar g) {
		nonterminals = new ArrayList<Nonterminal>();
		for (Nonterminal nt : g.getNonterminals()) {
			nonterminals.add(nt);
		}
		terminals = new ArrayList<Terminal>();
		for (Terminal t : g.getTerminals()) {
			terminals.add(t);
		}
		rules = new ArrayList<Rule>();
		for (Rule r : g.getRules()) {
			rules.add(r);
		}
	}

	public static StringMapper getInstance(Grammar g) {
		if (cache == null) {
			cache = new SoftReference<Map<Grammar, SoftReference<StringMapper>>>(
					new HashMap<Grammar, SoftReference<StringMapper>>());
		}
		Map<Grammar, SoftReference<StringMapper>> map = cache.get();
		if (map == null) {
			map = new HashMap<Grammar, SoftReference<StringMapper>>();
			cache = new SoftReference<Map<Grammar, SoftReference<StringMapper>>>(
					map);
		}
		SoftReference<StringMapper> entry = map.get(g);
		if (entry == null) {
			entry = new SoftReference<StringMapper>(new StringMapper(g));
			map.put(g, entry);
		}
		StringMapper sm = entry.get();
		if (sm == null) {
			sm = new StringMapper(g);
			entry = new SoftReference<StringMapper>(sm);
			map.put(g, entry);
		}
		return sm;
	}

	public int getId(Rule r) {
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).equals(r))
				return i;
		}
		return -1;
	}

	public Rule getRule(int id) {
		return rules.get(id);
	}

	public int getId(Nonterminal nt) {
		for (int i = 0; i < nonterminals.size(); i++) {
			if (nonterminals.get(i).equals(nt))
				return i;
		}
		return -1;
	}

	public Nonterminal getNonterminal(int id) {
		return nonterminals.get(id);
	}

	public int getId(Terminal t) {
		for (int i = 0; i < terminals.size(); i++) {
			if (terminals.get(i).equals(t))
				return i;
		}
		return -1;
	}

	public Terminal getTerminal(int id) {
		return terminals.get(id);
	}

	public void append(StringBuilder sb, Terminal t) {
		sb.append('T');
		int id = getId(t);
		String hex = Integer.toHexString(id);
		sb.append(hex);
	}

	public Terminal getTerminal(String s) {
		String hex = s.substring(1);
		int num = Integer.parseInt(hex, 16);
		Terminal result = getTerminal(num);
		return result;
	}

	public void append(StringBuilder sb, Nonterminal nt) {
		sb.append('N');
		int id = getId(nt);
		String hex = Integer.toHexString(id);
		sb.append(hex);
	}

	public Nonterminal getNonterminal(String s) {
		String hex = s.substring(1);
		int num = Integer.parseInt(hex, 16);
		Nonterminal result = getNonterminal(num);
		return result;
	}

	public void append(StringBuffer sb, Rule r) {
		sb.append('R');
		int id = getId(r);
		String hex = Integer.toHexString(id);
		sb.append(hex);
	}

	public Rule getRule(String s) {
		String hex = s.substring(1);
		int num = Integer.parseInt(hex, 16);
		Rule result = getRule(num);
		return result;
	}

	public void append(StringBuilder sb, Rule.Match a) {
		sb.append('A');
		int id = getId(a.rule);
		String hex = Integer.toHexString(id);
		sb.append(hex);
		sb.append(Const.TUPEL_SEPARATOR);
		appendInt(sb, a.left.start);
		sb.append(Const.TUPEL_SEPARATOR);
		appendInt(sb, a.left.end);
		sb.append(Const.TUPEL_SEPARATOR);
		hex = Integer.toHexString(a.leftHandSideStart);
		sb.append(hex);
		sb.append(Const.TUPEL_SEPARATOR);
		appendInt(sb, a.right.start);
		sb.append(Const.TUPEL_SEPARATOR);
		appendInt(sb, a.right.end);
	}
	
	private void appendInt(StringBuilder sb, int i) {
		String hex = Integer.toHexString(Math.abs(i));
		if (i<0) {
			sb.append('-');
		}
		sb.append(hex);
	}

	public Rule.Match getApplication(String s) {
		int p = s.indexOf(Const.TUPEL_SEPARATOR);
		String i1 = s.substring(1, p);
		String i2 = s.substring(p + Const.TUPEL_SEPARATOR.length(), s.length());
		int rid = Integer.parseInt(i1, 16);
		Rule r = getRule(rid);

		StringTokenizer st = new StringTokenizer(i2, Const.TUPEL_SEPARATOR);

		String t1 = st.nextToken();
		String t2 = st.nextToken();
		Context.Match lC = getContextMatch(r.getLeftContext(), t1, t2);
		String t = st.nextToken();
		int leftHandSideStart = Integer.parseInt(t, 16);
		t1 = st.nextToken();
		t2 = st.nextToken();
		Context.Match rC = getContextMatch(r.getRightContext(), t1, t2);
		Rule.Match result = r.new Match(lC, leftHandSideStart, rC);
		return result;
	}
	
	private Context.Match getContextMatch(Context c, String token1, String token2) {
		int start = Integer.parseInt(token1, 16);
		int end = Integer.parseInt(token2, 16);
		Context.Match result;
		if (start == AnyContext.MATCHVAL
				&& end == AnyContext.MATCHVAL) {
			result = AnyContext.anywhere;
		} else {
			result = c.new Match(start,
					end);
		}
		return result;
	}

	public String toString(SymbolSequence word) {
		StringBuilder result = new StringBuilder();
		for (Symbol s : word) {
			if (s instanceof Terminal) {
				append(result, (Terminal) s);
			} else {
				append(result, (Nonterminal) s);
			}
			result.append(Const.LIST_SEPARATOR);
		}
		if (word instanceof Word) {
			Word w = (Word) word;
			Derivation d = w.getDerivation();
			for (Rule.Match a : d) {
				append(result, a);
				result.append(Const.LIST_SEPARATOR);
			}
		}
		if (result.length() > 0) {
			// we wrote some characters so we have a separator to cut off
			result.setLength(result.length() - Const.LIST_SEPARATOR.length());
		}
		return result.toString();
	}

	public SymbolSequence toSymbolSequence(String str) {
		SymbolSequence result = new SymbolSequence();
		ArrayList<Rule.Match> applications = null;
		String[] sa = str.split(Const.LIST_SEPARATOR);
		for (String token : sa) {
			if (token.charAt(0) == 'T') {
				Symbol s = getTerminal(token);
				result.append(s);
			} else if (token.charAt(0) == 'N') {
				Symbol s = getNonterminal(token);
				result.append(s);
			} else if (token.charAt(0) == 'A') {
				if (applications == null)
					applications = new ArrayList<Rule.Match>();
				Rule.Match a = getApplication(token);
				applications.add(a);
			} else {
				throw new IllegalArgumentException();
			}
		}
		if (applications != null) {
			Derivation d = new Derivation(applications);
			Word newResult = new Word(result, d);
			result = newResult;
		}
		return result;
	}

}
