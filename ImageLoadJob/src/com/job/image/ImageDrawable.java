/**
 * 
 */
package com.job.image;

import java.lang.ref.WeakReference;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;

/**
 * @author Octopus
 * @Data 2015年1月6日
 * @Time 下午1:00:53
 * @Tags
 * @TODO TODO
 */
public class ImageDrawable extends Drawable {
	WeakReference<ImageJob> weakJob;
	Drawable drawable;

	ImageDrawable(Drawable defDrawable, ImageJob job) {
		drawable = defDrawable;
		if (job != null) {
			this.weakJob = new WeakReference<ImageJob>(job);
		} else {
			weakJob = null;
			drawable = null;
		}
	}

	public void removeFromImagePool() {
		if (weakJob == null) return;
		ImageJob imageJob = weakJob.get();
		if (imageJob == null) return;
		imageJob.removeFromJobPool();
		imageJob = null;
	}

	public void recyleImagePool() {
		weakJob = null;
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