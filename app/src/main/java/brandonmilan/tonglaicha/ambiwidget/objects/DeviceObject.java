package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class DeviceObject implements Serializable {
	private String mDeviceId;
	private String mRoomName;
	private String mLocationName;

	public String deviceId() {
		return mDeviceId;
	}
	public String roomName() {
		return mRoomName;
	}
	public String locationName() {
		return mLocationName;
	}

	public DeviceObject (String deviceId, String roomName, String locationName) {
		this.mDeviceId = deviceId;
		this.mRoomName = roomName;
		this.mLocationName = locationName;
	}
}
