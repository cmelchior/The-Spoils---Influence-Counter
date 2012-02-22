package dk.ilios.influencecounter.views;
/**
 * TextView for displaying the current counter value.
 * Can also rotate the text a number of degrees (will probably clip the text
 * for other than 180 degree rotations).
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;
import dk.ilios.influencecounter.R;

public class RotatableCounterTextView extends TextView {

	private int mRotation = 0;	// Degrees to rotate text

	public RotatableCounterTextView(Context context) {
		super(context);
	}

	public RotatableCounterTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAtttributes(context, attrs);
	}

	public RotatableCounterTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAtttributes(context, attrs);
	}

	/**
	 * Initialize custom attributes
	 */
	private void initAtttributes(Context context, AttributeSet attrs) {
		if (attrs != null) { 
        	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotatableCounterTextView);
			mRotation = (a.getInt(0, R.styleable.RotatableCounterTextView_rotation));
			a.recycle();
		}
	}
	
	/**
	 * Set rotation across center in degrees
	 */
	public void setRotation(int rotation) {
		mRotation = rotation;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.rotate(mRotation, getWidth()/2, getHeight()/2);
		super.onDraw(canvas);
		canvas.restore();
	}
}
