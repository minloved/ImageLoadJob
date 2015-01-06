package com.job.utils;

import java.io.File;

import android.os.Looper;

public class JobUtils {

	/**
	 * 检测是否在UI线程内调用,并抛出运行时异常
	 */
	public static void checkIfInUIThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException("没有在UI线程内操作.");
		}
	}

	/**
	 * 深度copy一份最新的数据
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] clone(byte[] data) {
		int size = data.length;
		byte[] c = new byte[size];
		System.arraycopy(data, 0, c, 0, size);
		return c;
	}

	/**
	 * 
	 * @param path
	 *            这个文件的绝对路径
	 * @return 如果存在返回该文件夹,否则返回Null
	 */
	public static File exsit(String path) {
		File file = new File(path);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	/**
	 * 
	 * @param path
	 * @return 文件还存在,返回false,其他返回true
	 */

	public static boolean removeFileIfExsit(String path) {
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				for (String p : file.list()) {
					boolean removed = removeFileIfExsit(p);
					if (!removed) return false;
				}
			}
			return file.delete();
		}
		return true;
	}
}
