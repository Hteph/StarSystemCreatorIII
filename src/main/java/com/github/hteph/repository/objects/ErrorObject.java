package com.github.hteph.repository.objects;

import java.math.BigDecimal;

/**
 */
public class ErrorObject extends StellarObject {

    public ErrorObject(String errorMessage) {
        super.setName("Error");
        super.setDescription(errorMessage);
    }

    public BigDecimal getOrbitalDistance() {
        return null;
    }
}
