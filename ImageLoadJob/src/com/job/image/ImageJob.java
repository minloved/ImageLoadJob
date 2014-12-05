package com.job.image;

import java.lang.ref.WeakReference;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 
 * @author Octopus
 * @Data 2014年11月26日
 * @Time 下午1:27:47
 * @Tags
 * @TODO TODO 当个图片下载对应的任务
 */
public class ImageJob {
	private String uri;

	private WeakReference<View> weakView;
	private ITccConvertor convertor = ImageJob.NULL;
	private ITCCDisplayer displayer;
	private ImageDrawable defDrawable;
	private int defErr;
	private WeakReference<View> progView;

	private int idealWidth;
	private int idealHeight;
	private ImageJobPool imageJobPool;

	private ImageJob(String uri) {
		this.uri = uri;
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
	public static ImageJob obtainJob(String uri, View view, ITCCDisplayer displayer, Drawable defSrc) {
		return obtainJob(uri, view, null, displayer, defSrc, -1);
	};

	public static ImageJob obtainJob(String uri, View view, View progView, ITCCDisplayer displayer, Drawable defSrc, int defErr) {
		return obtainJob(uri, view, null, displayer, defSrc, defErr, 0, 0);
	};

	public static ImageJob obtainJob(String uri, View view, View progView, ITCCDisplayer displayer, Drawable defSrc, int width, int height) {
		return obtainJob(uri, view, null, displayer, defSrc, -1, width, height);
	};

	public static ImageJob obtainJob(String uri, View view, View pView, ITCCDisplayer displayer, Drawable defSrc, int defErr, int width, int height) {
		if (displayer == null)
			displayer = DEFAULT;
		Drawable drawable = displayer.getDrawable(view);
		if (drawable != null && drawable instanceof ImageDrawable) {
			ImageDrawable imageDrawable = (ImageDrawable) drawable;
			WeakReference<ImageJob> wij = imageDrawable.imageJob;
			if (wij != null) {
				ImageJob ij = wij.get();
				if (ij != null) {
					ij.update(view, pView, displayer, imageDrawable, defErr, width, height);
					if (ij.uri != null && ij.uri.equals(uri)) {
						return null;
					} else {
						ij.removeFromJobPool();
						ij.uri = uri;
						return ij;
					}
				}
			}
		}
		ImageJob iJob = ImageJob.obtainJob(uri);
		ImageDrawable iDrawable = new ImageDrawable(defSrc, iJob);
		displayer.onDisplayDefault(view, iDrawable);
		iJob.update(view, pView, displayer, iDrawable, defErr, width, height);
		iJob.addOnAttachStateChangeListener();
		return iJob;
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void addOnAttachStateChangeListener() {
		if (weakView != null) {
			View view = weakView.get();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
				view.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
					@Override
					public void onViewDetachedFromWindow(View v) {
						v.removeOnAttachStateChangeListener(this);
						removeFromJobPool();
					}

					@Override
					public void onViewAttachedToWindow(View v) {
						v.removeOnAttachStateChangeListener(this);
					}
				});
			}
		}
	}

	private void update(View view, View pView, ITCCDisplayer displayer, ImageDrawable defSrc, int defErr, int width, int height) {
		if (view != null) {
			weakView = new WeakReference<View>(view);
		} else {
			weakView = null;
		}
		if (pView != null) {
			progView = new WeakReference<View>(pView);
		} else {
			progView = null;
		}
		this.displayer = displayer;
		defDrawable = defSrc;
		this.defErr = defErr;
		this.idealWidth = width;
		this.idealHeight = height;
	}

	/**
	 * 
	 * @param convertor
	 *            返回一系列变化后的图片
	 * @return
	 */
	public ImageJob after(ITccConvertor convertor) {
		if (convertor != null) {
			this.convertor = convertor;
		}
		return this;
	}

	/**
	 * 
	 * @return 内存缓存图片的Key值
	 */
	public String key() {
		return uri + "*" + idealWidth + "#" + idealHeight + convertor.key();
	}

	public final boolean removeFromJobPool() {
		if (imageJobPool != null) {
			imageJobPool.removeJob(this);
			imageJobPool = null;
			return true;
		}
		return false;
	}

	public final void addToJobPool(ImageJobPool ijpool) {
		this.imageJobPool = ijpool;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public final boolean display(Bitmap bmp) {
		boolean deal = false;
		if (displayer != null) {
			if (weakView != null) {
				View tv = weakView.get();
				if (tv != null) {
					if (bmp != null) {
						displayer.onDisplay(tv, bmp);
					} else {
						if (defErr > 0) {
							displayer.onDisplayError(tv, defErr);
						}
					}
				}
				deal = true;
			}
		}
		return deal;
	}

	public final void fileLoadSuccess(boolean success, String filePath) {
		if (displayer != null) {
			View view = progView != null ? progView.get() : null;
			if (TextUtils.isEmpty(filePath)) {
				displayer.onFailed(view);
			} else {
				displayer.onSuccessed(view, filePath);
			}
		}
	}

	public final boolean disPlayDefault() {
		boolean deal = false;
		if (weakView != null) {
			View tv = weakView.get();
			if (tv != null && displayer != null) {
				displayer.onDisplayDefault(tv, defDrawable);
				deal = true;
			}
		}
		return deal;
	}

	public final boolean update(int progress) {
		boolean deal = false;
		if (progView != null) {
			if (displayer != null) {
				displayer.onUpdate(progView.get(), progress);
				deal = true;
			}
		}
		return deal;
	}

	public View getView() {
		if (weakView != null) {
			return weakView.get();
		}
		return null;
	}

	public String getUri() {
		return uri;
	}

	public void release() {
		imageJobPool = null;
		progView = null;
		uri = null;
		if (weakView != null) {
			weakView = null;
		}
	}

	/**
	 * 空转换
	 */
	public static final ITccConvertor NULL = new ITccConvertor() {

		@Override
		public Bitmap onConvert(Bitmap src) {
			return src;
		}

		@Override
		public String key() {
			return "src";
		}
	};

	public static ITCCDisplayer DEFAULT = new ITCCDisplayer() {

		@Override
		public void onDisplay(View src, Bitmap result) {
			// TODO Auto-generated method stub
			if (src instanceof ImageView) {
				((ImageView) src).setScaleType(ScaleType.CENTER_CROP);
				((ImageView) src).setImageBitmap(result);
			}
		}

		@Override
		public void onDisplayDefault(View src, Drawable imageDrawable) {
			if (src instanceof ImageView && imageDrawable != null) {
				((ImageView) src).setScaleType(ScaleType.CENTER_INSIDE);
				((ImageView) src).setImageDrawable(imageDrawable);
			}
		}

		@Override
		public void onDisplayError(View src, int errID) {
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

		@Override
		public void onSuccessed(View proView, String filePath) {
		}

		@Override
		public void onFailed(View proView) {
		}

	};

	public static interface ITccConvertor {

		/**
		 * @return 转换器关键字,用于判断是否是同一个转换器
		 */
		public String key();

		/**
		 * 
		 * @param src
		 *            原图片
		 * @return 目标图片
		 */
		public Bitmap onConvert(Bitmap src);

	}

	public static interface ITCCDisplayer {

		public void onDisplayDefault(View src, Drawable drawable);

		public void onDisplayError(View src, int errID);

		public void onDisplay(View src, Bitmap result);

		public void onUpdate(View pView, int progress);

		public Drawable getDrawable(View src);

		public void onSuccessed(View proView, String filePath);

		public void onFailed(View proView);

	}

	private static final class ImageDrawable extends Drawable {
		private WeakReference<ImageJob> imageJob;
		private Drawable drawable;

		private ImageDrawable(Drawable defDrawable, ImageJob job) {
			drawable = defDrawable;
			if (job != null) {
				this.imageJob = new WeakReference<ImageJob>(job);
			} else {
				imageJob = null;
				drawable = null;
			}
		}

		@Override
		public void draw(Canvas canvas) {
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}

		@Override
		public void setAlpha(int i) {
			if (drawable != null) {
				drawable.setAlpha(i);
			}
		}

		@Override
		public void setColorFilter(ColorFilter colorFilter) {
			if (drawable != null) {
				drawable.setColorFilter(colorFilter);
			}
		}

		@Override
		public int getOpacity() {
			return drawable == null ? Byte.MAX_VALUE : drawable.getOpacity();
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			if (drawable != null) {
				drawable.setBounds(left, top, right, bottom);
			}
		}

		@Override
		public void setBounds(Rect bounds) {
			if (drawable != null) {
				drawable.setBounds(bounds);
			}
		}

		@Override
		public void setChangingConfigurations(int configs) {
			if (drawable != null) {
				drawable.setChangingConfigurations(configs);
			}
		}

		@Override
		public int getChangingConfigurations() {
			return drawable == null ? 0 : drawable.getChangingConfigurations();
		}

		@Override
		public void setDither(boolean dither) {
			if (drawable != null) {
				drawable.setDither(dither);
			}
		}

		@Override
		public void setFilterBitmap(boolean filter) {
			if (drawable != null) {
				drawable.setFilterBitmap(filter);
			}
		}

		@Override
		public void invalidateSelf() {
			if (drawable != null) {
				drawable.invalidateSelf();
			}
		}

		@Override
		public void scheduleSelf(Runnable what, long when) {
			if (drawable != null) {
				drawable.scheduleSelf(what, when);
			}
		}

		@Override
		public void unscheduleSelf(Runnable what) {
			if (drawable != null) {
				drawable.unscheduleSelf(what);
			}
		}

		@Override
		public void setColorFilter(int color, PorterDuff.Mode mode) {
			if (drawable != null) {
				drawable.setColorFilter(color, mode);
			}
		}

		@Override
		public void clearColorFilter() {
			if (drawable != null) {
				drawable.clearColorFilter();
			}
		}

		@Override
		public boolean isStateful() {
			return drawable != null && drawable.isStateful();
		}

		@Override
		public boolean setState(int[] stateSet) {
			return drawable != null && drawable.setState(stateSet);
		}

		@Override
		public int[] getState() {
			return drawable == null ? null : drawable.getState();
		}

		@Override
		public Drawable getCurrent() {
			return drawable == null ? null : drawable.getCurrent();
		}

		@Override
		public boolean setVisible(boolean visible, boolean restart) {
			return drawable != null && drawable.setVisible(visible, restart);
		}

		@Override
		public Region getTransparentRegion() {
			return drawable == null ? null : drawable.getTransparentRegion();
		}

		@Override
		public int getIntrinsicWidth() {
			return drawable == null ? 0 : drawable.getIntrinsicWidth();
		}

		@Override
		public int getIntrinsicHeight() {
			return drawable == null ? 0 : drawable.getIntrinsicHeight();
		}

		@Override
		public int getMinimumWidth() {
			return drawable == null ? 0 : drawable.getMinimumWidth();
		}

		@Override
		public int getMinimumHeight() {
			return drawable == null ? 0 : drawable.getMinimumHeight();
		}

		@Override
		public boolean getPadding(Rect padding) {
			return drawable != null && drawable.getPadding(padding);
		}

		@Override
		public Drawable mutate() {
			return drawable == null ? null : drawable.mutate();
		}

		@Override
		public ConstantState getConstantState() {
			return drawable == null ? null : drawable.getConstantState();
		}
	}

}
