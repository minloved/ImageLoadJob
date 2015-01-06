package com.job.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;

import com.job.cache.CacheBitmapController;
import com.job.syn.JobDetail;
import com.job.syn.JobScheduler;
import com.job.utils.JobUtils;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:37:00
 * @Tags
 * @TODO 控制图片缓存目录.已经将图片下载的工作送给线程执行
 */
public class ImageJobController {

	/**
	 * 将tcm的下载文件保存为absolutePat
	 * 
	 * @param view
	 *            将加载完成的Bitmap 绑定到 该view上 显示
	 * @return
	 */

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static void load(String absolutePath, ImageJob imageJob) {
		JobUtils.checkIfInUIThread();
		if (imageJob == null) return;
		String uri = imageJob.getUri();
		Bitmap bmp = CacheBitmapController.singleInstance().opt(uri);
		View view = imageJob.getView();
		if (view != null) {
			if (bmp != null) {
				imageJob.display(bmp);
				return;
			}
			String defKey = String.valueOf(imageJob.defSrc);
			bmp = CacheBitmapController.singleInstance().opt(defKey);
			if (bmp == null) {
				Resources res = view.getResources();
				if (res != null) {
					try {
						BitmapDrawable bd = (BitmapDrawable) res.getDrawable(imageJob.defSrc);
						if (bd != null) bmp = bd.getBitmap();
						if (bmp != null) CacheBitmapController.singleInstance().put(defKey, bmp, false);
					} catch (OutOfMemoryError e) {
					}

				}
			}
			imageJob.disPlayDefault(new BitmapDrawable(bmp));
		}
		JobDetail job = JobScheduler.getJobInGroup(ImageJobPool.JOB_GROUP, uri);
		if (job != null) {
			if (job instanceof ImageJobPool) {
				ImageJobPool pool = (ImageJobPool) job;
				if (pool.getUrl().equals(uri)) {
					pool.addJob(imageJob);
					return;
				}
			}
		}
		ImageJobPool container = new ImageJobPool(uri, absolutePath);
		container.addJob(imageJob);
		JobScheduler.push(container);
	}
}
