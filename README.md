## Bounce Scroller

=====
Bounce Scroller is a library for Android platform which provides iOS
like bounce scroller effect and can also be used as pull-to-refresh.

### Features

* Support all ViewGroups and almost all Views(except self scrollable TextView);
* Bounce can add with both header and footer to be pull-to-refresh feature;
* No need to specify customer wrappers, ONLY one container to finish all bounces;

### Usage

Just as below code, only need to make BounceScroller as the wrapper.

```
<me.dawson.bounce.BounceScroller xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/pc_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/blue" >

    <TextView
        android:id="@+id/tv_info"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/grey_4"
        android:gravity="center"
        android:text="@string/app_name"
        android:textColor="@color/white" />

</me.dawson.bounce.BounceScroller>
```

Set scroller features

```
// get scroller view
scroller = (BounceScroller) findViewById(R.id.pc_root);

// set bounce listener
scroller.setListener(bounceListener);

// set if header can bounce
scroller.ifHeaderBounce(true);

// set if footer can bounce
scroller.ifFooterBounce(true);

// set header view
scroller.setHeaderView(header);

// set footer view
scroller.setFooterView(footer);

// set recover content interpolator
scroller.setInterpolator(interpolator);
```

### Demo

### demo manage screen
![](http://ww1.sinaimg.cn/large/70489561gw1ejdsjweaesg208c0ephdu.gif)

===

Any further question?

[email](mailto:coder.kiss@gmail.com) me please!