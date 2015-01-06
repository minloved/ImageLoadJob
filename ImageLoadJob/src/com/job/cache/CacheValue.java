package com.job.cache;

import com.job.cache.CacheOverTime.CacheOverTimeInterface;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:35:32
 * @Tags
 * @TODO TODO 缓存的数据到对象的映射
 */
abstract class CacheValue<KEY> implements CacheOverTimeInterface {
	private final CacheOverTime overTime = new CacheOverTime();
	private Object value;
	private final KEY key;
	private long size = 0;
	private boolean canRemoved = true;

	public CacheValue(KEY key, Object value, long size) {
		this.key = key;
		setValue(value, size);
	}

	@Override
	public final boolean isOverTime(long maxTime) {
		return overTime.isOverTime(maxTime);
	}

	@Override
	public final void updateCacheTime() {
		overTime.updateCacheTime();
	}

	public void setValue(Object value, long size) {
		this.value = value;
		this.size = size;
	}

	public KEY key() {
		return key;
	};

	public Object value() {
		return value;
	};

	public long size() {
		return size;
	};

	public void recyle() {
	};

	public Object cloneValue() {
		return null;
	};

	public boolean canRemoved() {
		return canRemoved;
	}

	public void setRemoved(boolean bool) {
		canRemoved = bool;
	}

}
