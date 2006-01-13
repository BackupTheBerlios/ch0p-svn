/*
 * Created on 07.10.2005
 */
package ch0p.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ch0p.datastrutures.Application;
import ch0p.datastrutures.Derivation;
import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.Rule;
import ch0p.datastrutures.Symbol;
import ch0p.datastrutures.Word;
import ch0p.datastrutures.mapping.StringMapper;
import ch0p.util.BigList;


/**
 * ToDo: - wenn das leere wort (epsilon) abgeleitet werden kann geht dependsOn nicht - das vollständige
 * löschen wird nicht berücksichtigt (hinterlassen KEINES zeichens)
 * 
 * @author Tom Gelhausen
 * @version $Id$
 */
public class PermutationReducer {

	/**
	 * Instances of this class represent an occurrence of a certain Symbol within a word (Occurrence[] or
	 * List&lt;Occurrence&gt;). Each occurrence has a history in terms of an Application that caused this
	 * Symbol to occur, and a position within the word, this Symbol was initially written to. (By inserting or
	 * deleting Symbols in front, Symbols can shift further to or from the back.)
	 * 
	 * @author Tom Gelhausen
	 * @version $Id$
	 */
	private static class Occurrence {
		/**
		 * the position within the word this Occurrence was initially written to
		 */
		private int pos;

		/**
		 * the Apllication that cused this Symbol to occur
		 */
		private Rule.Match source;

		private Occurrence sourceOccurrence;
		
		/**
		 * the Symbol this Occurrence is representing
		 */
		private Symbol symbol;

		private int hv = 0;

		public void setPosition(int pos) {
			this.pos = pos;
		}

		public int getPosition() {
			return pos;
		}

		public void setSource(Rule.Match source) {
			this.source = source;
		}

		public Rule.Match getSource() {
			return source;
		}

		public void setSymbol(Symbol symbol) {
			this.symbol = symbol;
		}

		public Symbol getSymbol() {
			return symbol;
		}

