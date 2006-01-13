/*
 * Created on 09.09.2004
 */
package ch0p.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Implements a disk based list.
 * 
 * @author Tom Gelhausen
 * @version $Id$
 */
class DiskList implements SimpleList, RunlevelDestructor.CleanUp {

	public static class Index {
		public static final int RUNLEVEL = 3457;
		public static final long ERROR_VALUE = Long.MIN_VALUE;

		public static final int INCREASE_SIZE = 256 * 1024; // in bytes

		private static final ByteBuffer INCREASE = ByteBuffer
				.allocateDirect(INCREASE_SIZE);
		{
			// initialization of the INCREASE buffer

			LongBuffer lb = INCREASE.asLongBuffer();
			lb.limit(lb.capacity());
			lb.position(0);
			for (int i = 0; i < lb.capacity(); i++) {
				lb.put(ERROR_VALUE);
			}
			lb.position(0);
			INCREASE.limit(INCREASE.capacity());
		}

		private File datafile;
		
		boolean keepAfterTermination = false;
		
		private FileChannel myChannel = null;

		private long currentBufferPos = -1;

		private LongBuffer myBuffer = null;

		private long max = 0;

		/**
		 * Creates an Index based on a temporary file.
		 * 
		 * @throws IOException
		 */
		public Index() throws IOException {
			this(File.createTempFile("Disklist", ".idx"), false);
		}

		protected Index(File f, boolean keepAfterTermination)
				throws IOException {
			datafile = f;
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			this.keepAfterTermination=keepAfterTermination;
			if (!keepAfterTermination) {
				datafile.deleteOnExit();
			}
			myChannel = raf.getChannel();
			// how to obtain max ???
		}

		/**
		 * Moves the buffer window the correct position, if necessary. If the
		 * underlying index file is not large enough, it is automatically
		 * increased in size.
		 * 
		 * @param pos
		 * @throws IOException
		 */
		private void moveBuffer(long pos) throws IOException {
			int base = (int) (pos >> 32);
			long size = myChannel.size();
			if ((currentBufferPos != base) || (pos >= size)) {
				if (pos >= size) {
					allocUntil(pos);
					size = myChannel.size();
				}
				long blocksize = Integer.MAX_VALUE;
				if (((base + 1l) << 32) > size) {
					blocksize = (size & 0xFFFFFFFF);
				}
				MappedByteBuffer mbb = myChannel.map(
						FileChannel.MapMode.READ_WRITE, base, blocksize);
				myBuffer = mbb.asLongBuffer();
				currentBufferPos = base;
			}
		}

		/**
		 * increases the file size until it is large enough to access pos
		 * @param pos
		 * @throws IOException
		 */
		private void allocUntil(long pos) throws IOException {
			long size = myChannel.size();
			while (pos >= size) {
				myChannel.position(size);
				INCREASE.position(0);
				myChannel.write(INCREASE);
				size = myChannel.size();
			}
		}

		/**
		 * Creates an Index from a given file. The whole file is read and lines
		 * (determined by CR, LF, or CRLF) are indexed.
		 * 
		 * @param f
		 * @return
		 * @throws IOException
		 */
		public static Index createIndexFor(File f) throws IOException {
			Index result = new Index();

			FileInputStream fis = new FileInputStream(f);
			FileChannel readchannel = fis.getChannel();

			readchannel.position(0);
			long startpos = 0;
			long rest = readchannel.size();
			int currentSize = Integer.MAX_VALUE;
			if (currentSize > rest)
				currentSize = (int) rest;
			result.setPositionOfLine(0, 0);
			long nextline = 0;
			byte verylastchar = bLF; // for empty files: assume there was a
			// LF
			boolean maybebetweenCRLF = false;
			while (currentSize > 0) {
				MappedByteBuffer mbb = readchannel.map(
						FileChannel.MapMode.READ_ONLY, startpos, currentSize);
				for (int i = 0; i < currentSize; i++) {
					byte b = mbb.get(i);
					verylastchar = b;
					if (b == bLF) {
						if (maybebetweenCRLF) {
							// we just read a CRLF, so do not count this line
							// again
							maybebetweenCRLF = false;

							// but we need to correct the index sice the next
							// line starts one byte later!
							long p = result.getPositionOfLine(nextline);
							result.setPositionOfLine(nextline, p + 1);

							// continue with next i...
						} else {
							nextline++;
							result
									.setPositionOfLine(nextline, startpos + i
											+ 1);
							// maybebetweenCRLF is allready false
							// continue with next i...
						}
					} else if (b == bCR) {
						nextline++;
						result.setPositionOfLine(nextline, startpos + i + 1);
						maybebetweenCRLF = true;
						// continue with next i...
					} else {
						// this is nither a CR nor a LF
						maybebetweenCRLF = false;
						// continue with next i...
					}
				}
				mbb.clear();
				mbb = null;
				// allow the system to garbage collect the buffer if it wants to
				// ;-)
				Thread.yield();
				startpos += currentSize;
				rest -= currentSize;
				if (currentSize > rest)
					currentSize = (int) rest;
			}

			if (!((verylastchar == bLF) || (verylastchar == bCR))) {
				// the file did not end with a CR/LF
				nextline++;
				result.setPositionOfLine(nextline, readchannel.size());
			}

			// tidy up
			readchannel.close();
			fis.close();

			// we neet to wait for the MappedByteBuffer objects to be garbage
			// collectet before we can
			// continue. See FileChannel.map for more information...
			System.gc();
			return result;
		}

