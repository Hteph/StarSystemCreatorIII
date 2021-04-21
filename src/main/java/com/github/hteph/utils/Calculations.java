package com.github.hteph.utils;

import com.github.hteph.repository.CentralRegistry;
import com.github.hteph.repository.objects.Planet;
import com.github.hteph.repository.objects.Star;

import static com.github.hteph.utils.NumberUtilities.cubed;
import static com.github.hteph.utils.NumberUtilities.sqrt;
import static com.github.hteph.utils.NumberUtilities.squared;

public class Calculations {

    static double getOrbitalPeriodOfPlanet(Planet planet) {

        var orbitR = planet.getOrbitalFacts().getOrbitalDistance().doubleValue();

        var starMass = ((Star) CentralRegistry.getFromArchive(planet.getOrbitalFacts().getOrbitsAround()))
                .getMass().doubleValue();
        //in earth days
        return sqrt(cubed(orbitR) / starMass);
    }

    static double getMassOfPlanet(Planet planet) {
        //In earth eqvivalents
        return cubed(planet.getRadius() / 6380.0) * planet.getDensity().doubleValue();
    }

    static double getGravityOfPlanet(Planet planet) {
        //in earth gravities
        return planet.getMass().doubleValue() / squared((planet.getRadius() / 6380.0));
    }

}
