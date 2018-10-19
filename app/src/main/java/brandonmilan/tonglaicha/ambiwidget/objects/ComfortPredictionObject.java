package brandonmilan.tonglaicha.ambiwidget.objects;

import android.content.Context;

import brandonmilan.tonglaicha.ambiwidget.R;

public class ComfortPredictionObject {
	private double mLevel;

	public double levelAsFloat() {
		return mLevel;
	}
	public int levelAsInt() {
		return (int) Math.round(mLevel);
	}
	public String levelAsText(Context context) {
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

	public ComfortPredictionObject (double predictionLevel) {
		this.mLevel = predictionLevel;
	}
}
