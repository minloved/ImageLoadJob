package com.job.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Octopus(将图片保存到sdcard缓存)
 * @Data 2014年11月26日
 * @Time 下午3:35:07
 * @Tags
 * @TODO TODO
 */
public class CacheBytesDisk implements CacheInterface<String> {
	private static CacheBytesDisk sInstance = new CacheBytesDisk();

	public static CacheBytesDisk instance() {
		return sInstance;
	}

	private CacheBytesDisk() {
	}

	/**
	 * 将会对cache强制转换为byte[]
	 */
	@Override
	public boolean put(String path, Object cache) {
		if (!UtilsCache.removeFileIfExsit(path) && !(cache instanceof byte[])) {
			return false;
		}
		byte[] byteArr = (byte[]) cache;
		OutputStream oStream = null;
		try {
			File file = new File(path);
			oStream = new FileOutputStream(file);
			UtilsStream.writeByteArr(oStream, byteArr);
			file.setLastModified(System.currentTimeMillis());
			return true;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			UtilsCache.removeFileIfExsit(path);
		} finally {
			UtilsStream.safeCloseOutputStream(oStream);
		}
		return false;
	}

	@Override
	public byte[] opt(String path) {
		if (null == UtilsCache.exsit(path)) {
			return null;
		}
		InputStream inStream = null;
		try {
			File file = new File(path);
			inStream = new FileInputStream(file);
			file.setLastModified(System.currentTimeMillis());
			return UtilsStream.readByteArr(inStream);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			UtilsCache.removeFileIfExsit(path);
		} finally {
			UtilsStream.safeCloseInputStream(inStream);
		}
		return null;
	}

}