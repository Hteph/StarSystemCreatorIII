package com.github.hteph.repository.objects;

import lombok.Builder;
import lombok.Data;
import java.util.Arrays;

@Data
@Builder
public class AsteroidBelt extends StellarObject{

	private static final long serialVersionUID = 1L;
	private OrbitalFacts orbitalFacts;
	private double mass;	
	private double eccentricity;
	private String asterioidBeltType;
	private double asteroidBeltWidth;
	private String objectClass = "Asteroid Belt";
	
	private double[] sizeDistribution = new double[2]; // Average size/ Max size


	public void setSizeDistribution(double average, double max) {
		
		this.sizeDistribution[0] = average;
		this.sizeDistribution[1] =max;
	}

	@Override
	public String toString() {
		return "AsteroidBelt [asterioidBeltType=" + asterioidBeltType + ", asteroidBeltWidth=" + asteroidBeltWidth
				+ ", sizeDistribution=" + Arrays.toString(sizeDistribution) + "]";
	}
}
