package dk.ilios.influencecounter.pages;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public abstract class PageGenerator {
	
	protected Activity mParentActivity;
	
	public PageGenerator(Activity activity) {
		mParentActivity = activity;
	}
	
	public void onCreate(Context context) {}
	public abstract View onCreateView();
	
	protected void onResume() {}
	protected void onPause() {}

	protected Activity getActivity() {
		return mParentActivity;
	}
}
