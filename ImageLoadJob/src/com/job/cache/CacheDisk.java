package com.job.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import com.job.utils.ImageUtils;
import com.job.utils.JobUtils;
import com.job.utils.StreamUtils;

/**
 * 
 * @author Octopus(将图片保存到sdcard缓存)
 * @Data 2014年11月26日
 * @Time 下午3:35:07
 * @Tags
 * @TODO TODO
 */
public class CacheDisk implements CacheInterface<String> {
	private static CacheDisk sInstance = new CacheDisk();

	public static CacheDisk instance() {
		return sInstance;
	}

	private CacheDisk() {
	}

	/**
	 * 将会对cache强制转换为byte[]
	 */
	@Override
	public synchronized boolean put(String path, Object cache) {
		if (!JobUtils.removeFileIfExsit(path) && !(cache instanceof byte[])) {
			return false;
		}
		byte[] byteArr = (byte[]) cache;
		OutputStream oStream = null;
		String tempPath = path + ".tmp";
		try {
			boolean deleted = JobUtils.removeFileIfExsit(tempPath);
			if (!deleted) return false;
			File file = new File(tempPath);
			oStream = new FileOutputStream(file);
			StreamUtils.writeBytesArrWithoutCheckByte(oStream, byteArr);
			StreamUtils.safeCloseOutputStream(oStream);
			oStream = null;
			file.setLastModified(System.currentTimeMillis());
			boolean success = file.renameTo(new File(path));
			if (success) return true;
			JobUtils.removeFileIfExsit(tempPath);
			return false;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			JobUtils.removeFileIfExsit(tempPath);
		} finally {
			StreamUtils.safeCloseOutputStream(oStream);
		}
		return false;
	}

	@Override
	public synchronized byte[] opt(String path) {
		if (null == JobUtils.exsit(path)) {
			return null;
		}
		try {
			return readCacheFromFile(path);
		} catch (IOException e) {
			JobUtils.removeFileIfExsit(path);
		}
		return null;
	}

	public byte[] readCacheFromFile(String path) throws IOException {
		InputStream inStream = null;
		try {
			File file = new File(path);
			inStream = new FileInputStream(file);
			file.setLastModified(System.currentTimeMillis());
			return StreamUtils.readBytesArrWithoutCheckByte(inStream);
		} finally {
			StreamUtils.safeCloseInputStream(inStream);
		}
	}

}
