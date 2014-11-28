package com.job.cache;

class CacheOverTime {

	private long mLastUseCacheTime;

	public CacheOverTime() {
		updateCacheTime();
	}

	public void updateCacheTime() {
		this.mLastUseCacheTime = System.currentTimeMillis();
	}

	public final boolean isOverTime(long maxTime) {
		if (mLastUseCacheTime > 0 && System.currentTimeMillis() - mLastUseCacheTime > maxTime) {
			return true;
		}
		return false;
	}

	public static interface CacheOverTimeInterface {
		public boolean isOverTime(long maxTime);

		public void updateCacheTime();
	}

}
