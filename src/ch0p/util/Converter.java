/*
 * Created on 09.09.2004
 */
package ch0p.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
//import java.nio.charset.Charset;
//import java.nio.charset.CharsetDecoder;
//import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class Converter
{
	public static final String			UTF8		= "UTF-8";
//	private static final Charset		CHARSET		= Charset.forName(UTF8);
//	private static final CharsetEncoder	ENCODER		= CHARSET.newEncoder();
//	private static final CharsetDecoder	DECODER		= CHARSET.newDecoder();
	private static final int			MAP_SIZE	= 128;
	private static final String[]		MAP;
	static {
		MAP = new String[MAP_SIZE];
		for (int i = 0; i < MAP_SIZE; i++) MAP[i] = null;
		MAP[(byte) '\\'] = "\\\\";
		MAP[(byte) '\t'] = "\\t";
		MAP[(byte) '\n'] = "\\n";
		MAP[(byte) '\r'] = "\\r";
		MAP[(byte) '\f'] = "\\f";
		MAP[(byte) '\u0007'] = "\\a";
		MAP[(byte) '\u001B'] = "\\e";
		MAP[(byte) '['] = "\\[";
		MAP[(byte) ']'] = "\\]";
		MAP[(byte) '^'] = "\\^";
		MAP[(byte) '-'] = "\\-";
		MAP[(byte) '&'] = "\\&";
		MAP[(byte) '.'] = "\\.";
		MAP[(byte) '{'] = "\\{";
		MAP[(byte) '}'] = "\\}";
		MAP[(byte) '$'] = "\\$";
		MAP[(byte) '?'] = "\\?";
		MAP[(byte) '+'] = "\\+";
		MAP[(byte) '*'] = "\\*";
		MAP[(byte) ','] = "\\,";
		MAP[(byte) '('] = "\\(";
		MAP[(byte) ')'] = "\\)";
		MAP[(byte) '|'] = "\\|";
	}

	/**
	 * Convert the bytes in a ByteBuffer into a String. The bytes are read beginning from
	 * the buffers current position up to the buffers limit. The bytes are assumed to be UTF8
	 * encoded.
	 * 
	 * @param bb
	 * @return
	 */
	public static final String byteBufferToString(ByteBuffer bb)
	{
		// CharBuffer cb;
		// cb = CHARSET.decode(bb);
		// cb = CharBuffer.allocate(bb.capacity());
		// decoder.reset();
		// decoder.decode(bb, cb, true);
		// decoder.flush(cb);
		// String symbol = cb.toString();
		// return symbol;

		try {
			byte[] ba = new byte[bb.limit()-bb.position()];
			bb.get(ba, bb.position(), bb.limit());
			return new String(ba, UTF8);
		}
		catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Bad VM! Does not support "+UTF8);
		}
	}

	/**
	 * Create a ByteBuffer object from a String instance. The characters are UTF8 encoded.
	 * 
	 * @param symbol
	 * @return
	 */
	public static final ByteBuffer stringToByteBuffer(String s)
	{
		ByteBuffer bb;
		// bb = CHARSET.encode(symbol);
		try {
			byte[] ba = s.getBytes(UTF8);
			bb = ByteBuffer.wrap(ba);
			return bb;
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Bad VM! Does not support "+UTF8);
		}
	}

	/**
	 * This method encodes a CharSequence to be serarchable by a java.util.regexp.Pattern. The resulting
	 * CharSequence will contain '\t' instead of a single tab character, for example.
	 * 
	 * @param symbol
	 * @return
	 */
	public static final CharSequence regExpEncode(CharSequence s)
	{
		StringBuffer result = new StringBuffer();
		for (int pos = 0; pos < s.length(); pos++) {
			char c = s.charAt(pos);
			if (c > MAP_SIZE) {
				result.append(c);
			}
			else {
				if (MAP[(byte) c] == null) {
					result.append(c);
				}
				else {
					result.append(MAP[(byte) c]);
				}
			}
		}
		return result;
	}

	
	private static final ArrayList<ByteBuffer> bb16Pool = new ArrayList<ByteBuffer>();
	private static final byte[] mapping = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
	private synchronized static final ByteBuffer getBB16()
	{
		int size = bb16Pool.size();
		if (size==0) return ByteBuffer.allocate(16);
		ByteBuffer bb = bb16Pool.get(size-1);
		bb16Pool.remove(size-1);
		return bb;
	}
	private static final void returnBB16(ByteBuffer b)
	{
		bb16Pool.add(b);
	}
	
	public static void writeLong(WritableByteChannel c, long l) throws IOException
	{
		ByteBuffer bb = getBB16();
		bb.clear();
		bb.put(mapping[(int)((l & 0xF000000000000000l)>>>60)]);
		bb.put(mapping[(int)((l & 0x0F00000000000000l)>>>56)]);
		bb.put(mapping[(int)((l & 0x00F0000000000000l)>>>52)]);
		bb.put(mapping[(int)((l & 0x000F000000000000l)>>>48)]);
		bb.put(mapping[(int)((l & 0x0000F00000000000l)>>>44)]);
		bb.put(mapping[(int)((l & 0x00000F0000000000l)>>>40)]);
		bb.put(mapping[(int)((l & 0x000000F000000000l)>>>36)]);
		bb.put(mapping[(int)((l & 0x0000000F00000000l)>>>32)]);
		bb.put(mapping[(int)((l & 0x00000000F0000000l)>>>28)]);
		bb.put(mapping[(int)((l & 0x000000000F000000l)>>>24)]);
		bb.put(mapping[(int)((l & 0x0000000000F00000l)>>>20)]);
		bb.put(mapping[(int)((l & 0x00000000000F0000l)>>>16)]);
		bb.put(mapping[(int)((l & 0x000000000000F000l)>>>12)]);
		bb.put(mapping[(int)((l & 0x0000000000000F00l)>>> 8)]);
		bb.put(mapping[(int)((l & 0x00000000000000F0l)>>> 4)]);
		bb.put(mapping[(int)((l & 0x000000000000000Fl)    )]);
		c.write(bb);
		returnBB16(bb);
	}
}