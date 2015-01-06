package com.job.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

abstract class CacheController<K, V extends CacheValue<K>> implements CacheInterface<K> {
	private final HashMap<K, V> mCacheMap = new HashMap<K, V>();
	private final LinkedList<V> mLinkedList = new LinkedList<V>();
	private final ReentrantLock mLock = new ReentrantLock();
	public final static long DEFAULT_MAX_CACHE_SIZE;
	static {
		long M = Runtime.getRuntime().maxMemory() >> 20;
		M = M > 150 ? 120 : M;
		DEFAULT_MAX_CACHE_SIZE = M / 6 << 20;
	}
	private long mCachedSize = 0;
	private long mMaxCacheSize = 0;

	public CacheController() {
		mCachedSize = 0;
		mMaxCacheSize = DEFAULT_MAX_CACHE_SIZE;
	}

	public boolean put(K key, Object obj, boolean canRemoved) {
		if (obj == null) return false;
		mLock.lock();
		V value = mCacheMap.get(key);
		if (value != null && value.value() != null) {
			value.updateCacheTime();
			value.setRemoved(canRemoved);
			mLinkedList.remove(value);
			mLinkedList.addFirst(value);
			mLock.unlock();
			return true;
		}
		value = createCacheValue(key, obj);
		mCachedSize += value.size();
		mCacheMap.put(key, value);
		mLinkedList.addFirst(value);
		recyleOverSizeBorder();
		mLock.unlock();
		return false;
	}

	@Override
	public boolean put(K key, Object obj) {
		return put(key, obj, true);
	}

	@Override
	public Object opt(K uriKey) {
		mLock.lock();
		V value = mCacheMap.get(uriKey);
		if (value != null) {
			if (value.value() != null) {
				value.updateCacheTime();
				Object data = value.cloneValue();
				mLinkedList.remove(value);
				mLinkedList.addFirst(value);
				mLock.unlock();
				if (data != null) return data;
				return value.value();
			}
			mLinkedList.remove(value);
			mCachedSize -= value.size();
			mCacheMap.remove(uriKey);
		}
		mLock.unlock();
		return null;
	}

	/**
	 * 释放最大缓存的数据
	 */

	public boolean recyleOverSizeBorder() {
		if (mCachedSize < mMaxCacheSize) return false;
		LinkedList<V> noRemoved = new LinkedList<V>();
		int remain = remainCacheSize();
		while (!mLinkedList.isEmpty() && mCachedSize > remain) {
			V entry = mLinkedList.removeLast();
			if (!entry.canRemoved()) {
				noRemoved.addLast(entry);
				continue;
			}
			mCachedSize -= entry.size();
			mCacheMap.remove(entry.key());
			entry.recyle();
		}
		while (!noRemoved.isEmpty()) {
			mLinkedList.addFirst(noRemoved.removeFirst());
		}
		System.gc();
		return true;
	}

	private int remainCacheSize() {
		return (int) (mMaxCacheSize * 0.8);
	}

	public long getMaxCacheSize() {
		return mMaxCacheSize;
	}

	public void setMaxMemorySize(long mms) {
		mLock.lock();
		this.mMaxCacheSize = mms;
		recyleOverSizeBorder();
		mLock.unlock();
	}

	public abstract V createCacheValue(K key, Object value);

}
