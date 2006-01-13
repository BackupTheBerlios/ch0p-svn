/*
 * Created on 04.10.2005
 */
package ch0p.datastrutures;

/**
 * @author Tom Gelhausen
 */
public class Word extends SymbolSequence {
	
	public Word() {
	}
	
	public Word(SymbolSequence ss, Derivation derivation) {
		this.append(ss);
		this.setDerivation(derivation);
	}
	
	private Derivation derivation;

	/**
	 * @return Returns the derivation.
	 */
	public Derivation getDerivation() {
		return derivation;
	}

	/**
	 * @param derivation The derivation to set.
	 */
	public void setDerivation(Derivation derivation) {
		this.derivation = derivation;
	}
}
