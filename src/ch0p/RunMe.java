/*
 * Created on 04.10.2005
 * by Tom Gelhausen
 */
package ch0p;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ch0p.datastrutures.Derivation;
import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.Rule;
import ch0p.datastrutures.Word;
import ch0p.datastrutures.XMLGrammarReader;
import ch0p.datastrutures.XMLGrammarReader.IllegalGrammarFileException;
import ch0p.logic.WordGenerator;
import ch0p.logic.WordList;
import ch0p.util.BigList;



public class RunMe {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) {
		System.out.println("Generating language");
		System.out.println("\tfrom\t" + args[0]);
		System.out.println("\tto\t\t" + args[1]);
		System.out.println("\tsorted\t" + args[2]);
		System.out.println("\tpermutations removed\t" + args[3]);
		System.out.println("\tdepth\t" + args[4] + " rule applications ... ");

		try {
			long t1 = System.currentTimeMillis();
			File grammarfile = new File(args[0]);
			File datafile = new File(args[1]);
			File sortedfile = new File(args[2]);
			// File permfile = new File(args[3]);
			int depth = Integer.parseInt(args[4]);
			
			
			Grammar g = XMLGrammarReader.read(grammarfile);
			WordList result = new WordList(datafile, g);

			WordGenerator wg;
			wg = new WordGenerator(g, result);
			Word start = new Word();
			start.append(g.getStartSymbol());
			start.setDerivation(new Derivation(new ArrayList<Rule.Match>(0)));

			System.out.println("Initialization: " + (System.currentTimeMillis() - t1) + "ms.");
			Thread.sleep(2000);

			long t2 = System.currentTimeMillis();
			wg.applyRules(start, depth);
			long t3 = System.currentTimeMillis();
			wg = null;
			Thread.sleep(2000);
			System.out.println("Calculation: " + (t3 - t2) + "ms.");
			BigList sortedList = new BigList(sortedfile);
			Thread.sleep(2000);
			System.out.println("Now sorting...");
			long t4 = System.currentTimeMillis();
			result.sortTo(sortedList);
			long t5 = System.currentTimeMillis();
			System.out.println("Sorting: " + (t5 - t4) + "ms.");
			result = null;
			System.gc();
			Thread.sleep(2000);
//			BigList permList = new BigList(permfile);
//			PermutationReducer pr = new PermutationReducer(g);
//			long t6 = System.currentTimeMillis();
//			pr.reduce(sortedList, permList);
//			long t7 = System.currentTimeMillis();
//			System.out.println("Duplicate removal: " + (t7 - t6) + "ms.");
			sortedList = null;
//			permList = null;
			System.gc();
			Thread.sleep(2000);
			System.out.println("Done.");
		} catch (IOException e) {
			printError(e);
		} catch (IllegalGrammarFileException e) {
			printError(e);
		} catch (InterruptedException e) {
			printError(e);
		}


	}

	private static void printError(Throwable e)
	{
		while (e != null) {
			e.printStackTrace(System.err);
			e = e.getCause();
		}
	}

}
