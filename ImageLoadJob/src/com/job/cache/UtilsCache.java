package com.job.cache;

import java.io.File;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月19日
 * @Time 上午10:50:38
 * @Tags
 * @TODO 图片相关操作的工具类
 */
class UtilsCache {

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
			return file.delete();
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public static long computeBitmapSize(Bitmap bitmap) {
		if (bitmap == null) {
			return 0;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount();
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	protected static byte[] cloneDataFragment(byte[] data) {
		int size = data.length;
		byte[] c = new byte[size];
		System.arraycopy(data, 0, c, 0, size);
		return c;
	}
}
