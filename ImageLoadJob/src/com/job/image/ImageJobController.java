package com.job.image;

import android.content.Context;
import android.graphics.Bitmap;

import com.job.cache.CacheBitmapController;
import com.job.syn.JobDetail;
import com.job.syn.JobScheduler;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:37:00
 * @Tags
 * @TODO 控制图片缓存目录.已经将图片下载的工作送给线程执行
 */
public class ImageJobController {
	private static String CACHE_DIR;
	static Context sContext;

	public static void init(Context context) {
		sContext = context;
	};

	public static boolean initImageCacheDir(String cacheDir) {
		CACHE_DIR = cacheDir;
		return ImageUtils.initImageCacheDir(CACHE_DIR);
	}

	/**
	 * 
	 * @param view
	 *            将加载完成的Bitmap 绑定到 该view上 显示
	 * @return
	 */

	@SuppressWarnings("rawtypes")
	public static void load(String uri, ImageJob tcm) {
		ImageUtils.checkIfInUIThread();
		ImageUtils.initImageCacheDir(CACHE_DIR);
		if (tcm != null) {
			Bitmap bmp = CacheBitmapController.singleInstance().opt(uri);
			if (tcm.getView() != null && bmp != null) {
				tcm.display(bmp);
				return;
			}
			JobDetail job = JobScheduler.getJobInGroup(ImageJobPool.JOB_GROUP, uri);
			if (job != null) {
				if (job instanceof ImageJobPool) {
					ImageJobPool pool = (ImageJobPool) job;
					if (pool.uri.equals(uri)) {
						pool.addJob(tcm);
						return;
					}
				}
			}
			ImageJobPool container = new ImageJobPool(uri, CACHE_DIR);
			container.addJob(tcm);
			JobScheduler.push(container);
		}

	}
}
