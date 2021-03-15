package com.github.hteph.utils.comparators;



import com.github.hteph.repository.objects.StellarObject;

import java.io.Serializable;
import java.util.Comparator;

public class lunarObjectDistanceComparator implements Comparator<StellarObject>,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(StellarObject o1, StellarObject o2) {
		
        if(o1.getOrbitalFacts().getOrbitalDistance().doubleValue() > o2.getOrbitalFacts().getOrbitalDistance().doubleValue()){
            return -1;
        } else {
            return 1;
        }
	}

}