		/**
		 * @return Returns the maximal ID that has been set.
		 */
		public long getCurrentMaxID() {
			return max;
		}

		/**
		 * Returns the start position of line id.
		 * 
		 * @param id
		 * @return
		 * @throws IOException
		 */
		public long getPositionOfLine(long id) throws IOException {
			assert (id >= 0) : "unknown id (negative: " + id + ")";
			assert (id <= max) : "unknown id (too big: " + id + ", max: " + max
					+ ")";
			moveBuffer(id * 8);
			int offset = (int) (id & 0xFFFFFFFF);
			long result = myBuffer.get(offset);
			return result;
		}

		/**
		 * Sets the start position of line id to pos.
		 * 
		 * @param id
		 * @param pos
		 * @throws IOException
		 */
		public void setPositionOfLine(long id, long pos) throws IOException {
			assert (id >= 0) : "unknown id (negative: " + id + ")";
			if (id > max) {
				max = id;
			}
			moveBuffer(id * 8);
			int offset = (int) (id & 0xFFFFFFFF);
			myBuffer.put(offset, pos);
		}
		
		protected void shutdown() throws IOException {
			myBuffer = null;
			myChannel.force(true);
			myChannel.close();
			myChannel = null;
			System.gc();
			if (!keepAfterTermination) {
				System.out.print("Deleting index file(" + datafile + ")... ");
				datafile.delete();
				System.out.println("done.");
			}
		}
	}

	public static final int RUNLEVEL = 3456;

	private static final String NEW_LINE = System.getProperty("line.separator");

	private static final byte bCR = '\r';

	private static final byte bLF = '\n';

	private Cache<String> cache;

	private FileChannel datafilereadchannel;

	private FileChannel datafilewritechannel;

	private File fileref;

	private Index index;

	private boolean deleted = false;

	private boolean isTempFile = false;

	private long size;

	/**
	 * Creates a DiskList. The data is stored in a temporary file. The temporary
	 * file is removed when the instance is garbage collected.
	 * 
	 * @throws IOException
	 */
	public DiskList() throws IOException {
		this(File.createTempFile("DiskListTempFile", ".txt"));
		isTempFile = true;
		fileref.deleteOnExit();
	}

	/**
	 * Creates a DiskList backed up by the given file. If it allready exists, it
	 * is read, indexed, and all subsequent calls to <code>append()</code>
	 * append to the end of this file. The file is not removed when the instance
	 * is garbage collected.
	 * 
	 * @param datafile
	 * @throws IOException
	 */
	public DiskList(File datafile) throws IOException {
		fileref = datafile;

		if ((datafile.exists()) && (datafile.length() > 0)) {
			index = Index.createIndexFor(datafile);
			size = index.getCurrentMaxID();
		} else {
			index = new Index();
			index.setPositionOfLine(0, 0);
			size = 0;
		}

		cache = new Cache<String>();

		FileOutputStream fos = new FileOutputStream(datafile, true);
		datafilewritechannel = fos.getChannel();
		datafilewritechannel.position(datafilewritechannel.size());

		FileInputStream fis = new FileInputStream(datafile);
		datafilereadchannel = fis.getChannel();
		index.setPositionOfLine(0, 0);

		RunlevelDestructor.add(this, RUNLEVEL);
	}

