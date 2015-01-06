package com.job.cache;

import android.graphics.Bitmap;

import com.job.cache.CacheBitmapController.BitmapValue;
import com.job.utils.ImageUtils;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:34:00
 * @Tags
 * @TODO TODO 内存中bitmap缓存
 */
public final class CacheBitmapController extends CacheController<String, BitmapValue> {

	private static final CacheBitmapController sInstance = new CacheBitmapController();

	public static CacheBitmapController singleInstance() {
		return sInstance;
	}

	// 设为最大内存的1/3
	private CacheBitmapController() {
		super();
		setMaxMemorySize(DEFAULT_MAX_CACHE_SIZE * 6 / 5);
	}

	@Override
	public Bitmap opt(String uriKey) {
		return (Bitmap) super.opt(uriKey);
	}

	public static class BitmapValue extends CacheValue<String> {

		public BitmapValue(String key, Bitmap value) {
			super(key, value, ImageUtils.computeBitmapSize(value));
		}

	}

	@Override
	public BitmapValue createCacheValue(String key, Object value) {
		return new BitmapValue(key, (Bitmap) value);
	}
}
