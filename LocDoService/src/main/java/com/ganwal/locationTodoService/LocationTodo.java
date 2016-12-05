package com.ganwal.locationTodoService;


import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import org.codehaus.jackson.annotate.JsonProperty;


@Entity
public class LocationTodo {

    @JsonProperty("cloudId")
    @Id Long id;
    private String name;
    private String summary;
    private boolean locationAlert;
    private long geofenceID;
    private float latitude;
    private float longitude;
    private String locationDescr;
    private float radius;
    private int priority;
    private long dueDate;
    private boolean completed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isLocationAlert() {
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationTodo)) return false;

        LocationTodo todo = (LocationTodo) o;

        return getId().equals(todo.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "LocationTodo{" +
                "id=" + id +
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
                '}';
    }

}
