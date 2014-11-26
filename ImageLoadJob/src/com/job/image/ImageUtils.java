package com.job.image;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Looper;
import android.util.Log;

/**
 * 
 * @author Octopus
 * @Data 2014年11月19日
 * @Time 上午10:50:38
 * @Tags
 * @TODO 图片相关操作的工具类
 */
class ImageUtils {

	private static final String TAG = ImageUtils.class.getCanonicalName();

	/**
	 * 检测是否在UI线程内调用,并抛出运行时异常
	 */
	public static void checkIfInUIThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException("没有在UI线程内操作.");
		}
	}

	public static Options getDefaultOptions() {
		Options options = new Options();
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inDither = false;
		options.inPreferredConfig = Config.ARGB_8888;
		return options;
	}

	public static Bitmap createBitmapByByteArr(byte[] data, Options options) {
		Bitmap bmp = null;
		if (data != null) {
			try {
				if (options == null) {
					options = getDefaultOptions();
				}
				bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				if (bmp == null) {
					options.inPreferredConfig = Config.RGB_565;
					bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				}
			} catch (OutOfMemoryError e) {
				Log.w(TAG, "OutOfMemoryError");
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
		return bmp;
	}

	public static long computeBitmapSize(Bitmap bmp) {
		if (bmp == null) {
			return 0;
		}
		return 0;
	}

	public static boolean initImageCacheDir(String cacheDir) {
		checkIfInUIThread();
		File file = new File(cacheDir);
		if (file.exists() && file.isDirectory()) {
			return true;
		}
		return file.mkdirs();
	}

	public static String md5Encrypt(String str) {
		if (str == null)
			return "";
		try {
			final MessageDigest md5 = MessageDigest.getInstance("MD5");
			final byte[] buffer = str.getBytes();
			md5.update(buffer, 0, buffer.length);
			String suffix = "";
			return toHexString(md5.digest()) + suffix;
		} catch (final NoSuchAlgorithmException e) {
		}
		return "";
	}

	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static String toHexString(byte[] b) {
		final StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}

}
