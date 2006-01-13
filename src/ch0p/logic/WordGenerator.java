/*
 * Created on 09.09.2004
 */
package ch0p.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch0p.datastrutures.Derivation;
import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.Rule;
import ch0p.datastrutures.SymbolSequence;
import ch0p.datastrutures.Word;
import ch0p.datastrutures.Rule.NotApplicableException;


/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class WordGenerator {
	/**
	 * <p>
	 * There are two possible extremes: either to only work with a single
	 * disklist or to work with one disklist per descendence step. The
	 * disadvantage of the first is that it uses a huge file with a high
	 * percentage of "dead" data, possibly limiting the capabilities of
	 * sprachbaukasten (disk capacity limit) earlier than necessary. The
	 * disadvantage of the latter is that the amount of small files consumes too
	 * much CPU time handling directory entries, opening and closing files, etc.
	 * </p>
	 * <p>
	 * To covercome both disadvantages, each created disklist is used until it
	 * has at least <code>NEW_DISK_LIST_WATER_MARK</code> lines, but new
	 * disklists are still created regularely. (When the garbage collector
	 * collects a disklist object, the associated file is deleted.)
	 * </p>
	 */
	public static final long NEW_DISK_LIST_WATER_MARK = 50000;

	private int maxWordLength;

	private Grammar g;

	private WordList finalWords; // will keep final words that can not be

	// derived any further

	public WordGenerator(Grammar g, WordList target) {
		this(g, target, Integer.MAX_VALUE);
	}

	public WordGenerator(Grammar g, WordList target, int maxLen) {
		this.g = g;
		this.finalWords = target;
		this.maxWordLength = maxLen;
		// nonfinalWords = new TempWordList(ruleSet);
	}

	public final void applyRules(Word w, int maxDepth) throws IOException {
		applyRules(w, null, maxDepth);
	}

	/**
	 * This method recursively calls applyRules. All words are returned that can
	 * be created starting from Word w and applying rules to it. The recursion
	 * process stops if either no more rules are applicable to any of the
	 * resulting words or ath the latest when the maximum recursion depth is
	 * reached. A call like <code>applyRules(word, 0);</code> is equivalent to
	 * a call to <code>applyRules(word)</code>.
	 * 
	 * @param w
	 *            the start word
	 * @param maxDepth
	 */
	private final void applyRules(Word w, WordList nonfinalWords, int maxDepth)
			throws IOException {
		if ((nonfinalWords == null)
				|| nonfinalWords.size() > NEW_DISK_LIST_WATER_MARK) {
			// too many small files cost too much os/fs overhead, so we recycle
			// the lists
			// and only create new ones, if water mark is reached
			nonfinalWords = new TempWordList(g);
		}
		long start = nonfinalWords.size();
		applyRules(w, nonfinalWords);
		long end = nonfinalWords.size();
		if (maxDepth > 0) {
			for (long i = start; i < end; i++) {
				Word currentWord = (Word) nonfinalWords.get(i);
				applyRules(currentWord, nonfinalWords, maxDepth - 1);
			}
		}
	}

	/**
	 * This method applies all possible rules to all possible occurences. The
	 * order is the following: <br>
	 * <code>for every rule (in declaration order)<br>
	 * &nbsp;&nbsp;for every occurence of the left hand side in the word (from left to right)</code><br>
	 * A call to this function is equivalent to a call like
	 * <code>applyRules(word, 0);</code>
	 */
	private final void applyRules(Word w, WordList nonfinalWords)
			throws IOException {
		Derivation currentDerivation = w.getDerivation();
		for (Rule r : g.getRules()) {
			Collection<Rule.Match> matches = r.getMatches(w);
			for (Rule.Match match : matches) {
				SymbolSequence seq = null;;
				try {
					seq = r.applyTo(w, match);
				} catch (NotApplicableException e) {
					e.printStackTrace();
					throw new IllegalStateException();
				}
				if (seq.length(false) <= maxWordLength) {
					Derivation newDerivation = newDerivation(currentDerivation,
							match);
					Word word = new Word();
					word.append(seq);
					word.setDerivation(newDerivation);
					if (word.containsNonterminals()) {
						nonfinalWords.add(word);
					} else {
						finalWords.add(word);
					}
				}
			}
		}
	}

	private Derivation newDerivation(Derivation d, Rule.Match match) {
		List<Rule.Match> l = new ArrayList<Rule.Match>(d.size() + 1);
		l.addAll(d);
		l.add(match);
		Derivation result = new Derivation(l);
		return result;
	}

}
