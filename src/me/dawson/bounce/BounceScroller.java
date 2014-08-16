package me.dawson.bounce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class BounceScroller extends RelativeLayout {
	public static final String TAG = "BounceScroller";

	public static final int DEFALUT_DURATION = 400;

	public static enum State {
		STATE_FIT_CONTENT, STATE_SHOW, STATE_OVER, STATE_FIT_EXTRAS
	};

	protected State mState = State.STATE_FIT_CONTENT;
	private Flinger mFlinger = new Flinger();;
	private BounceListener mListener;
	private View mContentView;

	private int mLastEventY;
	private int mLastTargetTop;

	private View mHeaderView;
	protected int mHeaderHeight;

	private View mFooterView;
	private int mFooterHeight;

	private boolean overScrolled;

	private boolean pullingHeader;
	private boolean pullingFooter;

	private boolean headerPullable;
	private boolean footerPullable;

	private View mTargetView;

	public BounceScroller(Context context) {
		super(context);
	}

	public BounceScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		mContentView = getChildAt(0);
	}

	private long mTimeBase = 0;

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mContentView == null && !eventInView(event, mContentView)) {
			return super.dispatchTouchEvent(event);
		}

		int action = event.getAction();

		if (takeTouchEvent(event)) {
			Log.d(TAG, System.currentTimeMillis() + " takeTouchEvent " + action);
		} else {
			boolean result = super.dispatchTouchEvent(event);
			Log.d(TAG, System.currentTimeMillis() + " dispatchTouchEvent "
					+ result);
		}

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			overScrolled = false;
			mTimeBase = 0;
			pullingHeader = false;
			pullingFooter = false;
			mLastEventY = 0;
			mLastTargetTop = 0;
			mTargetView = null;
			return true;
		} else if (action == MotionEvent.ACTION_DOWN) {
			mTargetView = getTargetView(mContentView, event);
			mTimeBase = 0;
		} else if (action == MotionEvent.ACTION_MOVE) {
			int eventOffset = (int) event.getY() - mLastEventY;
			if (!overScrolled) {
				if (mHeaderView != null && mHeaderView.getBottom() > 0
						&& eventOffset < 0) {
					overScrolled = true;
				} else if (mFooterView != null
						&& mFooterView.getTop() < getBottom()
						&& eventOffset > 0) {
					overScrolled = true;
				} else {
					overScrolled = false;
				}
			}

			if (mTargetView != null
					&& mTargetView.getVisibility() != View.VISIBLE) {
				mTargetView = getTargetView(mContentView, event);
				Log.d(TAG, "update mTargetView " + mTargetView.getId());
				mTimeBase = 0;
				overScrolled = false;
			} else {
				int targetTop = getViewTop(mTargetView);
				Log.d(TAG, "mTargetView " + mTargetView);
				int viewOffset = targetTop - mLastTargetTop;
				Log.d(TAG, "targetTop " + targetTop + " viewOffset "
						+ viewOffset + " eventOffset " + eventOffset
						+ " mTimeBase " + mTimeBase);
				if (eventOffset != 0 && viewOffset == 0 && !overScrolled) {
					long currentTime = System.currentTimeMillis();
					if (mTimeBase == 0) {
						mTimeBase = currentTime;
					} else if (currentTime - mTimeBase > 50) {
						overScrolled = true;
						mTimeBase = 0;
					}
				} else if (eventOffset != 0 && viewOffset != 0) {
					mTimeBase = 0;
				}
			}
		}

		mLastTargetTop = getViewTop(mTargetView);
		mLastEventY = (int) event.getY();

		return true;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, System.currentTimeMillis() + " onDraw");
	}

	private int getViewTop(View view) {
		if (view == null) {
			return 0;
		}
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location[1];
	}

	private boolean takeTouchEvent(MotionEvent event) {
		if (!headerPullable && !footerPullable) {
			return false;
		}

		int action = event.getAction();
		int contentTop = mContentView.getTop();

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL) {
			if (contentTop > 0) {
				int offset = contentTop;
				if (mHeaderView != null && offset > mHeaderHeight / 2) {
					offset = offset - mHeaderHeight;
					mFlinger.recover(offset);
					setState(State.STATE_FIT_EXTRAS, true);
				} else {
					mFlinger.recover(offset);
				}
			} else if (contentTop < 0) {
				int offset = mContentView.getBottom() - getBottom();
				if (mFooterView != null && (offset + mFooterHeight / 2) < 0) {
					offset = offset + mFooterHeight;
					mFlinger.recover(offset);
					setState(State.STATE_FIT_EXTRAS, false);
				} else {
					mFlinger.recover(offset);
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			int offset = (int) (event.getY() - mLastEventY);
			offset = offset / 2;
			boolean handled = false;

			if (headerPullable && !handled && contentTop >= 0 && !pullingFooter) {
				handled |= pullHeader(offset);
			}

			if (footerPullable && !handled && contentTop <= 0 && !pullingHeader) {
				handled |= pullFooter(offset);
			}
			return handled;
		}
		return false;
	}

	private void setState(State newState, boolean header) {
		String position = header ? "header" : "footer";

		if (newState == mState) {
			return;
		}

		Log.d(TAG, position + " setState " + mState + " -> " + newState);

		mState = newState;

		if (mListener != null) {
			mListener.onState(header, newState);
		}
	}

	private boolean pullHeader(int offset) {
		int scrollY = mContentView.getScrollY();
		int curTop = mContentView.getTop();

		// pull header
		if (!overScrolled || scrollY > 0
				|| (offset < 0 && scrollY == 0 && curTop <= 0)) {
			return false;
		}

		pullingHeader = true;

		int nextTop = curTop + offset;
		if (nextTop <= 0) {
			offset = -curTop;
			overScrolled = false;
			mTimeBase = 0;
			nextTop = 0;
			pullingHeader = false;
			if (mState != State.STATE_FIT_CONTENT) {
				setState(State.STATE_FIT_CONTENT, true);
			}
		} else if (nextTop > 0 && nextTop <= mHeaderHeight) {
			if ((mState != State.STATE_SHOW)) {
				setState(State.STATE_SHOW, true);
			}
		} else if (nextTop > mHeaderHeight) {
			if (mState != State.STATE_OVER) {
				setState(State.STATE_OVER, true);
			}
		}

		Log.d(TAG, "pullHeader " + offset + " nextTop " + nextTop);
		offsetContent(offset);

		if (mListener != null) {
			mListener.onOffset(true, nextTop);
		}
		return true;
	}

	private boolean pullFooter(int offset) {
		int curBottom = mContentView.getBottom();
		int conBottom = this.getBottom();

		// pull footer
		if (!overScrolled || (offset > 0 && conBottom <= curBottom)) {
			return false;
		}

		pullingFooter = true;

		int nextBottom = curBottom + offset;
		if (nextBottom >= conBottom) {
			offset = conBottom - curBottom;
			overScrolled = false;
			mTimeBase = 0;
			nextBottom = conBottom;
			pullingFooter = false;
			if (mState != State.STATE_FIT_CONTENT) {
				setState(State.STATE_FIT_CONTENT, false);
			}
		} else if (nextBottom < conBottom
				&& nextBottom >= (conBottom - mFooterHeight)) {
			if ((mState != State.STATE_SHOW)) {
				setState(State.STATE_SHOW, false);
			}
		} else if (nextBottom < (conBottom - mFooterHeight)) {
			if (mState != State.STATE_OVER) {
				setState(State.STATE_OVER, false);
			}
		}

		Log.d(TAG, "pullFooter " + offset + " nextBottom " + nextBottom);
		offsetContent(offset);

		if (mListener != null) {
			mListener.onOffset(false, nextBottom - conBottom);
		}
		return true;
	}

	private class Flinger implements Runnable {
		private Scroller scroller;
		private int lastY;

		public Flinger() {
			scroller = new Scroller(getContext());
		}

		@Override
		public void run() {
			boolean offset = scroller.computeScrollOffset();
			if (offset) {
				offsetContent(lastY - scroller.getCurrY());
				lastY = scroller.getCurrY();
				post(this);
			} else {
				removeCallbacks(this);
			}
		}

		public void recover(int offset) {
			removeCallbacks(this);
			lastY = 0;
			scroller.startScroll(0, 0, 0, offset, DEFALUT_DURATION);
			post(this);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int contentTop = 0;
		int contentBottom = 0;

		if (mContentView != null) {
			contentTop = mContentView.getTop();
			contentBottom = contentTop + this.getMeasuredHeight();
			mContentView.layout(0, contentTop, right, contentBottom);
		}

		if (mHeaderView != null) {
			int headerTop = contentTop - mHeaderHeight;
			mHeaderView.layout(0, headerTop, right, headerTop + mHeaderHeight);
		}

		if (mFooterView != null) {
			int footerTop = contentBottom;
			mFooterView.layout(0, footerTop, right, footerTop + mFooterHeight);
		}
	}

	private boolean offsetContent(int offset) {
		if (mContentView != null) {
			mContentView.offsetTopAndBottom(offset);
		}

		if (mHeaderView != null) {
			mHeaderView.offsetTopAndBottom(offset);
		}

		if (mFooterView != null) {
			mFooterView.offsetTopAndBottom(offset);
		}

		invalidate();
		return true;
	}

	public void fitContent() {
		if (mState != State.STATE_FIT_EXTRAS) {
			return;
		}

		if (mContentView == null) {
			return;
		}

		int offset = mContentView.getTop();
		if (offset != 0) {
			mFlinger.recover(offset);
		}
		mState = State.STATE_FIT_CONTENT;
	}

	public void setHeaderPullable(boolean pullable) {
		this.headerPullable = pullable;
	}

	public void setFooterPullable(boolean pullable) {
		this.footerPullable = pullable;
	}

	public void setHeaderView(View view) {
		if (mHeaderView != null) {
			removeView(mHeaderView);
			mHeaderView = null;
		}

		mHeaderView = view;
		if (mHeaderView != null) {
			mHeaderView.measure(MeasureSpec.UNSPECIFIED,
					MeasureSpec.UNSPECIFIED);
			mHeaderHeight = mHeaderView.getMeasuredHeight();
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					mHeaderHeight);
			addView(mHeaderView, 0, params);
		}
	}

	public View getHeaderView() {
		return mHeaderView;
	}

	public void setFooterView(View view) {
		if (mFooterView != null) {
			removeView(mFooterView);
			mFooterView = null;
		}

		mFooterView = view;
		if (mFooterView != null) {
			mFooterView.measure(MeasureSpec.UNSPECIFIED,
					MeasureSpec.UNSPECIFIED);
			mFooterHeight = mFooterView.getMeasuredHeight();
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					mHeaderHeight);
			addView(mFooterView, 0, params);
		}
	}

	public View getFooterView() {
		return mFooterView;
	}

	public void setListener(BounceListener listener) {
		mListener = listener;
	}

	private View getTargetView(View target, MotionEvent event) {
		View view = null;
		if (target == null) {
			return view;
		}

		if (!eventInView(event, target)) {
			return view;
		}

		if (!(target instanceof ViewGroup)) {
			view = target;
			return view;
		}

		if (target instanceof AdapterView) {
			AdapterView<?> parent = (AdapterView<?>) target;
			int first = parent.getFirstVisiblePosition();
			int last = parent.getLastVisiblePosition();
			for (int index = 0; index <= (last - first); ++index) {
				View child = parent.getChildAt(index);
				if (!eventInView(event, child)) {
					continue;
				}
				if (!(child instanceof ViewGroup)) {
					view = child;
					return view;
				}

				view = getTargetView(child, event);
				// stop search in current view group
				return view;
			}
		} else if (target instanceof ViewGroup) {
			ViewGroup parent = (ViewGroup) target;
			int childCount = parent.getChildCount();

			// with z-order
			for (int index = childCount - 1; index >= 0; --index) {
				View child = parent.getChildAt(index);
				if (!eventInView(event, child)) {
					continue;
				}

				if (!(child instanceof ViewGroup)) {
					view = child;
					return view;
				}

				view = getTargetView(child, event);
				// stop search in current view group
				return view;
			}
		}

		// set view as group self
		view = target;
		return view;
	}

	private boolean eventInView(MotionEvent event, View view) {
		if (event == null || view == null) {
			return false;
		}

		int eventX = (int) event.getRawX();
		int eventY = (int) event.getRawY();

		int[] location = new int[2];
		view.getLocationOnScreen(location);

		int width = view.getWidth();
		int height = view.getHeight();
		int left = location[0];
		int top = location[1];
		int right = left + width;
		int bottom = top + height;

		Rect rect = new Rect(left, top, right, bottom);
		boolean contains = rect.contains(eventX, eventY);
		return contains;
	}
}
