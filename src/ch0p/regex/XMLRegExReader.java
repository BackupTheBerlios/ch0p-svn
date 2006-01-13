/*
 * Created on 29.11.2005
 */
package ch0p.regex;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
public class XMLRegExReader {
	
	protected Grammar g;
	protected ArrayList<Symbol> alphabet;
	
	public XMLRegExReader(Grammar g) {
		this.g = g;
		alphabet = new ArrayList<Symbol>();
		alphabet.addAll(g.getTerminals());
		alphabet.addAll(g.getNonterminals());
	}
	
	public Pattern getPattern(Element e) {
		String name = e.getTagName();
		if (name.equals("regex")) return getSequence(e);
		if (name.equals("seq")) return getSequence(e);
		if (name.equals("term")) return getSingle(e);
		if (name.equals("nonterm")) return getSingle(e);
		if (name.equals("any")) return getAny(e);
		if (name.equals("one")) return getOne(e);
		if (name.equals("some")) return getSome(e);
		if (name.equals("all")) return getAll(e);
		if (name.equals("not")) return getNot(e);
		throw new IllegalArgumentException("Unknown tag name: "+name);
	}
	
	protected AnySymbolPattern getAny(Element e) {
		AnySymbolPattern result = new AnySymbolPattern(alphabet);
		setFlags(result, e);
		return result;
	}

	protected NotPattern getNot(Element e) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		NodeList children = e.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Element) {
				Element elem = (Element) c;
				Pattern p = getPattern(elem);
				patterns.add(p);
			}
		}
		if (patterns.size()!=1) throw new IllegalArgumentException("A \"not\" pattern must have exactly one child element");
		NotPattern result = new NotPattern(patterns.get(0));
		return result;
	}

	protected AllPattern getAll(Element e) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		NodeList children = e.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Element) {
				Element elem = (Element) c;
				Pattern p = getPattern(elem);
				patterns.add(p);
			}
		}
		AllPattern result = new AllPattern(patterns);
		setFlags(result, e);
		return result;
	}

	protected SomePattern getSome(Element e) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		NodeList children = e.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Element) {
				Element elem = (Element) c;
				Pattern p = getPattern(elem);
				patterns.add(p);
			}
		}
		SomePattern result = new SomePattern(patterns);
		setFlags(result, e);
		return result;
	}

	protected SingleSymbolPattern getSingle(Element e) {
		String name = e.getTagName();
		String val = e.getAttribute("value");
		if (val.equals("")) throw new IllegalArgumentException("The value attribute is missing");
		Symbol  b;
		if (name.equals("term")) {
			b = g.getTerminal(val);
		} else if (name.equals("nonterm")) {
			b = g.getNonterminal(val);
		} else {
			throw new IllegalArgumentException("term/nonterm");
		}
		SingleSymbolPattern result = new SingleSymbolPattern(b);
		setFlags(result, e);
		return result;
	}
	
	protected OnePattern getOne(Element e) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		NodeList children = e.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Element) {
				Element elem = (Element) c;
				Pattern p = getPattern(elem);
				patterns.add(p);
			}
		}
		OnePattern result = new OnePattern(patterns);
		setFlags(result, e);
		return result;
	}
	
	public SequencePattern getSequence(Element e) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		NodeList children = e.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Element) {
				Element elem = (Element) c;
				Pattern p = getPattern(elem);
				patterns.add(p);
			}
		}
		SequencePattern result = new SequencePattern(patterns);
		setFlags(result, e);
		return result;
	}
	
	protected void setFlags(Pattern p, Element e) {
		String optional = e.getAttribute("optional");
		if (optional.equalsIgnoreCase("true")) {
			p.setOptional(true);
		} else {
			p.setOptional(false);
		}
		String repeatable = e.getAttribute("repeatable");
		if (repeatable.equalsIgnoreCase("true")) {
			p.setRepeatable(true);
		} else {
			p.setRepeatable(false);
		}
	}
}
