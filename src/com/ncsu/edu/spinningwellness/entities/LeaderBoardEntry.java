package com.ncsu.edu.spinningwellness.entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LeaderBoardEntry {
	
	String name;
	double distanceCovered;
	int position;
	
	public LeaderBoardEntry() {}
	
	public LeaderBoardEntry(String name, double distanceCovered, int position) {
		super();
		this.name = name;
		this.distanceCovered = distanceCovered;
		this.position = position;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getDistanceCovered() {
		return distanceCovered;
	}
	
	public void setDistanceCovered(double distanceCovered) {
		this.distanceCovered = distanceCovered;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
}