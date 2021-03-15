package com.github.hteph.repository.objects;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Star extends StellarObject {

    private static final long serialVersionUID = 1L;

    private BigDecimal lumosity;
    private BigDecimal diameter;
    private String classification;
    private BigDecimal age;
    private ArrayList<String> orbitalObjects;
    private int abundance;
    private BigDecimal mass;

    public String toString() {
        return "Star: " + getName() + " (" + classification + ")";
    }

    public void addOrbitalObject(String orbitalObject){
        orbitalObjects.add(orbitalObject);
    }
}