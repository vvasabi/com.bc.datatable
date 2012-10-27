package com.bc.datatable;

import java.io.Serializable;

public class City implements Serializable {

	private static final long serialVersionUID = -4460960974867732139L;

	private Integer id;
	private String name;
	private String asciiName;
	private Integer latitude;
	private Integer longitude;
	private String timeZone;
	private Integer population;
	private String province;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAsciiName() {
		return asciiName;
	}

	public void setAsciiName(String asciiName) {
		this.asciiName = asciiName;
	}

	public Integer getLatitude() {
		return latitude;
	}

	public void setLatitude(Integer latitude) {
		this.latitude = latitude;
	}

	public Integer getLongitude() {
		return longitude;
	}

	public void setLongitude(Integer longitude) {
		this.longitude = longitude;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Integer getPopulation() {
		return population;
	}

	public void setPopulation(Integer population) {
		this.population = population;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

}
