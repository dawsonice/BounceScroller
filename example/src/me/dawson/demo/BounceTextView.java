package me.dawson.demo;

import me.dawson.bounce.BounceListener;
import me.dawson.bounce.BounceScroller;
import me.dawson.bounce.BounceScroller.State;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class BounceTextView extends Activity {

	private BounceScroller scroller;
	private TextView tvInfo;
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		}
	};

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.bounce_textview);

		scroller = (BounceScroller) findViewById(R.id.pc_root);
		scroller.setListener(bl);
		scroller.setHeaderBounce(true);
		scroller.setFooterBounce(true);
		setHeaderView();
		// setFooterView();

		tvInfo = (TextView) findViewById(R.id.tv_info);
		tvInfo.setOnClickListener(onClickListener);
	}

	public void setHeaderView() {
		ImageView header = new ImageView(BounceTextView.this);
		header.setImageResource(R.drawable.image4);
		header.setScaleType(ScaleType.FIT_XY);
		scroller.setHeaderView(header);
	}

	public void setFooterView() {
		TextView footer = new TextView(BounceTextView.this);
		footer.setPadding(10, 20, 10, 20);
		footer.setText("Pullable Footer View");
		footer.setBackgroundColor(getResources().getColor(R.color.grey_3));
		footer.setTextColor(getResources().getColor(R.color.white));
		footer.setGravity(Gravity.CENTER);
		scroller.setFooterView(footer);
	}

	private BounceListener bl = new BounceListener() {
		@Override
		public void onState(boolean header, State state) {
			if (state == State.STATE_FIT_EXTRAS) {
				scroller.postDelayed(new Runnable() {
					@Override
					public void run() {
						scroller.fitContent();
					}
				}, 1200);
			}
		}

		@Override
		public void onOffset(boolean header, int offset) {
		}
	};
}
