package ch0p.datastrutures;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tom Gelhausen
 */
public class Grammar {
	private Set<Rule> loopingChainRules;
	private boolean loopingChainRulesValid = false;
	private SoftReference<Map<String, Nonterminal>> nonterminalMap = null;
	private List<Nonterminal> nonterminals;
	private Set<SymbolSequence> nullables;
	private boolean nullablesValid = false;
	private List<Rule> rules;
	private SoftReference<Map<SymbolSequence, List<Rule>>> rulesMap = null;
	private Nonterminal startSymbol;
	private SoftReference<Map<String, Terminal>> terminalMap = null;
	private ArrayList<Terminal> terminals;
	
	public Grammar() {
		nonterminals = new ArrayList<Nonterminal>();
		terminals = new ArrayList<Terminal>();
		rules = new ArrayList<Rule>();
	}
	
	/**
	 * Adds a rule to the grammar. Unknown symbols are automatically added to
	 * the terminal and nonterminal symbol sets.
	 * 
	 * @param r
	 *            The rule to be added to the grammar
	 */
	public void add(Rule r) {
		nullablesValid = false;
		loopingChainRulesValid = false;
		scanForNewSymbols(r);
		rules.add(r);
		if (rulesMap != null) {
			Map<SymbolSequence, List<Rule>> m = rulesMap.get();
			if (m != null) {
				SymbolSequence id = r.getLeftHandSide();
				List<Rule> l = m.get(id);
				if (l == null) {
					l = new ArrayList<Rule>();
					m.put(id, l);
				}
				l.add(r);
			}
		}
	}

	/**
	 * Adds a symbol depending on its type either to the terminal or nonterminal
	 * set. If the parameter symbol is nither an instance of the class
	 * <code>Terminal</code> nor of the class <code>Nonterminal</code> an
	 * <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param symbol
	 *            The symbol to be added to the grammar
	 */
	public void add(Symbol s) {
		if (s instanceof Terminal) {
			Terminal t = (Terminal) s;
			terminals.add(t);
			if (terminalMap != null) {
				Map<String, Terminal> m = terminalMap.get();
				if (m != null) {
					m.put(t.getStringRepresentation(), t);
				}
			}
			return;
		}
		if (s instanceof Nonterminal) {
			Nonterminal nt = (Nonterminal) s;
			nonterminals.add(nt);
			if (nonterminalMap != null) {
				Map<String, Nonterminal> m = nonterminalMap.get();
				if (m != null) {
					m.put(nt.getStringRepresentation(), nt);
				}
			}
			return;
		}
		throw new IllegalArgumentException("Unknown Symbol Type");
	}

	/**
	 * Creates the String->Nonterminal mapping data structure.
	 */
	private void createNonterminalMap() {
		Map<String, Nonterminal> m = new HashMap<String, Nonterminal>();
		for (Nonterminal nt : nonterminals) {
			m.put(nt.getStringRepresentation(), nt);
		}
		nonterminalMap = new SoftReference<Map<String, Nonterminal>>(m);
	}

	/**
	 * Creates the Nonterminal->Rules mapping data structure.
	 */
	private void createRuleMap() {
		Map<SymbolSequence, List<Rule>> m = new HashMap<SymbolSequence, List<Rule>>();
		for (Rule r : rules) {
			SymbolSequence id = r.getLeftHandSide();
			List<Rule> l = m.get(id);
			if (l == null) {
				l = new ArrayList<Rule>();
				m.put(id, l);
			}
			l.add(r);
		}
		rulesMap = new SoftReference<Map<SymbolSequence, List<Rule>>>(m);
	}

	/**
	 * Creates the String->Terminal mapping data structure.
	 */
	private void createTerminalMap() {
		Map<String, Terminal> m = new HashMap<String, Terminal>();
		for (Terminal t : terminals) {
			m.put(t.getStringRepresentation(), t);
		}
		terminalMap = new SoftReference<Map<String, Terminal>>(m);
	}

