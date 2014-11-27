package com.job.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.Log;

import com.job.cache.CacheBytesDisk;

/**
 * 
 * @author Octopus
 * @Data 2014年11月19日
 * @Time 上午10:50:38
 * @Tags
 * @TODO 图片相关操作的工具类
 */
public class ImageUtils {

	private static final String TAG = ImageUtils.class.getCanonicalName();

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

	/**
	 * 如果采用保护方式存储的时候
	 * 
	 * @param imageUri
	 *            仅供解析被本缓存程序缓存的资源
	 * @return
	 */
	public static Bitmap createFromCacheFileByPath(String path) {
		byte[] data = CacheBytesDisk.instance().opt(path);
		return createBitmapByByteArr(data, null);
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

	public static boolean recyleBitmap(Bitmap bmp) {
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
			return true;
		}
		return false;
	}

}
