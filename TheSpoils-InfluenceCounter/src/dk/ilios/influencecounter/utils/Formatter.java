package dk.ilios.influencecounter.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import dk.ilios.influencecounter.R;

public class Formatter {

	/**
	 * Return time formatter for hours/minutes/seconds
	 */
	public static DateFormat getTimeFormatter(Context ctx) {
		if (android.text.format.DateFormat.is24HourFormat(ctx)) {
			return new SimpleDateFormat("HH:mm:ss");
		} else {
			return new SimpleDateFormat("hh:mm:ss a");
		}
	}

	/**
	 * Color format a value, so x < 0 is red and green otherwise
	 */
	public static Spannable colorize(int value, Context context) {
		return colorize(null, value, context);
	}
	
	public static Spannable colorize(String formatterString, int value, Context context) {

		String res = "";
		Spannable str = null;

		try {

			String number = Integer.toString(value);
			
			// Construct string
			if (value >= 0) {
				res = "+" + number;
			} else {
				res = number;
			}
			
			// Format string if possible
			if (formatterString != null && !formatterString.isEmpty()) {
				res = String.format(formatterString, res);
			}
			
			// Colorize string
			str = new SpannableString(res);
			ForegroundColorSpan color; 

			if (value >= 0) {
				color = new ForegroundColorSpan(context.getResources().getColor(R.color.postive_influence_change));
				str.setSpan(color, 0, res.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else {
				color = new ForegroundColorSpan(context.getResources().getColor(R.color.negative_influence_change));
				str.setSpan(color, 0, res.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}


		} catch(NumberFormatException e) {
			str = new SpannableString("");
		}

		return str;
	}

}
