/*
 * Created on 20.10.2005
 */
package ch0p.datastrutures.parsegraph;

import java.util.ArrayList;
import java.util.List;

import ch0p.datastrutures.AnyContext;
import ch0p.datastrutures.Context;
import ch0p.datastrutures.Derivation;
import ch0p.datastrutures.Rule;
import ch0p.datastrutures.SymbolSequence;

/**
 * @author Tom Gelhausen
 */
public class ParseGraph {
	protected Occurrence root;

	protected SymbolSequence word;

	protected List<Object> compseq = null;

	protected int hash = 0;

	protected ParseGraph() {

	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ParseGraph))
			return false;
		ParseGraph that = (ParseGraph) o;
		if (this.compseq == null)
			this.compseq = this.getCompSeq();
		if (that.compseq == null)
			that.compseq = that.getCompSeq();
		boolean result = equal(this.compseq, that.compseq);
		return result;
	}

	/**
	 * @return Returns the root.
	 */
	public Occurrence getRoot() {
		return root;
	}

	/**
	 * @return Returns the word.
	 */
	public SymbolSequence getWord() {
		return word;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (compseq == null) {
			compseq = getCompSeq();
			int i = 0;
			for (Object o : compseq) {
				hash ^= (o.hashCode() + (i++));
			}
		}
		return hash;
	}

	/**
	 * macht eine Sequenz aus den Daten dieses Graphen, auf deren Grundlage
	 * dieser mit anderen Graphen verglichen wird
	 */
	private List<Object> getCompSeq() {
		List<Object> result = new ArrayList<Object>();
		result.add(root);
		List<Occurrence> currentO = new ArrayList<Occurrence>();
		currentO.add(root);
		List<Application> currentA;
		do {
			currentA = new ArrayList<Application>();
			for (Occurrence o : currentO) {
				currentA.addAll(o.getReaders());
			}
			result.addAll(currentA);
			currentO = new ArrayList<Occurrence>();
			for (Application a : currentA) {
				currentO.addAll(a.getChildren());
			}
		} while (currentA.size() > 0);
		return result;
	}

	public static ParseGraph createFrom(Derivation d) {
		ParseGraph result = new ParseGraph();
		result.root = new Occurrence(d.get(0).rule.getLeftHandSide().get(0));
		List<Occurrence> currentWord = new ArrayList<Occurrence>();
		currentWord.add(result.root);
		for (Rule.Match a : d) {
			List<Occurrence> newWord = apply(currentWord, a);
			currentWord = newWord;
		}
		result.word = getSymbolSequence(currentWord);
		return result;
	}

	private static List<Occurrence> apply(List<Occurrence> word,
			Rule.Match match) {
		SymbolSequence leftHandSide = match.rule.getLeftHandSide();
		SymbolSequence rightHandSide = match.rule.getRightHandSide();
		Context.Match leftContextMatch = match.left;
		Context.Match rightContextMatch = match.right;
		int newWordLength = word.size() - leftHandSide.length()
				+ rightHandSide.length();
		int pos = match.leftHandSideStart;
		List<Occurrence> result = new ArrayList<Occurrence>(newWordLength);
		Application newApp = new Application();
		newApp.parents = new ArrayList<Occurrence>(leftHandSide.length());
		newApp.children = new ArrayList<Occurrence>(rightHandSide.length());
		newApp.rule = match.rule;
		if (leftContextMatch == AnyContext.anywhere || leftContextMatch==null) {
			for (int i = 0; i < pos; i++) {
				Occurrence o = word.get(i);
				result.add(o);
			}
		} else {
			throw new IllegalStateException("not yet implementet");
		}
		for (int i = pos; i < pos + leftHandSide.length(); i++) {
			Occurrence o = word.get(i);
			o.addReader(newApp);
			newApp.parents.add(o);
		}
		for (int i = 0; i < rightHandSide.length(); i++) {
			Occurrence o = new Occurrence(rightHandSide.get(i));
			result.add(o);
			o.setSource(newApp);
			newApp.children.add(o);
		}
		if (rightContextMatch == AnyContext.anywhere || rightContextMatch==null) {
			for (int i = pos + leftHandSide.length(); i < word.size(); i++) {
				Occurrence o = word.get(i);
				result.add(o);
			}
		} else {
			throw new IllegalStateException("not yet implementet");
		}
		return result;
	}

	private static boolean equal(List<Object> l1, List<Object> l2) {
		if (l1.size() != l2.size())
			return false;
		for (int i = 0; i < l1.size(); i++) {
			Object o1 = l1.get(i);
			Object o2 = l2.get(i);
			if (o1.getClass() != o2.getClass())
				return false;
			if (o1 instanceof Occurrence) {
				Occurrence occ1 = (Occurrence) o1;
				Occurrence occ2 = (Occurrence) o2;
				if (!occ1.getSymbol().equals(occ2.getSymbol()))
					return false;
			} else if (o1 instanceof Application) {
				Application a1 = (Application) o1;
				Application a2 = (Application) o2;
				if (!a1.getRule().equals(a2.getRule()))
					return false;
			} else {
				assert false;
			}
		}
		return true;
	}

	private static SymbolSequence getSymbolSequence(List<Occurrence> word) {
		SymbolSequence result = new SymbolSequence();
		for (Occurrence o : word) {
			result.append(o.getSymbol());
		}
		return result;
	}
}
