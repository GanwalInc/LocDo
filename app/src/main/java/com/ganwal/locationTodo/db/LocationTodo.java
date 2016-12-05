package com.ganwal.locationTodo.db;

public class LocationTodo {
	private static final String TAG = LocationTodo.class.getSimpleName();


	private transient long id;
	private String cloudId;
	private transient long userId;
	private String name;
	private String summary;
	private boolean locationAlert;
	private long geofenceID;
	private float latitude;
	private float longitude;
	private String locationDescr;
	private float radius = 0.25f;
	private int priority = 3;
	private long dueDate;
	private boolean completed;
	//making fields transient so they won't be serialized
	private transient long createDate;
	private transient long lastUpdateDate;
	private transient boolean updated;
	private transient boolean deleted;

	//---------------------------------------------------\\
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCloudId() {
		return cloudId;
	}

	public void setCloudId(String cloudId) {
		this.cloudId = cloudId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean getLocationAlert() {
		return locationAlert;
	}

	public void setLocationAlert(boolean locationAlert) {
		this.locationAlert = locationAlert;
	}

	public long getGeofenceID() {
		return geofenceID;
	}

	public void setGeofenceID(long geofenceID) {
		this.geofenceID = geofenceID;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public String getLocationDescr() {
		return locationDescr;
	}

	public void setLocationDescr(String locationDescr) {
		this.locationDescr = locationDescr;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getDueDate() {
		return dueDate;
	}

	public void setDueDate(long dueDate) {
		this.dueDate = dueDate;
	}

	public boolean getCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public long getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public boolean getUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	//---------------------------------------------------\\


	@Override
	public String toString() {
		return "LocationTodo{" +
				"id=" + id +
				", cloudId=" + cloudId +
				", userId=" + userId +
				", name='" + name + '\'' +
				", summary='" + summary + '\'' +
				", locationAlert=" + locationAlert +
				", geofenceID=" + geofenceID +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", locationDescr='" + locationDescr + '\'' +
				", radius=" + radius +
				", priority=" + priority +
				", dueDate=" + dueDate +
				", completed=" + completed +
				", createDate=" + createDate +
				", lastUpdateDate=" + lastUpdateDate +
				", updated=" + updated +
				", deleted=" + deleted +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LocationTodo)) return false;

		LocationTodo that = (LocationTodo) o;

		return getId() == that.getId();

	}

	@Override
	public int hashCode() {
		return (int) (getId() ^ (getId() >>> 32));
	}
}
