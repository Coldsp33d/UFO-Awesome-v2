package org.ufoveryawesome.parser;

public class UFO {

	private UFODate sightingDate;
	private UFODate reportDate;
	private String location;
	private String shape;
	private String duration;
	private String description;

	public UFODate getSightingDate() {
		return sightingDate;
	}

	public void setSightingDate(UFODate sightingDate) {
		this.sightingDate = sightingDate;
	}

	public UFODate getReportDate() {
		return reportDate;
	}

	public void setReportDate(UFODate reportDate) {
		this.reportDate = reportDate;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(this.sightingDate != null && !this.sightingDate.isEmpty())
			sb.append(sightingDate);
		sb.append(",");
		if(this.reportDate != null && !this.reportDate.isEmpty())
			sb.append(reportDate);
		sb.append(",");
		if(this.description != null && !this.description.isEmpty())
			sb.append(description);
		sb.append(",");
		if(this.location!= null && !this.location.isEmpty())
			sb.append(location);
		sb.append(",");
		if(this.shape != null && !this.shape.isEmpty())
			sb.append(shape);
		sb.append(",");
		if(this.duration != null && !this.duration.isEmpty())
			sb.append(duration);
		return sb.toString();
	}
}