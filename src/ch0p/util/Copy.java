/*
 * Created on 09.09.2004
 */
package ch0p.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class Copy
{
	public static final int	DEFAULT_BUFFER_SIZE	= 4 * 1024 * 1024;

	/**
	 * Copy a file <code>from</code> to a file <code>to</code>. Us a buffer of
	 * <code>DEFAULT_BUFFER_SIZE</code> bytes.
	 * 
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	public static void copy(File from, File to) throws IOException
	{
		copy(from, to, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copy a file <code>from</code> to a file <code>to</code>. Use a buffer of <code>buffersize</code>
	 * bytes.
	 * 
	 * @param from
	 * @param to
	 * @param buffersize
	 * @throws IOException
	 */
	public static void copy(File from, File to, int buffersize) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(buffersize);
		FileInputStream fis = new FileInputStream(from);
		FileOutputStream fos = new FileOutputStream(to);
		FileChannel rc = fis.getChannel();
		FileChannel wc = fos.getChannel();
		buffer.clear();
		int read = rc.read(buffer);
		while (read > 0) {
			buffer.flip();
			wc.write(buffer);
			buffer.clear();
			read = rc.read(buffer);
		}
	}

	/**
	 * Returns a thread for copying file "from" to file "to". The thread will not yet be started.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static Thread copyThread(File from, File to)
	{
		return copyThread(from, to, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Returns a thread for copying file "from" to file "to". The thread will not yet be started.
	 * 
	 * @param from
	 * @param to
	 * @param buffersize
	 * @return
	 */
	public static Thread copyThread(File from, File to, int buffersize)
	{
		final File fromF = from;
		final File toF = to;
		final int bs = buffersize;
		Thread runner = new Thread(new Runnable()
		{
			public void run()
			{
				try {
					copy(fromF, toF, bs);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return runner;
	}

	public static void main(String[] args)
	{
		if ((args.length < 2) || (args.length > 3)) {
			System.out.println("usage: java ch0p.util.Copy <filenamefrom> <filenameto> [buffersize]");
			System.exit(-1);
		}
		File from = new File(args[0]);
		File to = new File(args[1]);
		int buffersize = DEFAULT_BUFFER_SIZE;
		if (args.length == 3) {
			buffersize = Integer.parseInt(args[2]);
		}
		try {
			long l1 = System.currentTimeMillis();
			copy(from, to, buffersize);
			long l2 = System.currentTimeMillis();
			double bps = (from.length() * 1000.0d) / ((l2 - l1) * 1024.0 * 1024.0);
			System.out.println("Copied " + from.length() + " bytes in " + (l2 - l1) + " ms. at " + (bps)
				+ " MB/sec.");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}