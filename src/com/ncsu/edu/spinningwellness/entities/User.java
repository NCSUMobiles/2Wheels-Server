package com.ncsu.edu.spinningwellness.entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {
	
	String name;
	String emailAddress;
	
	public User() {}
	
	public User(String name, String emailAddress) {
		super();
		this.name = name;
		this.emailAddress = emailAddress;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
}