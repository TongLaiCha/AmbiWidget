package brandonmilan.tonglaicha.ambiwidget.objects;

import java.util.Date;

public class TokenObject {
	private String mType;
	private String mValue;
	private Long mExpiresIn;
	private Date mCreationDate;

	public String type() {
		return mType;
	}
	public String value() {
		return mValue;
	}
	public Long expiresIn() {
		return mExpiresIn;
	}
	private Date creationDate() {
		return mCreationDate;
	}

	public TokenObject (String type, String value, Long expiresIn) {
		this.mType = type;
		this.mValue = value;
		if (expiresIn != null) {
			this.mExpiresIn = expiresIn * 1000;
		}
		this.mCreationDate = new Date();
	}

	public boolean isExpired() {
		if (this.mExpiresIn == null) return false;
		Date now = new Date();
		long difference = new Date().getTime() - this.creationDate().getTime();
		boolean expired = difference > mExpiresIn - 10000;
		if (expired) {
			this.mValue = null;
		}
		return expired;
	}
}

