package com.ganwal.locationTodo;

public class LocationPlace {
	
	private String placeID = null;
	
	private String descr = null;
	
	

	public LocationPlace(String placeID, String descr) {
		super();
		this.placeID = placeID;
		this.descr = descr;
	}

	public String getPlaceID() {
		return placeID;
	}

	public void setPlaceID(String placeID) {
		this.placeID = placeID;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	@Override
	public String toString() {
		return descr;
	}
	
	

}
