package com.github.hteph.utils.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class atmoCompositionComparator implements Comparator<AtmosphericGases>, Serializable {

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
