package com.job.image;

import java.lang.ref.WeakReference;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.job.image.ImageConfig.TargetConfig;

/**
 * 
 * @author Octopus
 * @Data 2014年11月26日
 * @Time 下午1:27:47
 * @Tags
 * @TODO TODO 当个图片下载对应的任务
 */
public class ImageJob {
	private static final ImageConfig EMPTY = new ImageConfig();
	private String mImageUrl;
	private ITCCTranslateToSdcardListener mTransferListener;

	private WeakReference<View> mWeakView;
	private ITCCDisplayer displayer;
	private ImageDrawable defDrawable;
	int defSrc = -1;
	int defErr = -1;

	private WeakReference<View> mWeakProView;
	private ImageConfig mConfig = EMPTY;

	private ImageJobPool mImageJobPool;

	private ImageJob(String imageUrl) {
		this.mImageUrl = imageUrl;
	}

	/**
	 * @param imageurl
	 *            即将加载的图片连接地址
	 * @return
	 */
	public static ImageJob obtainJob(String imageurl) {
		return new ImageJob(imageurl);
	}

	/**
	 * 
	 * @param view
	 *            将加载完成的Bitmap 绑定到 该view上 显示
	 * @return
	 */

	public static ImageJob obtainJob(String imageUrl, View view, View pView, ITCCDisplayer displayer, int defSrc, int defErr, ImageConfig config) {
		if (displayer == null) displayer = DEFAULT;
		ImageJob iJob = null;
		Drawable drawable = displayer.getDrawable(view);
		if (drawable != null && drawable instanceof ImageDrawable) {
			// 从任务队列里移除
			ImageDrawable imageDrawable = (ImageDrawable) drawable;
			if (imageDrawable != null) imageDrawable.removeFromImagePool();
		}
		if (iJob == null) iJob = ImageJob.obtainJob(imageUrl);
		iJob.update(view, pView, displayer, defSrc, defErr, config);
		return iJob;
	};

	public static ImageJob obtainJob(String imageUrl, ITCCTranslateToSdcardListener transListener, View pView) {
		ImageJob ret = new ImageJob(imageUrl);
		if (transListener != null) ret.mTransferListener = transListener;
		if (pView != null) ret.mWeakProView = new WeakReference<View>(pView);
		return ret;
	}

	public View getView() {
		if (mWeakView == null) return null;
		return mWeakView.get();
	}

	public View getProgressView() {
		if (mWeakProView == null) return null;
		return mWeakProView.get();
	}

	public final void update(View view, View pView, ITCCDisplayer displayer, int defSrc, int defErr, ImageConfig config) {
		if (config == null) config = new ImageConfig();
		if (view != null) {
			config.mTargetConfig = TargetConfig.BUILD_BITMAP_AND_CACHE;
			mWeakView = new WeakReference<View>(view);
		} else {
			config.mTargetConfig = TargetConfig.ONLY_SAVE_TO_FILE;
			mWeakView = null;
		}
		if (pView != null) {
			mWeakProView = new WeakReference<View>(pView);
		} else {
			mWeakProView = null;
		}
		this.displayer = displayer;
		this.defSrc = defSrc;
		this.defErr = defErr;
		mConfig = config;
	}

	public final boolean removeFromJobPool() {
		if (mImageJobPool != null) {
			mImageJobPool.removeJob(this);
			mImageJobPool = null;
			return true;
		}
		return false;
	}

	public final void addToJobPool(ImageJobPool ijpool) {
		this.mImageJobPool = ijpool;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public final boolean display(Bitmap bmp) {
		boolean deal = false;
		if (displayer != null) {
			if (mWeakView != null) {
				View tv = mWeakView.get();
				View pView = null;
				if (mWeakProView != null) pView = mWeakProView.get();
				if (tv != null) {
					if (bmp != null) displayer.onDisplay(tv, bmp, pView);
					else if (defErr > 0) displayer.onDisplayError(tv, defErr, pView);
				}
				deal = true;
			}
		}
		return deal;
	}

	public final void onTranslateToSdcard(boolean success) {
		if (mTransferListener == null) return;
		mTransferListener.onTranslateResult(success, mImageUrl, getProgressView());
	}

	public final boolean disPlayDefault(Drawable drawable) {
		this.defDrawable = new ImageDrawable(drawable, this);
		boolean deal = false;
		View tv = getView();
		if (tv != null && displayer != null) {
			displayer.onDisplayDefault(tv, defDrawable, getProgressView());
			deal = true;
		}
		return deal;
	}

	public final boolean update(int progress) {
		boolean deal = false;
		if (mWeakProView != null) {
			View pView = mWeakProView.get();
			if (displayer != null) {
				displayer.onUpdate(pView, progress);
				deal = true;
			}
			if (mTransferListener != null) {
				mTransferListener.onUpdate(pView, progress);
				if (!deal) deal = true;
			}
		}
		return deal;
	}

	public String getUri() {
		return mImageUrl;
	}

	public ImageConfig getImageConfig() {
		return mConfig;
	}

	public void setmConfig(ImageConfig mConfig) {
		this.mConfig = mConfig;
	}

	public void release() {
		mImageJobPool = null;
		mImageUrl = null;
		mTransferListener = null;

		if (mWeakView != null) {
			mWeakView.clear();
			mWeakView = null;
		}
		if (defDrawable != null) {
			if (defDrawable.weakJob != null) {
				defDrawable.weakJob.clear();
			}
		}
		if (mWeakProView != null) {
			mWeakProView.clear();
			mWeakProView = null;
		}
	}

	public static ITCCDisplayer DEFAULT = new ITCCDisplayer() {

		@Override
		public void onDisplay(View src, Bitmap result, View pView) {
			// TODO Auto-generated method stub
			if (src instanceof ImageView) {
				((ImageView) src).setScaleType(ScaleType.CENTER_CROP);
				((ImageView) src).setImageBitmap(result);
			}
		}

		@Override
		public void onDisplayDefault(View src, Drawable imageDrawable, View pView) {
			if (src instanceof ImageView && imageDrawable != null) {
				((ImageView) src).setScaleType(ScaleType.CENTER_INSIDE);
				((ImageView) src).setImageDrawable(imageDrawable);
			}
		}

		@Override
		public void onDisplayError(View src, int errID, View pView) {
			if (src instanceof ImageView && errID > 0) {
				((ImageView) src).setScaleType(ScaleType.CENTER_INSIDE);
				((ImageView) src).setImageResource(errID);
			}
		}

		@Override
		public void onUpdate(View pView, int progress) {
			if (pView != null) {
			}
		}

		@Override
		public Drawable getDrawable(View src) {
			if (src instanceof ImageView) {
				return ((ImageView) src).getDrawable();
			}
			return null;
		}

	};

	public static interface ITCCDisplayer {

		public void onDisplayDefault(View src, Drawable drawable, View pView);

		public void onDisplayError(View src, int errID, View pView);

		public void onDisplay(View src, Bitmap result, View pView);

		public void onUpdate(View pView, int progress);

		public Drawable getDrawable(View src);

	}

	public static interface ITCCTranslateToSdcardListener {

		public void onUpdate(View pView, int progress);

		public void onTranslateResult(boolean success, String imageUrl, View pView);
	}

}
