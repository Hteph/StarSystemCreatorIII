package com.github.hteph.repository;

import java.io.Serializable;
import java.util.Comparator;

public class AtmosphericGases implements Serializable, Comparable<AtmosphericGases>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int percentageInAtmo;



//	public AtmosphericGases(String name, int percentageInAtmo) {
//
//		this.name = name;
//		this.percentageInAtmo = percentageInAtmo;
//	}

    public AtmosphericGases(Builder builder) {

        this.name = builder.name;
        this.percentageInAtmo = builder.percentageInAtmo;
    }

//Getters and Setters-----------------------------------------------------------------------------------------


	public String getName() {
		return name;
	}

	public int getPercentageInAtmo() {
		return percentageInAtmo;
	}

	public void setPercentageInAtmo(int percentageInAtmo) {
		this.percentageInAtmo = percentageInAtmo;
	}

    public static Builder builder() {
        return new Builder();
    }

	@Override
	public String toString() {
        return name + " (" + percentageInAtmo + " %)";
	}

    @Override
    public int compareTo(AtmosphericGases otherGas) {
        return this.getPercentageInAtmo()-otherGas.getPercentageInAtmo();
    }

    public static class atmoCompositionComparator implements Comparator<AtmosphericGases>, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(AtmosphericGases gas1, AtmosphericGases gas2) {

            // Observe the sorting logic, higher percentage is sorted first

            return Integer.compare(gas2.getPercentageInAtmo(), gas1.getPercentageInAtmo());
        }

    }

    public static class Builder {
        private String name;
        private int percentageInAtmo;

        private Builder(){

        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPercentageInAtmo(int percentageInAtmo) {
            this.percentageInAtmo = percentageInAtmo;
            return this;
        }

        public AtmosphericGases build() {
            return new AtmosphericGases(this);
        }
    }
}
