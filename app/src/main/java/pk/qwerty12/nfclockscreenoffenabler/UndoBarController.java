/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pk.qwerty12.nfclockscreenoffenabler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

public class UndoBarController {
	private View mBarView;
	private TextView mMessageView;
	private ViewPropertyAnimator mBarAnimator;
	private Handler mHideHandler = new Handler();

	private UndoListener mUndoListener;

	// State objects
	private Parcelable mUndoToken;
	private CharSequence mUndoMessage;
	private Context mContext;

	public interface UndoListener {
		void onUndo(Parcelable token);
	}

	public UndoBarController(View undoBarView, UndoListener undoListener) {
		mBarView = undoBarView;
		mBarAnimator = mBarView.animate();
		mUndoListener = undoListener;
		mContext = undoBarView.getContext();

		mMessageView = (TextView) mBarView.findViewById(R.id.undobar_message);
		mBarView.findViewById(R.id.undobar_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideUndoBar(false);
				mUndoListener.onUndo(mUndoToken);
			}
		});

		hideUndoBar(true);
	}

	public void showUndoBar(boolean immediate, CharSequence message, Parcelable undoToken) {
		mUndoToken = undoToken;
		mUndoMessage = message;
		mMessageView.setText(mUndoMessage);

		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, 5000);

		mBarView.setVisibility(View.VISIBLE);
		if (immediate) {
			mBarView.setAlpha(1);
		} else {
			mBarAnimator.cancel();
			mBarAnimator
			.alpha(1)
			.setDuration(
					mBarView.getResources()
					.getInteger(android.R.integer.config_shortAnimTime))
					.setListener(null);
		}
	}

	public void showUndoBar(boolean immediate, int resId, Parcelable undoToken) {
		showUndoBar(immediate, mContext.getString(resId), undoToken);
	}

	public void hideUndoBar(boolean immediate) {
		mHideHandler.removeCallbacks(mHideRunnable);
		if (immediate) {
			mBarView.setVisibility(View.GONE);
			mBarView.setAlpha(0);
			mUndoMessage = null;
			mUndoToken = null;

		} else {
			mBarAnimator.cancel();
			mBarAnimator
			.alpha(0)
			.setDuration(mBarView.getResources()
					.getInteger(android.R.integer.config_shortAnimTime))
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mBarView.setVisibility(View.GONE);
							mUndoMessage = null;
							mUndoToken = null;
						}
					});
		}
	}

	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hideUndoBar(false);
		}
	};
}