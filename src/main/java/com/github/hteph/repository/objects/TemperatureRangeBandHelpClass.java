package com.github.hteph.repository.objects;

import lombok.Data;

@Data
public class TemperatureRangeBandHelpClass {
	
	private String name;
	private Integer one;
	private double two;
	private double three;
	private double four;
	private double five;
	private double six;
	private double seven;
	private double eight;
	private double nine;
	private double ten;
	
	public TemperatureRangeBandHelpClass(String name, int[] temp) {
		
		this.name = name;
		one = temp[0];
		two= temp[1];
		three= temp[2];
		four= temp[3];
		five= temp[4];
		six= temp[5];
		seven= temp[6];
		eight= temp[7];
		nine= temp[8];
		ten= temp[9];
		
	}

	@Override
	public String toString() {
		return "TemperatureRangeBandHelpClass [one=" + one + ", two=" + two + ", three=" + three + ", four=" + four
				+ ", five=" + five + ", six=" + six + ", seven=" + seven + ", eight=" + eight + ", nine=" + nine
				+ ", ten=" + ten + "]";
	}
}
