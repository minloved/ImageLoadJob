/**
 * 
 */
package com.job.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.job.utils.StreamUtils;

/**
 * @author Octopus
 * @Data 2015年1月6日
 * @Time 上午9:25:09
 * @Tags
 * @TODO TODO
 */
public class RemoteResourceToByteArray {
	private static final String TAG = RemoteResourceToByteArray.class.getSimpleName();
	public static final int PROGRESS_MAX = 100;
	public static final int PROGRESS_MIN = 0;

	private static final int BUFFER_LENGTH = 1024 * 4;

	public static interface OnRemoteProgressListener {
		public void onProgress(int progress);
	}

	public static byte[] loadFromRemote(String uri, OnRemoteProgressListener listener) {
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
			InputStream inputStream = entity.getContent();
			ByteArrayOutputStream bos = null;
			try {
				bos = new ByteArrayOutputStream();
				byte[] buf = new byte[BUFFER_LENGTH];
				int ch = -1;
				int count = 0;
				while ((ch = inputStream.read(buf)) != -1) {
					bos.write(buf, 0, ch);
					if (listener == null) continue;
					count += ch;
					int progress = (int) ((count / (float) length) * PROGRESS_MAX);
					listener.onProgress(progress);
				}
				byte[] bytes = bos.toByteArray();
				StreamUtils.safeCloseInputStream(inputStream);
				StreamUtils.safeCloseOutputStream(bos);
				return bytes;
			} catch (Exception e) {
			} finally {
				entity.consumeContent();
				StreamUtils.safeCloseOutputStream(bos);
				StreamUtils.safeCloseInputStream(inputStream);
			}
		} catch (IOException e) {
			if (getRequest != null && !getRequest.isAborted()) getRequest.abort();
		} catch (IllegalStateException e) {
			if (getRequest != null && !getRequest.isAborted()) getRequest.abort();
			Log.w(TAG, "Incorrect URL: " + uri);
		} catch (Exception e) {
			if (getRequest != null && !getRequest.isAborted()) getRequest.abort();
			Log.w(TAG, "Error while retrieving bitmap from " + uri, e);
		} finally {
			client.close();
		}
		return null;
	}
}
