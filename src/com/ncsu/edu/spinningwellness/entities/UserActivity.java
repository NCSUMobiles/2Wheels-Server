package com.ncsu.edu.spinningwellness.entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserActivity {
	
	String id;
	String rideId;
	String userName;
	double distaceCovered;
	double cadence;
	double averageSpeed;
	double caloriesBurned;
	long activityDate;
	
	public UserActivity() {}

	public UserActivity(String id, String rideId, String userName, double distaceCovered,
			double cadence, double averageSpeed, double caloriesBurned, long activityDate) {
		super();
		this.id = id;
		this.rideId = rideId;
		this.userName = userName;
		this.distaceCovered = distaceCovered;
		this.cadence = cadence;
		this.averageSpeed = averageSpeed;
		this.caloriesBurned = caloriesBurned;
		this.activityDate = activityDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRideId() {
		return rideId;
	}

	public void setRideId(String rideId) {
		this.rideId = rideId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public double getDistaceCovered() {
		return distaceCovered;
	}

	public void setDistaceCovered(double distaceCovered) {
		this.distaceCovered = distaceCovered;
	}

	public double getCadence() {
		return cadence;
	}

	public void setCadence(double cadence) {
		this.cadence = cadence;
	}

	public double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public double getCaloriesBurned() {
		return caloriesBurned;
	}

	public void setCaloriesBurned(double caloriesBurned) {
		this.caloriesBurned = caloriesBurned;
	}
	
	public long getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(long activityDate) {
		this.activityDate = activityDate;
	}
}