package com.job.cache;

import com.job.cache.CacheBytesController.BytesValue;
import com.job.utils.JobUtils;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:34:40
 * @Tags
 * @TODO TODO 内存中字节缓存
 */
public class CacheBytesController extends CacheController<String, BytesValue> {

	public CacheBytesController() {
		super();
		setMaxCacheTime(DEFAULT_MAX_CACHE_TIME << 2);
		setMaxMemorySize(DEFAULT_MAX_CACHE_SIZE >> 1);
	}

	@Override
	public byte[] opt(String uriKey) {
		Object result = super.opt(uriKey);
		if (result != null && result instanceof byte[]) {
			return (byte[]) result;
		}
		return null;
	}

	@Override
	public BytesValue createCacheValue(String key, Object value) {
		return new BytesValue(key, (byte[]) value);
	}

	public static class BytesValue extends CacheValue<String> {

		public BytesValue(String key, byte[] value) {
			super(key, value, value.length);
		}

		@Override
		public byte[] value() {
			return (byte[]) super.value();
		}

		@Override
		public byte[] cloneValue() {
			updateCacheTime();
			return JobUtils.clone(value());
		}
	}

}