	/**
	 * Appends a String to the end of this list. The system specific new line
	 * symbol(symbol) is appended (either CR, LF, or CRLF). The line number it gets
	 * is returned. The first line gets the index 0.
	 * 
	 * @param symbol
	 *            The String to append to the end of this list.
	 * @return Returns the line number this string got.
	 * @throws IOException
	 */
	public long append(String s) throws IOException {
		// writing line size
		ByteBuffer bb;
		bb = Converter.stringToByteBuffer(s + NEW_LINE);
		datafilewritechannel.write(bb);

		// allready write start position of the next line to be able to
		// determine the length of the current
		// (last) line
		size++;
		long pos = datafilewritechannel.position();
		index.setPositionOfLine(size, pos);
		return size - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cleaner.Cleanable#cleanUp()
	 */
	public synchronized void cleanUp() throws Throwable {
		try {
			index.shutdown();
		} catch (IOException e) {
			System.out.print("(" + e + ") ");
		}
		index = null;
		if (isTempFile) {
			System.out.print("Deleting temp file(" + fileref + ")... ");
			try {
				delete();
			} catch (IOException e) {
				System.out.print("(" + e + ") ");
			}
			System.out.println("done.");
		} else {
			System.out.print("Closing data file (" + fileref + ")... ");
			try {
				close();
			} catch (IOException e) {
				System.out.print("(" + e + ") ");
				e.printStackTrace();
			}
			System.out.println("done.");
		}
	}

	/**
	 * Closes this list. Not jet written data will be written and any backing
	 * files will be closed. Further operations on this list cause IOExceptions.
	 * 
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		if (datafilewritechannel.isOpen()) {
			datafilewritechannel.force(true);
			datafilewritechannel.close();
		}
		if (datafilereadchannel.isOpen()) {
			datafilereadchannel.close();
		}
	}

	/**
	 * Discards the list. The underlying file is deleted. Any subsequent calls
	 * to methods of a discarded instance will throw IOExceptions.
	 * 
	 * @throws IOException
	 */
	public synchronized void delete() throws IOException {
		if (!deleted) {
			if (datafilewritechannel.isOpen()) {
				datafilewritechannel.close();
			}
			if (datafilereadchannel.isOpen()) {
				datafilereadchannel.close();
			}
			fileref.delete();
			deleted = true;
		}
	}

	/**
	 * Returns the file with the stored data used by this DiskList.
	 * 
	 * @return
	 */
	File getBackingFile() {
		return fileref;
	}

	/**
	 * Obtains the String at the line lineNr. The first line has the index 0.
	 * 
	 * @param lineNr
	 *            The number of the line to return.
	 * @return te line with the specified line number.
	 * @throws IOException
	 */
	public String getLine(long lineNr) throws IOException {
		String cacheval = cache.get(lineNr);
		if (cacheval != null)
			return cacheval;
		long pos = index.getPositionOfLine(lineNr);
		long posnext = index.getPositionOfLine(lineNr + 1);
		int size = (int) (posnext - pos);
		if (size == 0)
			return "";
		if (size < 0)
			throw new IllegalArgumentException("Trying to access line nr "
					+ lineNr + " of " + (size() - 1)
					+ " (hint: the first line has index 0)");
		assert size > 0;
		ByteBuffer bb = ByteBuffer.allocate(size);
		datafilereadchannel.read(bb, pos);
		cutCRLF(bb);
		bb.position(0);
		String result = Converter.byteBufferToString(bb);
		cache.put(lineNr, result);
		return result;
	}

	/**
	 * Returns the number of lines currently in this list.
	 * 
	 * @return the number of lines currently in this list.
	 */
	public long size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		cleanUp();
		super.finalize();
	}

	/**
	 * cut of new line signals, if it exists
	 * 
	 * @param bb
	 *            the limit of the parameter buffer is reduced by 1 or 2,
	 *            depending whether this buffer ends with CR, LF, or CRLF
	 */
	private void cutCRLF(ByteBuffer bb) {
		int size = bb.limit();
		byte last = bb.get(size - 1);
		if ((last == bCR) || (last == bLF)) {
			if (size == 1) {
				bb.limit(0);
				return;
			}
			byte lastbutone = bb.get(size - 2);
			if ((lastbutone == bCR) || (lastbutone == bLF)) {
				bb.limit(size - 2);
			} else {
				bb.limit(size - 1);
			}
		}
	}

}
