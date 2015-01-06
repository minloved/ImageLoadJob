/**
 * 
 */
package com.job.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * @author Octopus
 * @Data 2015年1月6日
 * @Time 上午9:05:34
 * @Tags
 * @TODO TODO
 */
public class ImageConfig {
	public TargetConfig mTargetConfig = TargetConfig.ONLY_SAVE_TO_FILE;
	public Config mConfig = Config.ARGB_8888;
	public int mTargetWidth = 0;
	public int mTargetHeight;

	public static enum TargetConfig {
		// 只是保存到文件内,用于仅仅是下载
		ONLY_SAVE_TO_FILE(0),
		// 生成Bitmap,但是不缓存到内存里
		ONLY_BUILD_BITMAP(1),
		// 将生成的Bitmap缓存到缓存里
		BUILD_BITMAP_AND_CACHE(2);
		TargetConfig(int value) {
			this.value = value;
		}

		private int value;

		public boolean compare(TargetConfig config) {
			return value > config.value;
		};
	}

	public Bitmap convertBitmap(Bitmap bmp) {
		return bmp;
	}

	public void copyConfig(ImageConfig imageConfig) {
		if (imageConfig == null) return;
	}
}
