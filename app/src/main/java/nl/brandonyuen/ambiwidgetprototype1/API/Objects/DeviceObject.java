package nl.brandonyuen.ambiwidgetprototype1.API.Objects;

public class DeviceObject {
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
