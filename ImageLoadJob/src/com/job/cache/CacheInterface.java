package com.job.cache;

interface CacheInterface<K> {
	/**
	 * 
	 * @param uriKey
	 *            网络图片,本地文件路径
	 * @param cache
	 *            缓存内容
	 * @return
	 */
	public boolean put(K uriKey, Object cache);

	/**
	 * 
	 * @param uriKey
	 *            网络图片,本地文件路径
	 * @return
	 */
	public Object opt(K uriKey);
}
