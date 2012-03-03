package dk.ilios.influencecounter.views;
/**
 * Advanced TextView that supports rotation, outline and glow at the same time.
 * Currently only support centered text, and still a few bugs with the outline
 * placement.
 * 
 * Credit: Using a mix of techniques found in:
 * @see http://stackoverflow.com/questions/4342927/how-to-correctly-draw-text-in-an-extended-class-for-textview
 * @see https://bitbucket.org/ABitNo/zi/src/783b6b451ba1/src/me/abitno/zi/widget/view/OutlineTextView.java
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;
import dk.ilios.influencecounter.R;

public class OutlinedTextView extends TextView {

	private int mRotation = 0;	// Degrees to rotate text
	private boolean mIsDrawingCoordsInitialized = false;
	private float x;			// X-value for placing outline
	private float y;			// Y-value for placing outline

	private TextPaint mTextPaint;
	private TextPaint mTextPaintWithoutShadow;
	private TextPaint mTextPaintOutline;
	
	private int mTextColor;
	private float mOutlineSize; 	// Beware that small values might cause rendering artifacts
	private int mOutlineColor;
	
	// Shadow attributes for enabling/disabling blur effect at runtime
	private boolean mOriginalShadowConfigurationSaved = false;
	private float mOriginalShadowRadius;
    private float mOriginalShadowDx;
    private float mOriginalShadowDy;
    private int mOriginalShadowColor; 

	private float mCurrentShadowRadius;
    private float mCurrentShadowDx;
    private float mCurrentShadowDy;
    private int mCurrentShadowColor; 
    
    public OutlinedTextView(Context context) {
        super(context);
        initPaint();
        initFont(context);
    }

    public OutlinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAtttributes(context, attrs, 0);
        initPaint();
        initFont(context);
    }

    public OutlinedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAtttributes(context, attrs, defStyle);
        initPaint();
        initFont(context);
    }
    
	/**
	 * Initialize custom attributes
	 */
	private void initAtttributes(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) { 
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OutlineTextView, defStyle, 0);
			mRotation = (a.getInt(R.styleable.OutlineTextView_rotation, 0));
			mOutlineSize = (float) Math.ceil((a.getDimension(R.styleable.OutlineTextView_borderSize, 0)));
			mOutlineColor = (a.getColor(R.styleable.OutlineTextView_borderColor, 0x000000));
			a.recycle();
		}
		
		mTextColor = getTextColors().getDefaultColor();
		
    	// Force this text label to be centered
        super.setGravity(Gravity.CENTER_HORIZONTAL);
	}

	/**
	 * Initialize the stroke paint
	 */
	private void initPaint() {
		mTextPaintOutline = new TextPaint();
		mTextPaintOutline.setAntiAlias(true);
		mTextPaintOutline.setTextSize(getTextSize());
		mTextPaintOutline.setColor(mOutlineColor);
		mTextPaintOutline.setStyle(Paint.Style.STROKE);
		mTextPaintOutline.setTypeface(getTypeface());
		mTextPaintOutline.setStrokeWidth(mOutlineSize*2); // double up as border is both outside and inside
		mTextPaintOutline.setTextAlign(Paint.Align.CENTER);

		mTextPaintWithoutShadow = new TextPaint();
		mTextPaintWithoutShadow.setAntiAlias(true);
		mTextPaintWithoutShadow.setTextSize(getTextSize());
		mTextPaintWithoutShadow.setColor(mTextColor);
		mTextPaintWithoutShadow.setStyle(Paint.Style.FILL);
		mTextPaintWithoutShadow.setTypeface(getTypeface());
		mTextPaintWithoutShadow.setTextAlign(Paint.Align.CENTER);
		
		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(getTextSize());
		mTextPaint.setColor(mTextColor);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setTypeface(getTypeface());
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		
		if (mCurrentShadowRadius > 0) {
			mTextPaint.setShadowLayer(mCurrentShadowRadius, mCurrentShadowDx, mCurrentShadowDy, mCurrentShadowColor);
		}
	}
	
	/**
	 * Initialize a custom font
	 */
	private void initFont(Context ctx) {
		Typeface tf = null;
		try {
			tf = Typeface.createFromAsset(ctx.getAssets(), "font.name");  
			setTypeface(tf);
		} catch (Exception e) {
			/* Ignore */
		}
	}
	
	@Override
	public void setShadowLayer(float radius, float dx, float dy, int color) {
       
		if (!mOriginalShadowConfigurationSaved) {
			mOriginalShadowRadius = radius;
			mOriginalShadowDx = dx;
			mOriginalShadowDy = dy;
			mOriginalShadowColor = color;
			mOriginalShadowConfigurationSaved = true;
		}
		
		mCurrentShadowRadius = radius;
		mCurrentShadowDx = dx;
		mCurrentShadowDy = dy;
		mCurrentShadowColor = color;

       initPaint();
	}
	

	public void setTextColor(int textColor) {
		super.setTextColor(textColor);
		mTextColor = textColor;
		initPaint();
	}
	
	public void setGlowEnabled(boolean enabled) {
		if (!enabled) {
			mCurrentShadowRadius = 0;

		} else {
			mCurrentShadowRadius = mOriginalShadowRadius;
			mCurrentShadowDx = mOriginalShadowDx;
			mCurrentShadowDy = mOriginalShadowDy;
			mCurrentShadowColor = mOriginalShadowColor;
		}
		
		initPaint();
	}
	
	public void setGlowColor(int glowColor) {
		mOriginalShadowColor = glowColor;
		mCurrentShadowColor = glowColor;
		initPaint();
	}
	
	public void setBorderColor(int borderColor) {
		mOutlineColor = borderColor;
		initPaint();
	}
	
	private void initDrawingCoords() {
        // Figure out the drawing coordinates
		// If we do this in onDraw, the textBounds will change a little, causing
		// the outline to jump a little up and down.
	    Rect mTextBounds = new Rect();
		String baselineText = "25";
		super.getPaint().getTextBounds(baselineText, 0, baselineText.length(), mTextBounds);

        x = super.getWidth() * 0.5f;
        y = (float) (Math.ceil((super.getHeight() + mTextBounds.height()) * 0.5f) + Math.ceil(mOutlineSize));
        y++; // TODO Figure out why this is needed (probably some rounding error somewhere?
        mIsDrawingCoordsInitialized = true;
	}
	
	
    @Override
    protected void onDraw(Canvas canvas) {
    	if (!mIsDrawingCoordsInitialized) {
    		initDrawingCoords();
    	}
    	
    	// Get the text to print
    	final String text = super.getText().toString();

    	// draw everything
		canvas.save();
		canvas.rotate(mRotation, getWidth()/2, getHeight()/2);
        
        float x = this.x; 
        float y = this.y; 
        canvas.drawText(text, x, y, mTextPaint); // Blur effect (with transparent text)
        canvas.drawText(text, x, y, mTextPaintOutline); // Stroke effect
        canvas.drawText(text, x, y, mTextPaintWithoutShadow); // Text color on top of it all
        canvas.restore();
    }
}

