package dk.ilios.influencecounter.views;
/**
 * ViewPager where paging can be disabled.
 * 
 * @credit http://blog.svpino.com/2011/08/disabling-pagingswiping-on-android.html
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DisableableViewPager extends ViewPager {

	private boolean mEnabled = true;

	public DisableableViewPager(Context context) {
		super(context);
	}

	public DisableableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mEnabled) {
            return super.onTouchEvent(event);
        }
  
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mEnabled) {
            return super.onInterceptTouchEvent(event);
        }
 
        return false;
    }
 
    public void setPagingEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }
}
