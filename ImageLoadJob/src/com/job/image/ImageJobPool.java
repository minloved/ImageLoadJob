package com.job.image;

import java.io.File;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.util.Log;

import com.job.cache.CacheBitmapController;
import com.job.cache.CacheDisk;
import com.job.image.ImageConfig.TargetConfig;
import com.job.image.RemoteResourceToByteArray.OnRemoteProgressListener;
import com.job.syn.JobDetail;
import com.job.utils.ImageUtils;
import com.job.utils.JobUtils;

class ImageJobPool extends JobDetail<Integer, Bitmap> {
	private static final Object sSynLock = new Object();
	private static final String TAG = ImageJobPool.class.getSimpleName();
	public static final String JOB_GROUP = "IMAGE_LOAD_JOB";

	private final LinkedList<ImageJob> mTccJobs = new LinkedList<ImageJob>();
	private final String cachePath;

	private String uri;

	public ImageJobPool(String uri, String cacheDir) {
		this.uri = uri;
		cachePath = cacheDir;
	}

	public void addJob(ImageJob job) {
		JobUtils.checkIfInUIThread();
		mTccJobs.add(job);
		job.addToJobPool(this);
	}

	public void removeJob(ImageJob job) {
		JobUtils.checkIfInUIThread();
		mTccJobs.remove(job);
		if (mTccJobs.isEmpty()) abolish();
	}

	public void release() {
		while (!mTccJobs.isEmpty()) {
			ImageJob job = mTccJobs.remove(0);
			job.release();
		}
		uri = null;
	}

	@Override
	public Bitmap work() {
		byte[] data = null;
		File file = new File(cachePath);
		// 确保缓存文件存在
		boolean saved = true;
		if (!file.exists() || !file.isFile()) {
			// 如果资源没有下载,则主动将数据缓存到Sdcard内
			Log.w(TAG, "loadFromWebServices");
			data = RemoteResourceToByteArray.loadFromRemote(uri, new OnRemoteProgressListener() {
				@Override
				public void onProgress(int progress) {
					update(progress);
				}
			});
			if (data != null) saved = CacheDisk.instance().put(cachePath, data) && file.exists();
		}
		save(saved);
		// 如果文件不存在.而且从网络上读取失败的时候,查看是否有缓存
		if (data == null) {
			// 如果字节缓存内也没有获取到数据,就去Sdcard去获取
			Log.i(TAG, "loadFromDiskCache");
			data = CacheDisk.instance().opt(cachePath);
		}
		if (data == null) return null;
		synchronized (sSynLock) {
			return ImageUtils.createBitmapByByteArr(data, null);
		}
	}

	public void onTranslateToSdcard(boolean success) {
		for (ImageJob job : mTccJobs) {
			job.onTranslateToSdcard(success);
		}
	}

	@Override
	public void onResult(Bitmap result) {
		boolean used = false;
		boolean needCache = false;
		for (ImageJob job : mTccJobs) {
			boolean use = job.display(result);
			if (!used) used = use;
			ImageConfig config = job.getImageConfig();
			if (config == null) continue;
			if (!needCache) needCache = config.mTargetConfig == TargetConfig.BUILD_BITMAP_AND_CACHE;
		}
		if (!used) {
			if (result != null) {
				result.recycle();
				result = null;
			}
		} else if (needCache) {
			CacheBitmapController.singleInstance().put(uri, result);
		}
		release();
	}

	@Override
	public void onUpdate(Integer progress) {
		for (ImageJob job : mTccJobs) {
			job.update(progress);
		}
	}

	@Override
	public String group() {
		return JOB_GROUP;
	}

	@Override
	public String key() {
		return uri;
	}

	public String getUrl() {
		return uri;
	}
}
