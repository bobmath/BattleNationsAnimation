package util;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class LittleEndianInputStream extends BufferedInputStream {
	private byte[] buf;

	public LittleEndianInputStream(InputStream in) {
		super(in);
		buf = new byte[4];
	}

	public LittleEndianInputStream(InputStream in, int len) {
		super(in);
		if (len < 4) len = 4;
		buf = new byte[len];
	}

	public int readByte() throws IOException {
		int c = read();
		if (c < 0) throw new EOFException();
		return c;
	}

	public int readShort() throws IOException {
		if (read(buf, 0, 2) != 2)
			throw new EOFException();
		return ((buf[0] & 0xff) | (buf[1] << 8));
	}

	public int readInt() throws IOException {
		if (read(buf, 0, 4) != 4)
			throw new EOFException();
		return (buf[0] & 0xff) | ((buf[1] & 0xff) << 8)
				| ((buf[2] & 0xff) << 16) | (buf[3] << 24);
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public String readCString(int len) throws IOException {
		if (len > buf.length)
			buf = new byte[len];
		if (read(buf, 0, len) != len)
			throw new EOFException();
		int pos = 0;
		while (pos < len && buf[pos] != 0)
			pos++;
		return new String(buf, 0, pos, "us-ascii");
	}
}
