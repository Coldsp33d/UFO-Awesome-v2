package org.ufoveryawesome.parser;

public class UFODate {
	String date;
	String month;
	String year;

	public String getDate() {
		return date;
	}

	public void setDate(String day) {
		this.date = day;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public boolean isEmpty() {
		boolean flag = true;
		if (flag && date != null && !date.isEmpty())
			flag = false;
		if (flag && month != null && !month.isEmpty())
			flag = false;
		if (flag && year != null && !year.isEmpty())
			flag = false;
		return flag;
	}

	@Override
	public String toString() {
		String concatenatedDate = "";
		if (year != null && !year.isEmpty()) {
			concatenatedDate = year + concatenatedDate;
			if (month != null && !month.isEmpty()) {
				concatenatedDate = month + " " + concatenatedDate;
				if (date != null && !date.isEmpty()) {
					concatenatedDate = date + " " + concatenatedDate;
				}
			}
		}
		return concatenatedDate;
	}
	
	
}