package dk.ilios.influencecounter.views;
/**
 * Rotates a ImageButton.
 *
 * @author Christian Melchior <christian@ilios.dk>
 */

import dk.ilios.influencecounter.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class RotatableImageButton extends ImageButton {

	private int mRotation = 0;	// Degrees to rotate text

	public RotatableImageButton(Context context) {
		super(context);
	}

	public RotatableImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAtttributes(context, attrs);
	}

	public RotatableImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAtttributes(context, attrs);
	}

	/**
	 * Initialize custom attributes
	 */
	private void initAtttributes(Context context, AttributeSet attrs) {
		if (attrs != null) { 
        	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotatableImageButton);
			mRotation = (a.getInt(0, R.styleable.RotatableImageButton_rotation));
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
