package brandonmilan.tonglaicha.ambiwidget.objects;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;

import brandonmilan.tonglaicha.ambiwidget.R;

public class ComfortPredictionObject implements Serializable {
	private double mLevel;

	public double getLevelAsFloat() {
		return mLevel;
	}
	public int getLevelAsInt() {
		return (int) Math.round(mLevel);
	}
	public String getLevelAsText(Context context) {
		// Round level
		switch ((int) Math.round(mLevel)) {
			case -3:
				return context.getString(R.string.freezing);
			case -2:
				return context.getString(R.string.too_cold);
			case -1:
				return context.getString(R.string.bit_cold);
			case 0:
				return context.getString(R.string.comfy);
			case 1:
				return context.getString(R.string.bit_warm);
			case 2:
				return context.getString(R.string.too_warm);
			case 3:
				return context.getString(R.string.hot);
			default:
				return null;
		}
	}
	public String levelAsTag(Context context) {
		// Round level
		switch ((int) Math.round(mLevel)) {
			case -3:
				return context.getString(R.string.freezing_tag);
			case -2:
				return context.getString(R.string.too_cold_tag);
			case -1:
				return context.getString(R.string.bit_cold_tag);
			case 0:
				return context.getString(R.string.comfy_tag);
			case 1:
				return context.getString(R.string.bit_warm_tag);
			case 2:
				return context.getString(R.string.too_warm_tag);
			case 3:
				return context.getString(R.string.hot_tag);
			default:
				return null;
		}
	}
	public void setLevelByTag(String tag) {
		// Round level
		switch (tag) {
			case "freezing":
				this.mLevel = -3;
				break;
			case "too_cold":
				this.mLevel = -2;
				break;
			case "bit_cold":
				this.mLevel = -1;
				break;
			case "comfortable":
				this.mLevel = 0;
				break;
			case "bit_warm":
				this.mLevel = 1;
				break;
			case "too_warm":
				this.mLevel = 2;
				break;
			case "hot":
				this.mLevel = 3;
				break;
		}
	}

	public ComfortPredictionObject (double predictionLevel) {
		this.mLevel = predictionLevel;
	}
}
