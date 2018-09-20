package brandonmilan.tonglaicha.ambiwidget.API;

public interface OnProcessFinish<T> {
	void onSuccess(T object);
	void onFailure(T object);
}
