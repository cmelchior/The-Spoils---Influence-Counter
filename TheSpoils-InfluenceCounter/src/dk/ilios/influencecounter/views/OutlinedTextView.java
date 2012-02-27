package dk.ilios.influencecounter.views;
/**
 * Advanced textview that supports rotation, outline and glow at the same time
 * Currently only support centered text, and still a few bugs with the outline
 * placement.
 * 
 * Credit: Using a mix of techniques found in:
 * @see http://stackoverflow.com/questions/4342927/how-to-correctly-draw-text-in-an-extended-class-for-textview
 * @see https://bitbucket.org/ABitNo/zi/src/783b6b451ba1/src/me/abitno/zi/widget/view/OutlineTextView.java
 * 
 *
 * @author Christian Melchior
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

    private final Rect mTextBounds = new Rect();
	private TextPaint mTextPaintOutline;

	private float mOutlineSize; 
	private int mOutlineColor;
	
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
		mTextPaintOutline.setStrokeWidth(mOutlineSize);
		mTextPaintOutline.setTextAlign(Paint.Align.CENTER);
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


	boolean isDrawingCoordsInitialized = false;
	float x;
	float y;
	private void initDrawingCoords() {
        // Figure out the drawing coordinates
        String baselineText = "25";
		super.getPaint().getTextBounds(baselineText, 0, baselineText.length(), mTextBounds);

        x = super.getWidth() * 0.5f;
        y = (float) (Math.ceil((super.getHeight() + mTextBounds.height()) * 0.5f) + Math.ceil(mOutlineSize));
        y++; // TODO Figure out why this is needed (probably some rounding error somewhere?
        isDrawingCoordsInitialized = true;
	}
	
	
    @Override
    protected void onDraw(Canvas canvas) {
    	if (!isDrawingCoordsInitialized) {
    		initDrawingCoords();
    	}
    	
//        // Get the text to print
    	final String text = super.getText().toString();
//
//        // Figure out the drawing coordinates
//        super.getPaint().getTextBounds(text, 0, text.length(), mTextBounds);

        // draw everything
		canvas.save();
		canvas.rotate(mRotation, getWidth()/2, getHeight()/2);
        super.onDraw(canvas);
        
        float x = this.x; //super.getWidth() * 0.5f;
        float y = this.y; //1 + (float) (Math.ceil((super.getHeight() + mTextBounds.height()) * 0.5f) + Math.ceil(mOutlineSize));
        canvas.drawText(text, x, y, mTextPaintOutline);
        canvas.restore();
    }
}

