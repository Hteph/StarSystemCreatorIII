package com.github.hteph.repository.objects;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class OrbitalFacts implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orbitsAround;
    private BigDecimal orbitalDistance;
    private BigDecimal orbitalEccentricity;
    private BigDecimal orbitalInclination;
    private char orbitalObjectClass;

}
