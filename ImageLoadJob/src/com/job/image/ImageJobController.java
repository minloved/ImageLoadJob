package com.job.image;

import android.graphics.Bitmap;

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

	@SuppressWarnings("rawtypes")
	public static void load(String absolutePath, ImageJob tcm) {
		JobUtils.checkIfInUIThread();
		if (tcm != null) {
			String uri = tcm.getUri();
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
			ImageJobPool container = new ImageJobPool(uri, absolutePath);
			container.addJob(tcm);
			JobScheduler.push(container);
		}

	}
}
