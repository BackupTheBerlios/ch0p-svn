package ch0p.datastrutures;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ch0p.regex.Acceptor;
import ch0p.regex.Pattern;
import ch0p.regex.XMLRegExReader;

/**
 * @author Tom Gelhausen
 */
public class XMLGrammarReader {

	public static class IllegalGrammarFileException extends Exception {
		private static final long serialVersionUID = 1795458103583267667L;
		public IllegalGrammarFileException() {
			super();
		}
		public IllegalGrammarFileException(String message) {
			super(message);
		}
	}

	public static Grammar read(File f) throws IllegalGrammarFileException {
		try {

			Grammar result = new Grammar();

			// Set<Nonterminal> nonterminals = new HashSet<Nonterminal>();
			// Map<String, Nonterminal> nonterminalsDict = new HashMap<String,
			// Nonterminal>();
			// Set<Terminal> terminals = new HashSet<Terminal>();
			// Map<String, Terminal> terminalsDict = new HashMap<String,
			// Terminal>();
			// List<Rule> rules = new ArrayList<Rule>();

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(f);

			// normalize text representation
			doc.getDocumentElement().normalize();

			NodeList terminalsNL = doc.getDocumentElement()
					.getElementsByTagName("term");
			NodeList nonterminalsNL = doc.getDocumentElement()
					.getElementsByTagName("nonterm");
			NodeList rulesNL = doc.getDocumentElement().getElementsByTagName(
					"rule");

			System.out.println("\nNon-Terminals:");

			for (int i = 0; i < nonterminalsNL.getLength(); i++) {
				Node nonterminalNode = nonterminalsNL.item(i);
				Node valueNode = nonterminalNode.getAttributes().getNamedItem(
						"value");
				if (valueNode != null) {
					String s = valueNode.getNodeValue().intern();
					Nonterminal nt = result.getNonterminal(s);
					if (nt == null) {
						nt = new Nonterminal(s);
						result.add(nt);
						System.out.println("  " + s);
					}
				}
			}

			System.out.println("\nTerminals:");

			for (int i = 0; i < terminalsNL.getLength(); i++) {
				Node terminalNode = terminalsNL.item(i);
				Node valueNode = terminalNode.getAttributes().getNamedItem(
						"value");
				if (valueNode != null) {
					String s = valueNode.getNodeValue().intern();
					Terminal t = result.getTerminal(s);
					if (t == null) {
						t = new Terminal(s);
						result.add(t);
						System.out.println("  " + s);
					}
				}
			}

			System.out.println("\nRules:");

			for (int i = 0; i < rulesNL.getLength(); i++) {
				Node ruleNode = rulesNL.item(i);
				NodeList children = ruleNode.getChildNodes();
				Node lhsNode = findFirstNodeWithName("lhs", children);
				NodeList lhsChildren = lhsNode.getChildNodes();
				Node rhsNode = findFirstNodeWithName("rhs", children);
				NodeList rhsChildren = rhsNode.getChildNodes();
				Context lC = getLContext(lhsChildren, result);
				Context lR = getRContext(lhsChildren, result);
				SymbolSequence lhs = getSymbols(lhsChildren, result);
				SymbolSequence rhs = getSymbols(rhsChildren, result);
				Rule r = new Rule(lhs, rhs);
				result.add(r);
				System.out.println("  " + r);
			}

			System.out.println("\nStart symbol:");

			NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
			Node startSymbolAttrNode = attributes.getNamedItem("startsymbol");
			String startSymbolAttrValue = startSymbolAttrNode.getNodeValue();
			Nonterminal startSymbol = result.getNonterminal(startSymbolAttrValue);
			result.setStartSymbol(startSymbol);
			
			System.out.println("  "+startSymbol);
			return result;

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;

	}// end of main

	private static Context getRContext(NodeList lhsChildren, Grammar g) {
		if (lhsChildren.getLength()<1) return AnyContext.instance;
		Node last = lhsChildren.item(lhsChildren.getLength()-1);
		String nodename = last.getNodeName();
		if ("context".equals(nodename)) {
			XMLRegExReader rr = new XMLRegExReader(g);
			Pattern p = rr.getSequence((Element)last);
			Acceptor a = Acceptor.createFrom(p, g.getAlphabet());
			Context ctx = new Context(a);
			return ctx;
		}
		else {
			return AnyContext.instance;
		}
	}

	private static Context getLContext(NodeList lhsChildren, Grammar g) {
		if (lhsChildren.getLength()<1) return AnyContext.instance;
		Node first = lhsChildren.item(0);
		String nodename = first.getNodeName();
		if ("context".equals(nodename)) {
			XMLRegExReader rr = new XMLRegExReader(g);
			Pattern p = rr.getSequence((Element)first);
			Acceptor a = Acceptor.createFrom(p, g.getAlphabet());
			Context ctx = new Context(a);
			return ctx;
		}
		else {
			return AnyContext.instance;
		}
	}

	private static final Node findFirstNodeWithName(String name, NodeList in) {
		for (int i = 0; i < in.getLength(); i++) {
			Node n = in.item(i);
			if (n != null) {
				if (name.equals(n.getNodeName())) {
					return n;
				}
			}
		}
		return null;
	}

	private static final SymbolSequence getSymbols(NodeList nl, Grammar g) {
		SymbolSequence result = new SymbolSequence();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			Symbol s = getSymbol(n, g);
			if (s != null)
				result.append(s);
		}
		return result;
	}

	private static final Symbol getSymbol(Node n, Grammar g) {
		String nodename = n.getNodeName();
		Node selectNode = n.getAttributes().getNamedItem("value");
		String selectValue = selectNode.getNodeValue();
		Symbol result = null;
		if ("term".equals(nodename)) {
			result = g.getTerminal(selectValue);
		} else if ("nonterm".equals(nodename)) {
			result = g.getNonterminal(selectValue);
		}
		return result;
	}

}
