package com.job.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Looper;

public abstract class CacheController<K, V extends CacheValue<K>> implements CacheInterface<K> {
	private final HashMap<K, V> mCacheMap = new HashMap<K, V>();
	private final ReentrantLock mLock = new ReentrantLock();
	private final static long DEFAULT_MAX_CACHE_TIME = 2 * 60 * 1000;
	private final static long DEFAULT_MAX_CACHE_SIZE = 10 * 1024 * 1024;
	private long mCachedSize = 0;
	private long mMaxCacheTime = 0;
	private long mMaxCacheSize = 0;
	private boolean isRecyling = false;

	public CacheController() {
		mCachedSize = 0;
		mMaxCacheSize = DEFAULT_MAX_CACHE_SIZE;
		mMaxCacheTime = DEFAULT_MAX_CACHE_TIME;
	}

	@Override
	public boolean put(K key, Object obj) {
		mLock.lock();
		CacheValue<K> value = mCacheMap.get(key);
		if (value != null && value.value() != null) {
			value.updateCacheTime();
			mLock.unlock();
			return true;
		}
		V cv = createCacheValue(key, obj);
		mCachedSize += cv.size();
		releaseOverTimeOrOverSize();
		mCacheMap.put(key, cv);
		mLock.unlock();
		return false;
	}

	@Override
	public Object opt(K uriKey) {
		if (checkAllowOptCache()) {
		}
		mLock.lock();
		V value = mCacheMap.get(uriKey);
		if (value != null) {
			if (value.value() != null) {
				value.updateCacheTime();
				Object data = value.cloneValue();
				mLock.unlock();
				if (data != null) {
					return data;
				}
				return value.value();
			}
			mCachedSize -= value.size();
			mCacheMap.remove(uriKey);
		}
		mLock.unlock();
		return null;
	}

	/**
	 * 主线程内且正在被回收时,不允许获取缓存,提升主线程的速度
	 * 
	 * @return true 是允许,否则不允许
	 */
	public final boolean checkAllowOptCache() {
		return !(Looper.myLooper() == Looper.getMainLooper() && isRecyling);
	}

	public void releaseOverTimeOrOverSize() {
		try {
			mLock.lock();
			isRecyling = true;
			recyleOverTime();
			recyleOverSizeBorder();
			isRecyling = false;
			mLock.unlock();
		} finally {
			// TODO:
			System.gc();
		}
	}

	/**
	 * 释放超时的数据
	 */

	public boolean recyleOverTime() {
		Set<K> sets = mCacheMap.keySet();
		ArrayList<K> l = new ArrayList<K>();
		for (K key : sets) {
			V v = mCacheMap.get(key);
			if (v.isOverTime(mMaxCacheTime)) {
				mCachedSize -= v.size();
				v.recyle();
				l.add(key);
			}
		}
		for (K key : l) {
			mCacheMap.remove(key);
		}
		return false;
	}

	/**
	 * 释放最大缓存的数据
	 */

	public boolean recyleOverSizeBorder() {
		if (mCachedSize > mMaxCacheSize) {
			ArrayList<V> l = new ArrayList<V>(mCacheMap.values());
			try {
				Collections.sort(l, sComparator);
			} catch (Exception e) {
			}
			do {
				if (!l.isEmpty()) {
					V value = l.remove(0);
					mCachedSize -= value.size();
					mCacheMap.remove(value.key());
					value.recyle();
				}
			} while (mCachedSize > mMaxCacheSize);
			l.clear();
		}
		return false;
	}

	public void recyleAllCache() {
		mLock.lock();
		isRecyling = true;
		isRecyling = false;
		mLock.unlock();
	}

	public long getMaxCacheSize() {
		return mMaxCacheSize;
	}

	public void setMaxMemorySize(long mms) {
		mLock.lock();
		this.mMaxCacheSize = mms;
		releaseOverTimeOrOverSize();
		mLock.unlock();
	}

	public long getMaxCacheTime() {
		return mMaxCacheTime;
	}

	public void setMaxCacheTime(long mmt) {
		mLock.lock();
		this.mMaxCacheTime = mmt;
		releaseOverTimeOrOverSize();
		mLock.unlock();
	}

	public boolean isRecyling() {
		return isRecyling;
	}

	public abstract V createCacheValue(K key, Object value);

	@SuppressWarnings("rawtypes")
	public static Comparator<CacheValue> sComparator = new Comparator<CacheValue>() {
		@Override
		public int compare(CacheValue lhs, CacheValue rhs) {
			if (lhs.size() > rhs.size()) {
				return -1;
			}
			return 1;
		}
	};
}
