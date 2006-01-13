/*
 * Created on 07.10.2005
 */
package ch0p.test;

import java.io.IOException;

import ch0p.util.BigList;

/**
 * @author Tom Gelhausen
 */
public class TestBigList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BigList bl1 = new BigList();
		BigList bl2 = new BigList();
		
		for (int i=0; i<1000000; i++) {
			bl1.append("L"+i);
		}
		
		for (int i=0; i<bl1.size(); i++) {
			bl2.append(bl1.getLine(i));
		}
		
		bl1 = null;
		
		for (int i=0; i<bl2.size(); i++) {
			String s = bl2.getLine(i);
			String sub = s.substring(1);
			int x = Integer.parseInt(sub);
			if (x!=i) {
				System.err.println(x+"!="+i);
			}
		}
		System.out.println("Ok.");
	}

}
