package me.dawson.bounce;

import me.dawson.bounce.BounceScroller.State;

public interface BounceListener {

	public void onState(boolean header, State state);

	public void onOffset(boolean header, int offset);

}
