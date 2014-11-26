package com.job.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.job.cache.CacheBitmapController;
import com.job.cache.CacheBytesController;
import com.job.cache.CacheBytesDisk;
import com.job.cache.UtilsStream;
import com.job.syn.JobDetail;

class ImageJobPool extends JobDetail<Integer, Bitmap> {
	private static final String TAG = ImageJobPool.class.getSimpleName();
	public static final String JOB_GROUP = "image_load_job";
	private static final int BUFFER_LENGTH = 1024 * 4;
	public static final int LOAD_MAX_PROGRESS = 100;
	public static final long LOAD_INTERVAL_TIME = 20;

	private static final CacheBytesDisk sBytesDiskCache = CacheBytesDisk.instance();
	private static final CacheBytesController sMemoryCache = new CacheBytesController();
	private final LinkedList<ImageJob> mTccJobs = new LinkedList<ImageJob>();
	private final String cachePath;

	String uri;

	public ImageJobPool(String uri, String cacheDir) {
		this.uri = uri;
		cachePath = cacheDir + ImageUtils.md5Encrypt(uri);
	}

	public void addJob(ImageJob job) {
		ImageUtils.checkIfInUIThread();
		mTccJobs.add(job);
		job.addToJobPool(this);
	}

	public void removeJob(ImageJob job) {
		ImageUtils.checkIfInUIThread();
		mTccJobs.remove(job);
		if (mTccJobs.isEmpty()) {
			abolish();
		}
	}

	public void release() {
		while (!mTccJobs.isEmpty()) {
			ImageJob job = mTccJobs.remove(0);
			job.release();
		}
		uri = null;
	}

	private byte[] loadFromWebServices() {
		final AndroidHttpClient client = AndroidHttpClient.newInstance("SLAPI");
		HttpParams httpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 30 * 1000);
		final HttpGet getRequest = new HttpGet(uri);
		try {
			HttpResponse response = client.execute(getRequest);
			final HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
			length = Math.max(1, length);
			InputStream inputStream = null;
			inputStream = entity.getContent();
			ByteArrayOutputStream bos = null;
			try {
				bos = new ByteArrayOutputStream();
				byte[] buf = new byte[BUFFER_LENGTH];
				int ch = -1;
				int count = 0;
				long time = System.currentTimeMillis();
				while ((ch = inputStream.read(buf)) != -1) {
					bos.write(buf, 0, ch);
					count += ch;
					long now = System.currentTimeMillis();
					if ((now - time) < LOAD_INTERVAL_TIME)
						continue;
					time = now;
					int progress = (int) ((count / (float) length) * LOAD_MAX_PROGRESS);
					update(progress);
				}
				return bos.toByteArray();
			} catch (Exception e) {
			} finally {
				UtilsStream.safeCloseOutputStream(bos);
				UtilsStream.safeCloseInputStream(inputStream);
				entity.consumeContent();
			}
		} catch (IOException e) {
			if (getRequest != null && !getRequest.isAborted())
				getRequest.abort();
		} catch (IllegalStateException e) {
			if (getRequest != null && !getRequest.isAborted())
				getRequest.abort();
			Log.w(TAG, "Incorrect URL: " + uri);
		} catch (Exception e) {
			if (getRequest != null && !getRequest.isAborted())
				getRequest.abort();
			Log.w(TAG, "Error while retrieving bitmap from " + uri, e);
		} finally {
			client.close();
		}
		return null;
	}

	@Override
	public Bitmap work() {
		byte[] data = null;
		File file = new File(cachePath);
		// 确保缓存文件存在
		if (!file.exists() || !file.isFile()) {
			// 如果资源没有下载,则主动将数据缓存到Sdcard内
			Log.w(TAG, "loadFromWebServices");
			data = loadFromWebServices();
			if (data != null)
				sBytesDiskCache.put(cachePath, data);
		}
		// 如果Bitmap缓存中已经缓存了图片资源,则优先使用图片资源
		Bitmap bmp = CacheBitmapController.singleInstance().opt(uri);
		if (bmp == null) {
			// 如果Bitmap缓存中没有缓存图片资源
			if (data == null) {
				Log.i(TAG, "loadFromMemoryCache");
				// 如果Bitmap缓存没有缓存该图片资源,则到字节缓存中去获取缓存的字节
				data = sMemoryCache.opt(uri);
				if (data == null) {
					// 如果字节缓存内也没有获取到数据,就去Sdcard去获取
					Log.i(TAG, "loadFromDiskCache");
					data = sBytesDiskCache.opt(cachePath);
					// 如果从Sdcard内读取缓存成功则缓存到字节缓存内
					if (data != null)
						sMemoryCache.put(uri, data);
				}
			} else {
				sMemoryCache.put(uri, data);
			}
			if (data != null) {
				bmp = ImageUtils.createBitmapByByteArr(data, null);
				if (bmp != null) {
					CacheBitmapController.singleInstance().put(uri, bmp);
				}
			}
		}
		return bmp;
	}

	@Override
	public void onResult(Bitmap result) {
		boolean used = false;
		File file = new File(cachePath);
		boolean loadToFileSuccess = false;
		if (file.exists() && file.isFile()) {
			loadToFileSuccess = true;
		}
		for (ImageJob job : mTccJobs) {
			boolean u = job.display(result);
			job.fileLoadSuccess(loadToFileSuccess, cachePath);
			used = u ? true : used;
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
}