		@Override
		public final boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof Occurrence)) return false;
			Occurrence t = (Occurrence) o;
			if (source != t.source) return false;
			if (pos != t.pos) return false;
			if (symbol == t.symbol) return true;
			else {
				if (symbol == null) return false;
				return symbol.equals(t);
			}
		}

		@Override
		public final int hashCode() {
			if (hv == 0) {
				hv = pos ^ source.hashCode() ^ symbol.hashCode();
			}
			return hv;
		}

		@Override
		public String toString() {
			return symbol.toString() + "@" + pos;
		}

		/**
		 * @return Returns the sourceOccurrence.
		 */
		public final Occurrence getSourceOccurrence() {
			return sourceOccurrence;
		}

		/**
		 * @param sourceOccurrence The sourceOccurrence to set.
		 */
		public final void setSourceOccurrence(Occurrence sourceOccurrence) {
			this.sourceOccurrence = sourceOccurrence;
		}
	}

	private StringMapper mapper;

	public PermutationReducer(Grammar g) {
		mapper = StringMapper.getInstance(g);
	}

	public void reduce(BigList in, BigList out) throws IOException {
		long length = in.size();
		long wordnr = 0;

		for (long lineNo = 0; lineNo < length;) {
			Word word = getWord(in, lineNo);
			List<Derivation> allDerivations = new ArrayList<Derivation>();
			SortedSet<Derivation> normalizedDerivations = new TreeSet<Derivation>();
			lineNo = findEquals(lineNo, in, allDerivations);
			for (Derivation d : allDerivations) {
				Derivation soa = normalize(d);
				normalizedDerivations.add(soa);
			}
			int n1 = allDerivations.size();
			int n2 = normalizedDerivations.size();
			System.out.println("Word " + wordnr + ": keeping " + n2 + " of " + n1 + " = "
				+ ((n2 * 100.0f) / n1) + "%");
			wordnr++;
			for (Derivation d : normalizedDerivations) {
				writeOut(out, word, d);
			}
		}
		System.out.println("Total: keeping " + out.size() + " of " + in.size() + " = "
			+ ((out.size() * 100.0f) / in.size()) + "%");
	}

	/**
	 * @param from
	 * @param line
	 * @return
	 * @throws IOException
	 */
	private Word getWord(BigList from, long line) throws IOException {
		String s1 = from.getLine(line);
		Word word = (Word) mapper.toSymbolSequence(s1);
		return word;
	}

	/**
	 * @param a
	 * @return
	 */
	private final Derivation normalize(Derivation d) {
		List<Set<Rule.Match>> topology = getTopology(d);
		List<Rule.Match> list = sequentialize(topology);
		for (int pos = 0; pos<list.size(); pos++) {
//			Application a = list.get(pos);
//			int newPos = getPos(a, d);
			
//			List<Application> previous1 = selectPrevious(d.getList(), a);
//			List<Application> previous2 = selectPrevious(list, a);
			// verschiebung berechnen :-s
		}
		Derivation soa = new Derivation(list);
		return soa;
	}

	protected List<Application> selectPrevious(List<Application> list, Application a) {
		int pos = list.indexOf(a);
		List<Application> result = list.subList(0, pos);
		return result;
	}

	protected int getPos(Rule.Match a, Derivation d) {
		List<Rule.Match> l = d;
		int result = l.indexOf(a);
		return result;
	}

	/**
	 * @param to
	 * @param w
	 * @param soa
	 * @throws IOException
	 */
	private final void writeOut(BigList to, Word w, Derivation soa) throws IOException {
		Word w2 = new Word(w, soa);
		String s2 = mapper.toString(w2);
		to.append(s2);
	}

	/**
	 * applies "a" to the "word".
	 * 
	 * @return the resulting word
	 */
	private final List<Occurrence> apply(List<Occurrence> word, Rule.Match a) {
		Rule r = a.rule;
		int length = word.size() - r.getLeftHandSide().length() + r.getRightHandSide().length();
		int pos = a.leftHandSideStart;
		List<Occurrence> result = new ArrayList<Occurrence>(length);
		int rp = 0;
		for (; rp < pos; rp++) {
			result.add(word.get(rp));
		}
		Occurrence sourceOcc = word.get(rp+1);
		for (int i = 0; rp < pos + r.getRightHandSide().length(); rp++, i++) {
			Occurrence o = new Occurrence();
			o.setSymbol(r.getRightHandSide().get(i));
			o.setSourceOccurrence(sourceOcc);
			o.setPosition(rp);
			o.setSource(a);
			result.add(o);
		}
		for (int sp = pos + r.getLeftHandSide().length(); sp < word.size(); rp++, sp++) {
			result.add(word.get(sp));
		}
		return result;
	}

	/**
	 * @return the subsequence of Occurrences within the word "word" that the Application "a" depends on
	 */
	private final List<Occurrence> dependingSubsequence(List<Occurrence> word, Rule.Match a) {
		int pos = a.leftHandSideStart;
		int end = pos + a.rule.getLeftHandSide().length();
		List<Occurrence> result = word.subList(pos, end);
		return result;
	}

	/**
	 * Ensures the existance of a SortedSet&lt;Application&gt; in the position "pos" in the list "in".
	 */
	private final void ensureCapacity(int size, List<Set<Occurrence>> in) {
		if (in.size() < size) {
			for (int i = in.size(); i < size; i++) {
				in.add(new HashSet<Occurrence>());
			}
		}
	}

	/**
	 * @return the level of the Occurrence "o" in the Topo "in".
	 */
	private final int find(Occurrence o, List<Set<Occurrence>> levels) {
		for (int i = 0; i < levels.size(); i++) {
			if (levels.get(i).contains(o)) return i;
		}
		return levels.size();
	}

	/**
	 * Reads all possible derivations of the word at the position "currentWordIndex" in the BigList "in" and
	 * puts them into the List "derivations".
	 * 
	 * @return the index of the next line containing a different word
	 */
	private final long findEquals(long currentWordIndex, BigList in, List<Derivation> derivations)
		throws IOException {
		String s1 = in.getLine(currentWordIndex);
		Word w1 = (Word) mapper.toSymbolSequence(s1);
		derivations.add(w1.getDerivation());
		long pos = currentWordIndex + 1;
		long length = in.size();
		while (pos < length) {
			Word w2 = getWord(in, pos);
			if (w1.equals(w2)) {
				derivations.add(w2.getDerivation());
			} else {
				return pos;
			}
			pos++;
		}
		return pos;
	}

	/**
	 * @return the maximal level which the Occurrences in "dep" depend on
	 */
	private final int getMaxLevel(List<Occurrence> dep, List<Set<Occurrence>> levels) {
		int max = 0;
		for (Occurrence o : dep) {
			int v = find(o, levels);
			if (v > max) max = v;
		}
		return max;
	}

	/**
	 * Organized the Applications of "d" into a leveled data structure based on their dependance among each
	 * other
	 * 
	 * @return exactly the same Application instances in a leveled data structure
	 */
	private final List<Set<Rule.Match>> getTopology(List<Rule.Match> l) {
		List<Set<Occurrence>> levels = new ArrayList<Set<Occurrence>>();
		Occurrence initialWord = new Occurrence();
		initialWord.setSymbol(l.get(0).rule.getLeftHandSide().get(0));
		initialWord.setSource(null);
		initialWord.setPosition(0);
		ensureCapacity(1, levels);
		levels.get(0).add(initialWord);
		List<Occurrence> currentWord = new ArrayList<Occurrence>();
		currentWord.add(initialWord);
		for (int i = 0; i < l.size(); i++) {
			Rule.Match a = l.get(i);
			List<Occurrence> depSeq = dependingSubsequence(currentWord, a);
			List<Occurrence> newWord = apply(currentWord, a);
			List<Occurrence> prodSeq = producedSubsequence(newWord, a);
			int myLevel = getMaxLevel(depSeq, levels) + 1;
			ensureCapacity(myLevel + 1, levels);
			Set<Occurrence> myLevelSet = levels.get(myLevel);
			myLevelSet.addAll(prodSeq);
			currentWord = newWord;
		}
		List<Set<Rule.Match>> result = new ArrayList<Set<Rule.Match>>();
		for (Set<Occurrence> level : levels) {
			Set<Rule.Match> set = new HashSet<Rule.Match>();
			result.add(set);
			for (Occurrence o : level) {
				set.add(o.getSource());
			}
		}
		return result;
	}

	/**
	 * @return the subsequence of Occurrences within the word "newword" that the Application "a" created
	 */
	private final List<Occurrence> producedSubsequence(List<Occurrence> newword, Rule.Match a) {
		int pos = a.leftHandSideStart;
		int end = pos + a.rule.getRightHandSide().length();
		List<Occurrence> result = newword.subList(pos, end);
		return result;
	}

	/**
	 * Reorganizes the leveled data structure to a sequence
	 * 
	 * @return a sequence containing exactly the same Application instances in a defined order
	 */
	private final List<Rule.Match> sequentialize(List<Set<Rule.Match>> topo) {
		List<Rule.Match> result = new ArrayList<Rule.Match>();
		for (Set<Rule.Match> sa : topo) {
			SortedSet<Rule.Match> sorted = new TreeSet<Rule.Match>();
			sorted.addAll(sa);
			for (Rule.Match a : sorted) {
				result.add(a);
			}
		}
		return result;
	}

}