	/**
	 * Searches for infinite loops in the production rules. The "infinity" is to
	 * be understood in terms of not bound to the input word length. The rules
	 * found by this method do not produce any output, only nonterminals.
	 * 
	 * This method needs to be reinvoked if a rule has been added to the
	 * grammar.
	 */
	private void findLoops() {
		Collection<Rule> l1 = new ArrayList<Rule>();

		// add all rules to l1 which only produce nonterminal symbols
		for (Rule r : rules) {
			boolean add = true;
			SymbolSequence rhs = r.getRightHandSide();
			if (rhs.length(false) == 0) {
				add = false;
			} else {
				for (Symbol s : rhs) {
					if (s instanceof Terminal) {
						add = false;
					}
				}
			}
			if (add) {
				l1.add(r);
			}
		}

		// (recursively) select those rules that keep appearing on the right
		// side
		// of any (other) rule
		int count = l1.size();
		for (int i = 0; ((i < rules.size()) && (count > 0)); i++) {
			Collection<Rule> l2 = new ArrayList<Rule>();
			Set<Symbol> rightHandSideSymbols = new HashSet<Symbol>();
			for (Rule r : l1) {
				for (Symbol s : r.getRightHandSide()) {
					rightHandSideSymbols.add(s);
				}
			}
			for (Rule r : l1) {
				SymbolSequence lhs = r.getLeftHandSide();
				if (rightHandSideSymbols.contains(lhs)) {
					l2.add(r);
				}
			}
			l1 = l2;
			count = l1.size();
		}
		loopingChainRules = new HashSet<Rule>();
		loopingChainRules.addAll(l1);
		loopingChainRulesValid = true;
	}

	/**
	 * This method searches all nullable nonterminals in this grammar. A
	 * nonterminal symbol is nullable iff the empty word epsilon can be derived
	 * in an arbitrary amount of steps. 
	 */
	private void findNullables() {
		Set<SymbolSequence> s1 = new HashSet<SymbolSequence>();

		// add all epsilon productions to l1
		for (Rule r : rules) {
			if (r.getRightHandSide().length(false) == 0) {
				s1.add(r.getLeftHandSide());
			}
		}

		int count = s1.size();
		for (int i = 0; ((i < rules.size()) && (count > 0)); i++) {
			Set<SymbolSequence> s2 = new HashSet<SymbolSequence>();
			for (Rule r : rules) {
				SymbolSequence rhs = r.getRightHandSide();
				boolean allContained = true;
				for (Symbol s : rhs) {
					if (!s1.contains(s))
						allContained = false;
				}
				if (allContained) {
					// all right hand side symbols where in the previous
					// nullables list, so everything that is produced by this
					// rule is nullable so this symbol is nullable
					s2.add(r.getLeftHandSide());
				}
			}
			s1 = s2;
			count = s1.size();
		}
		nullables = s1;
		nullablesValid = true;
	}

	public Nonterminal getNonterminal(String s) {
		if (nonterminalMap == null) {
			createNonterminalMap();
		}
		Map<String, Nonterminal> m = nonterminalMap.get();
		if (m == null) {
			createNonterminalMap();
		}
		return m.get(s);
	}

	public List<Nonterminal> getNonterminals() {
		return nonterminals;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public List<Rule> getRules(Nonterminal leftHandSide) {
		if (rulesMap == null) {
			createRuleMap();
		}
		Map<SymbolSequence, List<Rule>> m = rulesMap.get();
		if (m == null) {
			createRuleMap();
		}
		return m.get(leftHandSide);
	}

	public Nonterminal getStartSymbol() {
		return startSymbol;
	}

	public Terminal getTerminal(String s) {
		if (terminalMap == null) {
			createTerminalMap();
		}
		Map<String, Terminal> m = terminalMap.get();
		if (m == null) {
			createTerminalMap();
		}
		return m.get(s);
	}

	public List<Terminal> getTerminals() {
		return terminals;
	}
	
	public List<Symbol> getAlphabet() {
		ArrayList<Symbol> result = new ArrayList<Symbol>(terminals.size()+nonterminals.size());
		result.addAll(terminals);
		result.addAll(nonterminals);
		return result;
	}

	public boolean isLoopingChainRule(Rule r) {
		if (!loopingChainRulesValid) {
			findLoops();
		}
		return loopingChainRules.contains(r);
	}

	public boolean isNullable(Nonterminal s) {
		if (!nullablesValid) {
			findNullables();
		}
		return nullables.contains(s);
	}

	private void scanForNewSymbols(Rule r) {
		for (Symbol s : r.getLeftHandSide()) {
			if ((!nonterminals.contains(s)) && (!terminals.contains(s))) {
				add(s);
			}
		}
		for (Symbol s : r.getRightHandSide()) {
			if ((!nonterminals.contains(s)) && (!terminals.contains(s))) {
				add(s);
			}
		}
	}

	public void setStartSymbol(Nonterminal nt) {
		startSymbol = nt;
	}
}
