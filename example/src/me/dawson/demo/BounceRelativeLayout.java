package me.dawson.demo;

import me.dawson.bounce.BounceListener;
import me.dawson.bounce.BounceScroller;
import me.dawson.bounce.BounceScroller.State;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BounceRelativeLayout extends Activity {

	private BounceScroller scroller;
	private Button btListView;
	private Button btTextView;

	private TextView tvOffset;

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Class<?> clazz = null;
			if (v.equals(btListView)) {
				clazz = BounceListView.class;
			} else if (v.equals(btTextView)) {
				clazz = BounceTextView.class;
			}

			startPage(clazz);
		}
	};

	private void startPage(Class<?> clazz) {
		if (clazz == null) {
			return;
		}
		Intent intent = new Intent(this, clazz);
		startActivity(intent);
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.bounce_relativelayout);

		scroller = (BounceScroller) findViewById(R.id.pc_root);
		scroller.setListener(bl);
		scroller.ifHeaderBounce(true);
		scroller.ifFooterBounce(true);
		setHeaderView();
		setFooterView();

		btListView = (Button) findViewById(R.id.bt_listview);
		btListView.setOnClickListener(clickListener);
		btTextView = (Button) findViewById(R.id.bt_textview);
		btTextView.setOnClickListener(clickListener);

		tvOffset = (TextView) findViewById(R.id.tv_offset);
	}

	public void setHeaderView() {
		TextView header = new TextView(BounceRelativeLayout.this);
		header.setPadding(10, 20, 10, 20);
		header.setText("Pullable Header View");
		header.setBackgroundColor(getResources().getColor(R.color.grey_3));
		header.setTextColor(getResources().getColor(R.color.white));
		header.setGravity(Gravity.CENTER);
		scroller.setHeaderView(header);
	}

	public void setFooterView() {
		TextView footer = new TextView(BounceRelativeLayout.this);
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
				}, 1000);
			}
		}

		@Override
		public void onOffset(boolean header, int offset) {
			String position = header ? "header" : "footer";
			tvOffset.setText(position + " offset " + offset);
		}
	};
}
