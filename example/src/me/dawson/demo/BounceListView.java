package me.dawson.demo;

import me.dawson.bounce.BounceListener;
import me.dawson.bounce.BounceScroller;
import me.dawson.bounce.BounceScroller.State;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BounceListView extends Activity {

	private BounceScroller scroller;
	private ListView lvInfo;

	private TextView tvHeader;
	private TextView tvFooter;

	private BaseAdapter adapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.bounce_list_item, lvInfo, false);
			}
			TextView title = (TextView) convertView.findViewById(R.id.tv_title);
			title.setText("pullable list item " + position);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return 12;
		}
	};

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.bounce_listview);

		scroller = (BounceScroller) findViewById(R.id.pc_root);
		scroller.setListener(bl);
		scroller.ifHeaderBounce(true);
		scroller.ifFooterBounce(true);
		setHeaderView();
		setFooterView();

		lvInfo = (ListView) findViewById(R.id.lv_info);
		lvInfo.setAdapter(adapter);
	}

	public void setHeaderView() {
		tvHeader = new TextView(BounceListView.this);
		tvHeader.setPadding(10, 20, 10, 20);
		tvHeader.setText("Pullable Header");
		tvHeader.setBackgroundColor(getResources().getColor(R.color.grey_3));
		tvHeader.setTextColor(getResources().getColor(R.color.white));
		tvHeader.setGravity(Gravity.CENTER);
		scroller.setHeaderView(tvHeader);
	}

	public void setFooterView() {
		tvFooter = new TextView(BounceListView.this);
		tvFooter.setPadding(10, 20, 10, 20);
		tvFooter.setText("Pullable Footer");
		tvFooter.setBackgroundColor(getResources().getColor(R.color.grey_3));
		tvFooter.setTextColor(getResources().getColor(R.color.white));
		tvFooter.setGravity(Gravity.CENTER);
		scroller.setFooterView(tvFooter);
	}

	private BounceListener bl = new BounceListener() {
		@Override
		public void onState(boolean header, State state) {
			if (header) {
				if (state == State.STATE_SHOW) {
					tvHeader.setText("Pullable Header Show");
				} else if (state == State.STATE_OVER) {
					tvHeader.setText("Pullable Header Over");
				} else if (state == State.STATE_FIT_EXTRAS) {
					scroller.postDelayed(new Runnable() {
						@Override
						public void run() {
							scroller.fitContent();
						}
					}, 800);
				}
			}
		}

		@Override
		public void onOffset(boolean header, int offset) {

		}
	};
}
