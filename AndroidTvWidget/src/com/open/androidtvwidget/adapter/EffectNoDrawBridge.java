package com.open.androidtvwidget.adapter;

import com.open.androidtvwidget.view.MainUpView;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * 为了兼容4.3以下版本的 AnimBridge. <br>
 * 使用方法： MainUpView.setAnimBridge(newAnimNoDrawBridge()); <br>
 * 如果边框带了阴影效果，使用这个函数自行调整: MainUpView.setDrawUpRectPadding(-12);
 * 
 * @author hailongqiu
 *
 */
public class EffectNoDrawBridge extends OpenEffectBridge {
	private AnimatorSet mCurrentAnimatorSet;

	@Override
	public void onInitBridge(MainUpView view) {
		super.onInitBridge(view);
		/**
		 * 防止边框第一次出现,<br>
		 * 从另一个地方飘过来的问题.<br>
		 */
		view.setVisibility(View.INVISIBLE);
	}

	/**
	 * 设置背景，边框不使用绘制.
	 */
	@Override
	public void setUpRectResource(int resId) {
		getMainUpView().setBackgroundResource(resId);
	}

	@Override
	public void setUpRectDrawable(Drawable upRectDrawable) {
		getMainUpView().setBackgroundDrawable(upRectDrawable);
	}

	@Override
	public void onOldFocusView(View oldFocusView, float scaleX, float scaleY) {
		if (!isAnimEnabled())
			return;
		if (oldFocusView != null) {
			oldFocusView.animate().scaleX(scaleX).scaleY(scaleY).setDuration(getTranDurAnimTime()).start();
		}
	}

	@Override
	public void onFocusView(View focusView, float scaleX, float scaleY) {
		if (!isAnimEnabled())
			return;
		if (focusView != null) {
			/**
			 * 我这里重写了onFocusView. <br>
			 * 并且交换了位置. <br>
			 * 你可以写自己的动画效果. <br>
			 */
			runTranslateAnimation(focusView, scaleX, scaleY);
			focusView.animate().scaleX(scaleX).scaleY(scaleY).setDuration(getTranDurAnimTime()).start();
		}
	}

	/**
	 * 重写边框移动函数.
	 */
	@Override
	public void flyWhiteBorder(final View focusView, float x, float y, float scaleX, float scaleY) {
		Rect paddingRect = getDrawUpRect();
		int newWidth = 0;
		int newHeight = 0;
		int oldWidth = 0;
		int oldHeight = 0;
		if (focusView != null) {
			newWidth = (int) (focusView.getMeasuredWidth() * scaleX) + (paddingRect.left + paddingRect.right);
			newHeight = (int) (focusView.getMeasuredHeight() * scaleY) + (paddingRect.top + paddingRect.bottom);
			x = x + ((focusView.getMeasuredWidth() - newWidth) / 2);
			y = y + ((focusView.getMeasuredHeight() - newHeight) / 2);
		}

		// 取消之前的动画.
		if (mCurrentAnimatorSet != null)
			mCurrentAnimatorSet.cancel();

		oldWidth = getMainUpView().getMeasuredWidth();
		oldHeight = getMainUpView().getMeasuredHeight();

		ObjectAnimator transAnimatorX = ObjectAnimator.ofFloat(getMainUpView(), "translationX", x);
		ObjectAnimator transAnimatorY = ObjectAnimator.ofFloat(getMainUpView(), "translationY", y);
		// BUG，因为缩放会造成图片失真(拉伸).
		// hailong.qiu 2016.02.26 修复 :)
		ObjectAnimator scaleXAnimator = ObjectAnimator.ofInt(new ScaleView(getMainUpView()), "width", oldWidth,
				(int) newWidth);
		ObjectAnimator scaleYAnimator = ObjectAnimator.ofInt(new ScaleView(getMainUpView()), "height", oldHeight,
				(int) newHeight);
		//
		AnimatorSet mAnimatorSet = new AnimatorSet();
		mAnimatorSet.playTogether(transAnimatorX, transAnimatorY, scaleXAnimator, scaleYAnimator);
		mAnimatorSet.setInterpolator(new DecelerateInterpolator(1));
		mAnimatorSet.setDuration(getTranDurAnimTime());
		mAnimatorSet.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (isVisibleWidget()) {
					getMainUpView().setVisibility(View.GONE);
				}
				if (getNewAnimatorListener() != null)
					getNewAnimatorListener().onAnimationStart(EffectNoDrawBridge.this, focusView, animation);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				getMainUpView().setVisibility(isVisibleWidget() ? View.GONE : View.VISIBLE);
				if (getNewAnimatorListener() != null)
					getNewAnimatorListener().onAnimationEnd(EffectNoDrawBridge.this, focusView, animation);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		mAnimatorSet.start();
		mCurrentAnimatorSet = mAnimatorSet;
	}

	/**
	 * 重写该函数，<br>
	 * 不进行绘制 边框和阴影.
	 */
	@Override
	public boolean onDrawMainUpView(Canvas canvas) {
		return false;
	}

}
