package com.github.hteph.generators;

import com.github.hteph.repository.CentralRegistry;
import com.github.hteph.repository.objects.*;
import com.github.hteph.tables.TableMaker;
import com.github.hteph.utils.Dice;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static com.github.hteph.utils.NumberUtilities.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JovianGenerator {

    static String Generator(String archiveID,
                                   String name,
                                   String description,
                                   String classificationName,
                                   BigDecimal orbitDistance,
                                   char orbitalObjectClass,
                                   Star orbitingAround) {

        //String lifeType; // TODO allow for Jovian life in the future

        final var TWO = new MathContext(2);
        final var THREE = new MathContext(3);
        final var FOUR = new MathContext(4);

        var gasGiantBuilder = Planet.builder();
        gasGiantBuilder.name(name);
        gasGiantBuilder.description(description);
        gasGiantBuilder.classificationName(classificationName);

        var orbitalFacts = OrbitalFacts.builder();
        orbitalFacts.orbitsAround(orbitingAround.getArchiveID());
        orbitalFacts.orbitalDistance(orbitDistance);

        double snowLine = 5 * Math.pow(orbitingAround.getLumosity().doubleValue(), 0.5);
        boolean innerZone = orbitDistance.doubleValue() < snowLine;
        int jovianRadius;
        double mass;

        if (orbitalObjectClass == 'J') { //TODO something is off here, check calc vs a source
            mass = 250 * Dice._3d6() + Dice.d10() * 100;
            jovianRadius = (int) (60000 + (Dice.d10() - orbitingAround.getAge().doubleValue() / 2.0) * 2000);
            gasGiantBuilder.radius(jovianRadius);
            gasGiantBuilder.mass(BigDecimal.valueOf(mass));
        } else {
            jovianRadius = Dice._2d6() * 7000;
            mass = cubed(jovianRadius / 6380d) * (innerZone ? 0.1 * Dice.d10() * 0.025 : 0.08 * Dice.d10() * 0.025);
            gasGiantBuilder.mass(BigDecimal.valueOf(mass).round(FOUR));
            gasGiantBuilder.radius(jovianRadius);
        }
        orbitalFacts.orbitalPeriod(BigDecimal.valueOf(sqrt(cubed(orbitDistance.doubleValue()) / orbitingAround.getMass().doubleValue()))
                                             .round(FOUR)); //in earth years
//Eccentricity and Inclination

        int eccentryMod = 1; //TODO This should probably be changed for the smaller Jovians

        orbitalFacts.orbitalEccentricity(BigDecimal.valueOf(eccentryMod * (Dice._2d6() - 2) / (100.0 * Dice.d6())));
        gasGiantBuilder.axialTilt(BigDecimal.valueOf((Dice._2d6() - 2) / (1.0 * Dice.d6()))
                                            .round(THREE)); //TODO this should be expanded
        orbitalFacts.orbitalInclination(BigDecimal.valueOf(eccentryMod * (Dice._2d6()) / (1 + mass / 10.0))
                                                  .round(THREE));
//Rotational Period
        double tidalForce = orbitingAround.getMass().doubleValue() * 26640000 / cubed(orbitDistance.doubleValue() * 400);
        double rotationalPeriod = (Dice._2d6() + 8) * (1 + 0.1 * (tidalForce * orbitingAround.getAge().doubleValue() - sqrt(mass)));
        if (Dice.d6() < 2) rotationalPeriod = Math.pow(rotationalPeriod, Dice.d6());
        gasGiantBuilder.rotationalPeriod(BigDecimal.valueOf(rotationalPeriod).round(FOUR));

//Magnetic field
        int[] magneticMassArray = {0, 50, 200, 500};
        Double[] magneticMassArrayMin = {0.1, 0.25, 0.5, 1.5, 1.5};
        Double[] magneticMassArrayMax = {1d, 1.5, 3d, 25d, 25d};

        gasGiantBuilder.magneticField(BigDecimal.valueOf((TableMaker.makeRoll((int) mass, magneticMassArray, magneticMassArrayMax)
                                                   - TableMaker.makeRoll((int) mass, magneticMassArray, magneticMassArrayMin))
                                                  / 10.0
                                                  * Dice.d10()).round(TWO));
//Temperature
        gasGiantBuilder.surfaceTemp(((int) (255 / sqrt((orbitDistance.doubleValue() / sqrt(orbitingAround.getLumosity().doubleValue()))))));

        //TODO here there should be moons generated!!!!

        // Inner group of moonlets,rings and catched stuff
        // Group of major moons
        // outer group of catched asteroids and stuff

        gasGiantBuilder.orbitalFacts(orbitalFacts.build());
        return CentralRegistry.putInArchive(gasGiantBuilder.build());

    }
}
