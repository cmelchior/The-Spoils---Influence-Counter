package dk.ilios.influencecounter.views;
/**
 * ViewGroup rotate all their children 180 degrees.
 * Note: Buttons don't work properly if rotated, so only use for static
 * drawables.
 *
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class RotatableRelativeLayout extends RelativeLayout {

	private int mRotation = 180;
	private float[] mTemp = new float[2]; 
	
	public RotatableRelativeLayout(Context context) {
		super(context);
	}

	public RotatableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public RotatableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();
		canvas.rotate(mRotation, canvas.getWidth()/2, canvas.getHeight()/2);
		super.dispatchDraw(canvas);
		canvas.restore();
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.rotate(mRotation, canvas.getWidth()/2, canvas.getHeight()/2);
		super.onDraw(canvas);
		canvas.restore();
	}
	
	
	@Override 
    public boolean dispatchTouchEvent(MotionEvent event) { 
        mTemp[0] = event.getX(); 
        mTemp[1] = event.getY(); 
        event.setLocation(getWidth()-mTemp[0], getHeight()-mTemp[1]); 
        return super.dispatchTouchEvent(event); 
    } 
}
