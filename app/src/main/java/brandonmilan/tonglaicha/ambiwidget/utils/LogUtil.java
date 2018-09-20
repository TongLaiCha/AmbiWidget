package brandonmilan.tonglaicha.ambiwidget.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogUtil {

	private static final int CALL_STACK_INDEX = 1;
	private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

	public static String lineNr() {
		// DO NOT switch this to Thread.getCurrentThread().getStackTrace(). The test will pass
		// because Robolectric runs them on the JVM but on Android the elements are different.
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		if (stackTrace.length <= CALL_STACK_INDEX) {
			throw new IllegalStateException(
					"Synthetic stacktrace didn't have enough elements: are you using proguard?");
		}
		int lineNumber = stackTrace[CALL_STACK_INDEX].getLineNumber();
		return "("+lineNumber+")";
	}
}
