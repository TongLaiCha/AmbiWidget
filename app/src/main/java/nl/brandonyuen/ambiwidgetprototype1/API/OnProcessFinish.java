package nl.brandonyuen.ambiwidgetprototype1.API;

public interface OnProcessFinish<T> {
	void onSuccess(T object);
	void onFailure(T object);
}
