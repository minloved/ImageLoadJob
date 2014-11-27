package com.job.utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月19日
 * @Time 下午6:43:33
 * @Tags
 * @TODO 流操作相关工具类
 */
public class StreamUtils {

	public static int read(InputStream is) throws IOException {
		int b = is.read();
		if (b == -1) {
			throw new EOFException();
		}
		return b;
	}

	public static void writeInt(OutputStream os, int n) throws IOException {
		os.write((n >> 0) & 0xff);
		os.write((n >> 8) & 0xff);
		os.write((n >> 16) & 0xff);
		os.write((n >> 24) & 0xff);
	}

	public static int readInt(InputStream is) throws IOException {
		int n = 0;
		n |= (read(is) << 0);
		n |= (read(is) << 8);
		n |= (read(is) << 16);
		n |= (read(is) << 24);
		return n;
	}

	public static void writeLong(OutputStream os, long n) throws IOException {
		os.write((byte) (n >>> 0));
		os.write((byte) (n >>> 8));
		os.write((byte) (n >>> 16));
		os.write((byte) (n >>> 24));
		os.write((byte) (n >>> 32));
		os.write((byte) (n >>> 40));
		os.write((byte) (n >>> 48));
		os.write((byte) (n >>> 56));
	}

	public static long readLong(InputStream is) throws IOException {
		long n = 0;
		n |= ((read(is) & 0xFFL) << 0);
		n |= ((read(is) & 0xFFL) << 8);
		n |= ((read(is) & 0xFFL) << 16);
		n |= ((read(is) & 0xFFL) << 24);
		n |= ((read(is) & 0xFFL) << 32);
		n |= ((read(is) & 0xFFL) << 40);
		n |= ((read(is) & 0xFFL) << 48);
		n |= ((read(is) & 0xFFL) << 56);
		return n;
	}

	public static void writeString(OutputStream os, String s) throws IOException {
		byte[] b = s.getBytes("UTF-8");
		writeLong(os, b.length);
		os.write(b, 0, b.length);
	}

	public static String readString(InputStream is) throws IOException {
		int n = (int) readLong(is);
		byte[] b = streamToBytes(is, n);
		return new String(b, "UTF-8");
	}

	public static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
		if (map != null) {
			writeInt(os, map.size());
			for (Map.Entry<String, String> entry : map.entrySet()) {
				writeString(os, entry.getKey());
				writeString(os, entry.getValue());
			}
		} else {
			writeInt(os, 0);
		}
	}

	public static Map<String, String> readStringStringMap(InputStream is) throws IOException {
		int size = readInt(is);
		Map<String, String> result = (size == 0) ? Collections.<String, String> emptyMap() : new HashMap<String, String>(size);
		for (int i = 0; i < size; i++) {
			String key = readString(is).intern();
			String value = readString(is).intern();
			result.put(key, value);
		}
		return result;
	}

	public static void writeByteArr(OutputStream os, byte[] arr) throws IOException {
		if (arr != null) {
			writeLong(os, arr.length);
			os.write(arr, 0, arr.length);
		} else {
			writeLong(os, 0);
		}
	}

	public static byte[] readByteArr(InputStream is) throws IOException {
		int n = (int) readLong(is);
		return streamToBytes(is, n);
	}

	/**
	 * 不用验证是否正确
	 * 
	 * @param os
	 * @param streamData
	 * @throws IOException
	 */
	public static void writeBytesArrWithoutCheckByte(OutputStream os, byte[] arr) throws IOException {
		if (arr != null) {
			os.write(arr, 0, arr.length);
		}
	}

	public static byte[] readBytesArrWithoutCheckByte(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int count = 0;
		while ((count = is.read(buffer)) != -1) {
			bos.write(buffer, 0, count);
		}
		byte[] ret = bos.toByteArray();
		safeCloseOutputStream(bos);
		return ret;
	}

	/**
	 * Reads the contents of an InputStream into a byte[].
	 * */
	private static byte[] streamToBytes(InputStream in, int length) throws IOException {
		byte[] bytes = new byte[length];
		int count;
		int pos = 0;
		while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
			pos += count;
		}
		if (pos != length) {
			throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
		}
		return bytes;
	}

	public static void safeCloseInputStream(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public static void safeCloseOutputStream(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
			}
		}
	}

}