/*
 * Created on 30.11.2005
 */
package ch0p.regex.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.Symbol;
import ch0p.datastrutures.SymbolSequence;
import ch0p.datastrutures.Terminal;
import ch0p.datastrutures.XMLGrammarReader.IllegalGrammarFileException;
import ch0p.regex.Acceptor;
import ch0p.regex.Pattern;
import ch0p.regex.XMLRegExReader;

/**
 * @author Tom Gelhausen
 */
public class Test {

	/**
	 * @param args
	 * @throws IllegalGrammarFileException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void main(String[] args) throws IllegalGrammarFileException, ParserConfigurationException,
		SAXException, IOException {
		Grammar g = new Grammar();
		g.add(new Terminal("a"));
		g.add(new Terminal("b"));
		g.add(new Terminal("c"));
		g.add(new Terminal("x"));
		g.add(new Terminal("y"));
		g.add(new Terminal("z"));

		File expressionsF = new File("expressions.xml");
		File patternsF = new File("patterns.xml");

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document expdoc = docBuilder.parse(expressionsF);
		Document patdoc = docBuilder.parse(patternsF);

		// normalize text representation
		expdoc.getDocumentElement().normalize();
		patdoc.getDocumentElement().normalize();

		NodeList expNL = expdoc.getDocumentElement().getElementsByTagName("expr");
		NodeList patNL = patdoc.getDocumentElement().getElementsByTagName("regex");

		XMLRegExReader xrer = new XMLRegExReader(g);

		List<Acceptor> acceptors = getAutomatons(patNL, g, xrer);
		List<SymbolSequence> expressions = getExpressions(expNL, g);
		System.out.println();
		System.out.println();
		System.out.println();
		
		int maxSSlen = 1;
		for (SymbolSequence ss:expressions) {
			int l = ss.toString().length();
			if (l>maxSSlen) maxSSlen = l;
		}

		int[] aLength = new int[acceptors.size()];
		int sumALength = 0;
		int z=0;
		for (Acceptor a : acceptors) {
			aLength[z] = a.getPattern().toString().length();
			sumALength += aLength[z] ;
			z++;
		}
		
		String separator = "   ";
		
		System.out.print(format("", maxSSlen, true));
		z=0;
		for (Acceptor a : acceptors) {
			System.out.print(separator);
			System.out.print(format(a.getPattern().toString(), aLength[z], true));
			z++;
		}
		System.out.println();
		
		for (int i=0; i<(maxSSlen+sumALength+separator.length()*acceptors.size()); i++) {
			System.out.print('-');
		}
		System.out.println();

		for (SymbolSequence ss:expressions) {
			System.out.print(format(ss.toString(),maxSSlen,true));
			int x=0;
			for (Acceptor a : acceptors) {
				String resultStr;
				System.out.print(separator);
				boolean result = a.accepts(ss);
				if (result) resultStr = "yes";
				else resultStr="no";
				System.out.print(format(resultStr, aLength[x], true));
				x++;
			}
			System.out.println();
		}

		for (int i=0; i<(maxSSlen+sumALength+separator.length()*acceptors.size()); i++) {
			System.out.print('-');
		}
		System.out.println();
		
		System.out.print(format("AvgCmp", maxSSlen, true));
		z=0;
		for (Acceptor a : acceptors) {
			System.out.print(separator);
			System.out.print(format(Double.toString(a.getStatistics()), aLength[z], true));
			z++;
		}
		System.out.println();


	}

	private static List<Acceptor> getAutomatons(NodeList nl, Grammar g, XMLRegExReader xrer) {
		ArrayList<Acceptor> result = new ArrayList<Acceptor>();
		List<Symbol> alphabet = new ArrayList<Symbol>();
		alphabet.addAll(g.getTerminals());
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			Element e = (Element) n;
			Pattern p = xrer.getPattern(e);
			System.out.println("Pattern: " + p);
			Acceptor a = Acceptor.createFrom(p, alphabet);
			System.out.println(a);
			result.add(a);
		}
		return result;
	}

	private static List<SymbolSequence> getExpressions(NodeList nl, Grammar g) {
		ArrayList<SymbolSequence> result = new ArrayList<SymbolSequence>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node parent = nl.item(i);
			NodeList children = parent.getChildNodes();
			SymbolSequence ss = new SymbolSequence();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (child instanceof Element) {
					Element e = (Element) child;
					String value = e.getAttribute("value");
					Symbol sym = null;
					if (e.getTagName().equals("term")) {
						sym = g.getTerminal(value);
					}
					if (e.getTagName().equals("nonterm")) {
						sym = g.getNonterminal(value);
					}
					ss.append(sym);
				}
			}
			System.out.println("Expression: " + ss);
			result.add(ss);
		}
		return result;
	}

	private static String format(String s, int nrOfChars, boolean alignLeft) {
		StringBuffer sb = new StringBuffer();
		if (alignLeft) {
			sb.append(s);
		}
		for (int i=0; i<nrOfChars-s.length(); i++) {
			sb.append(" ");
		}
		if (!alignLeft) {
			sb.append(s);
		}
		return sb.toString();
	}
}
