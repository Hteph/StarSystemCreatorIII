package com.github.hteph.generators;


import com.github.hteph.repository.objects.AtmosphericGases;
import com.github.hteph.repository.objects.OrbitalFacts;
import com.github.hteph.repository.objects.Planet;
import com.github.hteph.repository.objects.Star;
import com.github.hteph.repository.objects.StellarObject;
import com.github.hteph.repository.objects.TemperatureFacts;
import com.github.hteph.tables.FindAtmoPressure;
import com.github.hteph.tables.TableMaker;
import com.github.hteph.tables.TectonicActivityTable;
import com.github.hteph.utils.Dice;
import com.github.hteph.utils.StreamUtilities;
import com.github.hteph.utils.enums.Breathing;
import com.github.hteph.utils.enums.HydrosphereDescription;
import com.github.hteph.repository.CentralRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.github.hteph.utils.NumberUtilities.*;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class GenerateTerrestrialPlanet {

    static final MathContext TWO = new MathContext(2);
    static final MathContext THREE = new MathContext(3);
    static final MathContext FOUR = new MathContext(4);
    static final MathContext FIVE = new MathContext(5);

    public static String generate(final String archiveID,
                                  final String name,
                                  final String description,
                                  final String classificationName,
                                  BigDecimal orbitDistance,
                                  final char orbitalObjectClass,
                                  final String centralObjectArchiveID,
                                  final double lunarOrbitDistance) {

        //Last parameter (lunarOrbitDistance) is only used if this is a moon to be created

        StellarObject centralObject = CentralRegistry.getFromArchive(centralObjectArchiveID);


        double orbitalPeriod; //in earth years

        boolean planetLocked = false;
        //boolean moonLocked = false; //Not implemented yet
        double rotationalPeriod;
        double tectonicActivity;
        String tectonicCore;
        double magneticField;
        int baseTemperature;
        HydrosphereDescription hydrosphereDescription;
        int hydrosphere;
        int waterVaporFactor;
        TreeSet<AtmosphericGases> atmoshericComposition;
        BigDecimal atmoPressure;
        double albedo;
        double greenhouseFactor;
        String tectonicActivityGroup;
        Planet moonsPlanet = null;
        double moonOrbit = 0; //in planetary radii
        double moonsPlanetMass = 0;
        Double lunarOrbitalPeriod;
        Double moonTidal = null;

        double moonsPlanetsRadii = 0;

        boolean hasGaia;
        Breathing lifeType;
        Star orbitingAround;
        double tidalForce = 0;
        double tidelock;


        var planetBuilder = Planet.builder()
                                  .archiveID(archiveID)
                                  .name(name)
                                  .description(description)
                                  .classificationName(classificationName);

        var orbitalFacts = OrbitalFacts.builder();


        //Ugly hack to be able to reuse this method for generating moons
        if (String.valueOf(orbitalObjectClass).equalsIgnoreCase("m")) {
            moonsPlanet = (Planet) centralObject;
            moonsPlanetMass = moonsPlanet.getMass().doubleValue();
            moonsPlanetsRadii = moonsPlanet.getRadius();

            orbitalFacts.orbitsAround(moonsPlanet.getArchiveID());

            moonTidal = (moonsPlanetMass * 26640000 / 333000.0 / Math.pow(moonsPlanetsRadii * moonOrbit * 400 / 149600000, 3));
            orbitingAround = (Star) CentralRegistry.getFromArchive(moonsPlanet.getOrbitalFacts().getOrbitsAround());
            orbitDistance = moonsPlanet.getOrbitalFacts().getOrbitalDistance();

        } else orbitingAround = (Star) centralObject;

        var localOrbitDistance = BigDecimal.valueOf(String.valueOf(orbitalObjectClass).equalsIgnoreCase("m")
                                                            ? lunarOrbitDistance
                                                            : 0);

        final double SNOWLINE = 5 * sqrt(orbitingAround.getLumosity().doubleValue());
        final boolean IS_INNER_ZONE = orbitDistance.doubleValue() < SNOWLINE;

        // size may not be all, but here it is set
        //TODO add greater varity for moon objects, depending on planet
        final int planetRadius = Dice._2d6() * getBaseSize(orbitalObjectClass, moonsPlanet);
        planetBuilder.radius(planetRadius);

        //density
        final double density = getDensity(orbitDistance, orbitingAround, SNOWLINE);
        final double mass = cubed(planetRadius / 6380.0) * density;
        final double gravity = mass / squared((planetRadius / 6380.0));

        lunarOrbitalPeriod = null;
        if ("m".equalsIgnoreCase(Character.toString(orbitalObjectClass))) {
            lunarOrbitalPeriod = sqrt(cubed((moonOrbit * moonsPlanetsRadii) / 400000) * 793.64 / (moonsPlanetMass + mass));
        }
        ;

        orbitalPeriod = sqrt(cubed(orbitDistance.doubleValue()) / orbitingAround.getMass()
                                                                                .doubleValue()); //in earth days

        planetBuilder.mass(BigDecimal.valueOf(mass).round(THREE))
                     .gravity(BigDecimal.valueOf(gravity).round(TWO))
                     .density(BigDecimal.valueOf(density).round(TWO))
                     .lunarOrbitalPeriod(lunarOrbitalPeriod != null ? BigDecimal.valueOf(lunarOrbitalPeriod) : null);

        //Eccentricity and Inclination

        final double axialTilt = (int) (10 * Dice._3d6() / 2.0 * Math.random());
        final int eccentryMod = getEccentryMod(orbitalObjectClass);
        double eccentricity = eccentryMod * (Dice._2d6() - 2) / (100.0 * Dice.d6());

        planetBuilder.axialTilt(BigDecimal.valueOf(axialTilt).round(THREE));
        orbitalFacts.orbitalInclination(BigDecimal.valueOf(eccentryMod * (Dice._2d6()) / (1.0 + mass / 10.0))
                                                  .round(THREE));

        // TODO tidelocked or not should take into consideration moons too!
        List<String> moonList = null;
        if (!String.valueOf(orbitalObjectClass).equalsIgnoreCase("m")) {
            moonList = GenerateMoons.terrestialMoons(name, archiveID, orbitDistance, IS_INNER_ZONE);
            planetBuilder.moonList(moonList);
        }

        final double sumOfLunarTidal = StreamUtilities.getStreamEmptyIfNull(moonList)
                                                      .map(CentralRegistry::getFromArchive)
                                                      .map(m -> calcLunarTidal(planetRadius, (Planet) m))
                                                      .reduce(0d, Double::sum);

//        if (moonList != null && !moonList.isEmpty()) {
//            double[] lunartidal = new double[planet.getLunarObjects().size()];
//            for (int n = 0; n < planet.getLunarObjects().size(); n++) {
//                for (String moonID : planet.getLunarObjects()) {
//                    OrbitalObjects moon = (OrbitalObjects) CentralRegistry.getFromArchive(moonID);
//
//                    if (moon instanceof Planet) {
//                        lunartidal[n] = calcLunarTidal(planet, (Planet) moon);
//                        ((Planet) moon).setLunarTidal(lunartidal[n]);
//                    }
//                }
//            }
//            sumOfLunarTidal = DoubleStream.of(lunartidal).sum();
//        }
        boolean tidelocked = false;
        if (moonTidal != null) {
            if (moonTidal > 1) planetLocked = true;
            planetBuilder.planetLocked(planetLocked);
        } else {
            tidalForce = (orbitingAround.getMass()
                                        .doubleValue() * 26640000 / cubed(orbitDistance.doubleValue() * 400.0))
                    / (1.0 + sumOfLunarTidal);
            tidelock = (0.83 + (Dice._2d6() - 2) * 0.03) * tidalForce * orbitingAround.getAge().doubleValue() / 6.6;
            if (tidelock > 1) {
                tidelocked = true;
                //TODO Tidelocked planets generally can't have moon, but catched objects should be allowed?
                planetBuilder.moonList(Collections.emptyList());
            }
        }

        planetBuilder.tideLockedStar(tidelocked);

        //Rotation - day/night cycle
        if (tidelocked) {
            rotationalPeriod = orbitalPeriod * 365;
        } else if (planetLocked) {
            rotationalPeriod = lunarOrbitalPeriod;
        } else {
            rotationalPeriod = (Dice.d6() + Dice.d6() + 8)
                    * (1 + 0.1 * (tidalForce * orbitingAround.getAge().doubleValue() - sqrt(mass)));

            if (Dice.d6(2)) rotationalPeriod = Math.pow(rotationalPeriod, Dice.d6());

            if (rotationalPeriod > orbitalPeriod / 2.0) {

                double[] resonanceArray = {0.5, 2 / 3.0, 1, 1.5, 2, 2.5, 3, 3.5};
                double[] eccentricityEffect = {0.1, 0.15, 0.21, 0.39, 0.57, 0.72, 0.87, 0.87};

                int resultResonance = Arrays.binarySearch(resonanceArray, rotationalPeriod / orbitalPeriod);

                if (resultResonance < 0) {
                    eccentricity = eccentricityEffect[-resultResonance - 2];
                    rotationalPeriod = resonanceArray[-resultResonance - 2];
                } else {
                    resultResonance = Math.min(resultResonance, 6); //TODO there is something fishy here, Edge case of greater than
                    // 0.87 relation still causes problem Should rethink methodology
                    eccentricity = eccentricityEffect[resultResonance];
                    rotationalPeriod = resonanceArray[-resultResonance];
                }
            }
        }

        orbitalFacts.orbitalEccentricity(BigDecimal.valueOf(eccentricity));
        planetBuilder.rotationalPeriod(BigDecimal.valueOf(rotationalPeriod));

        //TODO tectonics should include moons!
        //TODO c type should have special treatment

        tectonicCore = findTectonicGroup(IS_INNER_ZONE, density);
        tectonicActivityGroup = getTectonicActivityGroup(orbitingAround, tidalForce, mass);

        planetBuilder.tectonicCore(tectonicCore)
                     .tectonicActivityGroup(tectonicActivityGroup)
                     .magneticField(BigDecimal.valueOf(getMagneticField(rotationalPeriod,
                                                                        tectonicCore,
                                                                        tectonicActivityGroup,
                                                                        orbitingAround,
                                                                        density,
                                                                        mass
                     )));

        //Temperature
        baseTemperature = (int) (255 / sqrt((orbitDistance.doubleValue()
                / sqrt(orbitingAround.getLumosity().doubleValue()))));

        //base temp is an value of little use beyond this generator and is not propagated to the planet object

        //Hydrosphere
        hydrosphereDescription = findHydrosphereDescription(IS_INNER_ZONE, baseTemperature);
        hydrosphere = findTheHydrosphere(hydrosphereDescription, planetRadius);
        waterVaporFactor = getWaterVaporFactor(baseTemperature, hydrosphereDescription, hydrosphere);

        planetBuilder.hydrosphereDescription(hydrosphereDescription);
        planetBuilder.hydrosphere(hydrosphere);

        //Atmoshperic details
        atmoshericComposition = MakeAtmosphere.createPlanetary(orbitingAround,
                                                               baseTemperature,
                                                               tectonicActivityGroup,
                                                               planetRadius,
                                                               gravity,
                                                               planetBuilder);
        var tempPlanet = planetBuilder.build();

        atmoPressure = FindAtmoPressure.calculate(tectonicActivityGroup,
                                                  hydrosphere,
                                                  tempPlanet.isBoilingAtmo(),
                                                  mass,
                                                  atmoshericComposition);

        // TODO Special considerations for c objects, this should be expanded upon when these gets more details

        if (orbitalObjectClass == 'c') { //These should never had a chance to get an "real" atmosphere in the first
            // place but may have some traces
            if (Dice.d6(6)) atmoPressure = BigDecimal.valueOf(0);
            else atmoPressure = BigDecimal.valueOf(0.001);
        }

        if (atmoPressure.doubleValue() == 0) atmoshericComposition.clear();
        if (atmoshericComposition.size() == 0) { //There are edge cases where all of atmo has boiled away
            atmoPressure = BigDecimal.valueOf(0);
            planetBuilder.hydrosphereDescription(HydrosphereDescription.REMNANTS);
            planetBuilder.hydrosphere(0);
        }

        planetBuilder.atmoPressure(atmoPressure);
        // The composition could be adjusted for the existence of life, so is set below

        //Bioshpere
        hasGaia = testLife(baseTemperature, atmoPressure.doubleValue(), hydrosphere, atmoshericComposition);
        if (hasGaia) lifeType = findLifeType(atmoshericComposition);
        else lifeType = Breathing.NONE;

        if (lifeType.equals(Breathing.OXYGEN)) adjustForOxygen(atmoPressure.doubleValue(), atmoshericComposition);

        albedo = findAlbedo(IS_INNER_ZONE, atmoPressure.doubleValue(), hydrosphereDescription, hydrosphere);
        double greenhouseGasEffect = findGreenhouseGases(atmoshericComposition, atmoPressure.doubleValue());

        greenhouseFactor = 1 + sqrt(atmoPressure.doubleValue()) * 0.01 * (Dice._2d6() - 1)
                + sqrt(greenhouseGasEffect) * 0.1
                + waterVaporFactor * 0.1;

        //TODO Here adding some Gaia moderation factor (needs tweaking probably) moving a bit more towards
        // water/carbon ideal
        if (lifeType.equals(Breathing.OXYGEN) && baseTemperature > 350) greenhouseFactor *= 0.8;
        if (lifeType.equals(Breathing.OXYGEN) && baseTemperature < 250) greenhouseFactor *= 1.2;

        int surfaceTemp = getSurfaceTemp(baseTemperature, atmoPressure, albedo, greenhouseFactor, hasGaia);
        planetBuilder.atmosphericComposition(atmoshericComposition);
        planetBuilder.lifeType(lifeType);

        //Climate -------------------------------------------------------
        // sets all the temperature stuff from axial tilt etc etc

        var temperatureFacts = setAllKindOfLocalTemperature(atmoPressure.doubleValue(),
                                                            hydrosphere,
                                                            rotationalPeriod,
                                                            axialTilt,
                                                            surfaceTemp,
                                                            orbitalPeriod); // sets all the temperature stuff from axial tilt etc etc
        temperatureFacts.surfaceTemp(surfaceTemp);
        //TODO Weather and day night temp cycle

        planetBuilder.orbitalFacts(orbitalFacts.build());
        planetBuilder.temperatureFacts(temperatureFacts.build());
        return CentralRegistry.putInArchive(planetBuilder.build());
    }

    private static int getWaterVaporFactor(int baseTemperature, HydrosphereDescription hydrosphereDescription, int hydrosphere) {
        int waterVaporFactor;
        if (hydrosphereDescription.equals(HydrosphereDescription.LIQUID)
                || hydrosphereDescription.equals(HydrosphereDescription.ICE_SHEET)) {
            waterVaporFactor = (int) Math.max(0, (baseTemperature - 240) / 100.0 * hydrosphere * (Dice._2d6() - 1));
        } else {
            waterVaporFactor = 0;
        }
        return waterVaporFactor;
    }

    private static double getMagneticField(double rotationalPeriod, String tectonicCore, String tectonicActivityGroup, Star orbitingAround, double density, double mass) {
        double magneticField;
        if (tectonicCore.contains("metal")) {
            magneticField = 10 / (sqrt((rotationalPeriod / 24.0)))
                    * squared(density)
                    * sqrt(mass)
                    / orbitingAround.getAge().doubleValue();
            if (tectonicCore.contains("small")) magneticField *= 0.5;
            if (tectonicCore.contains("medium")) magneticField *= 0.75;
            if (tectonicActivityGroup.equals("Dead")) magneticField = Dice.d6() / 15.0;
        } else {
            magneticField = Dice.d6() / 20.0;
        }
        return magneticField;
    }

    private static String getTectonicActivityGroup(Star orbitingAround, double tidalForce, double mass) {
        double tectonicActivity;
        String tectonicActivityGroup;
        tectonicActivity = (5 + Dice._2d6() - 2) * Math.pow(mass, 0.5) / orbitingAround.getAge().doubleValue();
        tectonicActivity *= (1 + 0.5 * tidalForce);
        tectonicActivityGroup = TectonicActivityTable.findTectonicActivityGroup(tectonicActivity);
        return tectonicActivityGroup;
    }

    private static int getEccentryMod(char orbitalObjectClass) {
        int eccentryMod = 1;
        if (orbitalObjectClass == 'C' || orbitalObjectClass == 'c') eccentryMod += 3;
        return eccentryMod;
    }

    private static double getDensity(BigDecimal orbitDistance, Star orbitingAround, double snowLine) {
        double density;
        if (orbitDistance.doubleValue() < snowLine) {
            density = 0.3 + (Dice._2d6() - 2) * 0.127 / Math.pow(
                    0.4 + (orbitDistance.doubleValue() / sqrt(orbitingAround.getLumosity().doubleValue())), 0.67);
        } else {
            density = 0.3 + (Dice._2d6() - 2) * 0.05;
        }
        return density;
    }

    private static int getBaseSize(char orbitalObjectClass, StellarObject moonsPlanet) {

        //TODO add greater varity for moon objects, depending on planet
        int baseSize = 900; //Default for planets
        if (orbitalObjectClass == 'M') {
            List<Integer> baseSizeList = Arrays.asList(100, 200, 300, 400, 500, 600, 700);
            TableMaker.makeRoll(Dice.d10(), baseSizeList);
            if (moonsPlanet instanceof Planet) baseSize = Math.min(baseSize, ((Planet) moonsPlanet).getRadius() / 8);
        } else if (orbitalObjectClass == 'm') {
            List<Integer> baseSizeList = Arrays.asList(1, 5, 10, 20, 30, 40, 50);
            TableMaker.makeRoll(Dice.d10(), baseSizeList);
            if (moonsPlanet instanceof Planet) baseSize = Math.min(baseSize, ((Planet) moonsPlanet).getRadius() / 8);
        } else if (orbitalObjectClass == 't' || orbitalObjectClass == 'c') baseSize = 90;
        return baseSize;
    }

    private static int getSurfaceTemp(int baseTemperature,
                                      BigDecimal atmoPressure,
                                      double albedo,
                                      double greenhouseFactor,
                                      boolean hasGaia) {

        // My take on the effect of greenhouse and albedo on temperature max planerary temp is 1000 and the half
        // point is 400
        double surfaceTemp;
        if (hasGaia) {
            surfaceTemp = 400 * (baseTemperature * albedo * greenhouseFactor)
                    / (350d + baseTemperature * albedo * greenhouseFactor);
        } else if (atmoPressure.doubleValue() > 0) {
            surfaceTemp = 800 * (baseTemperature * albedo * greenhouseFactor)
                    / (400d + baseTemperature * albedo * greenhouseFactor);
        } else {
            surfaceTemp = 1200 * (baseTemperature * albedo * greenhouseFactor)
                    / (800d + baseTemperature * albedo * greenhouseFactor);
        }
        return (int) surfaceTemp;
    }

    private static double calcLunarTidal(double radius, Planet moon) {

        double tidal = moon.getMass().doubleValue() * 26640000
                / 333000d
                / cubed(radius * moon.getLunarOrbitDistance().doubleValue() * 400
                                / 149600000d);
        moon.setLunarTidal(BigDecimal.valueOf(tidal).round(FIVE));
        return tidal;
    }

    private static void adjustForOxygen(double atmoPressure, TreeSet<AtmosphericGases> atmosphericComposition) {

        Map<String, AtmosphericGases> atmoMap = atmosphericComposition
                .stream()
                .collect(Collectors.toMap(AtmosphericGases::getName, x -> x));

        int oxygenMax = Math.max(50, (int) (Dice._3d6() * 2 / atmoPressure)); //This could be a bit more involved and interesting

        if (atmoMap.containsKey("CO2")) {
            if (atmoMap.get("CO2").getPercentageInAtmo() > oxygenMax) {
                AtmosphericGases co2 = atmoMap.get("CO2");
                atmoMap.remove("CO2");
                atmoMap.put("O2", AtmosphericGases.builder().name("O2").percentageInAtmo(oxygenMax).build());
                //perhaps the remnant CO should be put in as N2 instead?
                atmoMap.put("CO2", AtmosphericGases.builder()
                                                   .name("CO2")
                                                   .percentageInAtmo(co2.getPercentageInAtmo() - oxygenMax)
                                                   .build());

            } else {
                AtmosphericGases co2 = atmoMap.get("CO2");
                atmoMap.remove("CO2");
                atmoMap.put("O2", AtmosphericGases.builder()
                                                  .name("O2")
                                                  .percentageInAtmo(co2.getPercentageInAtmo())
                                                  .build());
            }
        } else { //if no CO2 we just find the largest and take from that
            AtmosphericGases gas = atmosphericComposition.pollFirst();
            if (gas != null) {
                if (gas.getPercentageInAtmo() < oxygenMax) {
                    atmoMap.put("O2", AtmosphericGases.builder()
                                                      .name("O2")
                                                      .percentageInAtmo(gas.getPercentageInAtmo())
                                                      .build());
                } else {
                    atmoMap.put("O2", AtmosphericGases.builder()
                                                      .name("O2")
                                                      .percentageInAtmo(oxygenMax)
                                                      .build());
                    atmoMap.put(gas.getName(), AtmosphericGases.builder()
                                                               .name("O2")
                                                               .percentageInAtmo(gas.getPercentageInAtmo() -
                                                                                         oxygenMax)
                                                               .build());
                }
            }
        }

        removeCombustibles(atmoMap);
        atmosphericComposition.clear();
        atmosphericComposition.addAll(atmoMap.values());
    }

    private static void removeCombustibles(Map<String, AtmosphericGases> atmoMap) {
        int removedpercentages = 0;
        if (atmoMap.containsKey("CH4")) {
            removedpercentages += atmoMap.get("CH4").getPercentageInAtmo();
            atmoMap.remove("CH4");
        }
        if (atmoMap.containsKey("H2")) {
            removedpercentages += atmoMap.get("H2").getPercentageInAtmo();
            atmoMap.remove("H2");
        }
//        There are indications that ammonia may be present in an oxygen atmosphere
        // so this is removed for now
//        if (atmoMap.containsKey("NH3")) {
//            removedpercentages += atmoMap.get("NH3").getPercentageInAtmo();
//            atmoMap.remove("NH3");
//        }
        if (removedpercentages > 0) {
            if (atmoMap.containsKey("N2")) {
                removedpercentages += atmoMap.get("N2").getPercentageInAtmo();
                atmoMap.remove("N2");
            }
            atmoMap.put("N2", AtmosphericGases.builder()
                                              .name("N2")
                                              .percentageInAtmo(removedpercentages)
                                              .build());
        }
    }

    private static Breathing findLifeType(Set<AtmosphericGases> atmoshericComposition) {
        //TODO Allow for alternate gases such as Cl2
        return atmoshericComposition.stream()
                                    .map(AtmosphericGases::getName)
                                    .anyMatch(b -> b.equals("NH3"))
                ? Breathing.AMMONIA
                : Breathing.OXYGEN;
    }

    /**
     * /*TODO
     * great season variations from the orbital eccentricity and multiple stars system
     * day night variation estimation, is interesting for worlds with short year/long days
     */

    private static TemperatureFacts.TemperatureFactsBuilder setAllKindOfLocalTemperature(double atmoPressure, int hydrosphere,
                                                                 double rotationalPeriod, double axialTilt, double surfaceTemp,
                                                                 double orbitalPeriod) {

        double[][] temperatureRangeBand = new double[][]{ // First is Low Moderation atmos, then Average etc
                {1.10, 1.07, 1.05, 1.03, 1.00, 0.97, 0.93, 0.87, 0.78, 0.68},
                {1.05, 1.04, 1.03, 1.02, 1.00, 0.98, 0.95, 0.90, 0.82, 0.75},
                {1.02, 1.02, 1.02, 1.01, 1.00, 0.99, 0.98, 0.95, 0.91, 0.87},
                {1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00}
        };

        double[] summerTemperature = new double[10];
        double[] winterTemperature = new double[10];
        double[] latitudeTemperature = new double[10];
        double[] baseTemperature = new double[10];

        int testModeration = 0;
        testModeration += (hydrosphere - 60) / 10;
        testModeration += (atmoPressure < 0.1) ? -3 : 1;
        testModeration += (int) atmoPressure;
        testModeration += (rotationalPeriod < 10) ? -3 : 1;
        testModeration += (int) (Math.sqrt(rotationalPeriod / 24)); //Shouldn't this be negative?
        testModeration += (int) (10 / axialTilt);

        String atmoModeration;
        if (atmoPressure == 0) atmoModeration = "No";
        else if (atmoPressure > 10) atmoModeration = "Extreme";
        else if (testModeration < -2) atmoModeration = "Low";
        else if (testModeration > 2) atmoModeration = "High";
        else atmoModeration = "Average";

        int atmoIndex;
        switch (atmoModeration) {
            case "High":
                atmoIndex = 2;
                break;
            case "Average":
                atmoIndex = 1;
                break;
            case "Extreme":
                atmoIndex = 3;
                break;
            default:
                atmoIndex = 0;
                break;
        }

        for (int i = 0; i < 10; i++) {
            latitudeTemperature[i] = temperatureRangeBand[atmoIndex][i] * surfaceTemp;
        }

        for (int i = 0; i < 10; i++) {
            baseTemperature[i] = latitudeTemperature[i] - 274;
        }

        for (int i = 0; i < 10; i++) {

            double seasonEffect = 1;
            // This part is supposed to shift the rangebands for summer /winter effects, it makes an
            // (to me unproven) assumption that winter temperatures at the poles is not changed by seasonal effects
            // this feels odd but I have to delve further into the science before I dismiss it.
            // the effect occurs from the intersection of axial tilt effects and rangeband effects in a way that
            //makes me suspect it is unintentional.
            int axialTiltEffect = (int) (axialTilt / 10);
            int summer = Math.max(0, i - axialTiltEffect);
            int winter = Math.min(9, i + axialTiltEffect);

            if (i < 3 && axialTiltEffect < 4) seasonEffect *= 0.75;
            if (i > 8 && axialTiltEffect > 3) seasonEffect *= 2;
            if (orbitalPeriod < 0.25 && !atmoModeration.equals("Low")) seasonEffect *= 0.75;
            if (orbitalPeriod > 3 && !atmoModeration.equals("High") && axialTilt > 40) seasonEffect *= 1.5;

            summerTemperature[i] = (int) (latitudeTemperature[summer] - latitudeTemperature[i]) * seasonEffect;
            winterTemperature[i] = (int) (latitudeTemperature[winter] - latitudeTemperature[i]) * seasonEffect;
        }
        var temperatureFacts = TemperatureFacts.builder();
        return temperatureFacts.rangeBandTemperature(DoubleStream.of(baseTemperature)
                                                                 .mapToInt(t -> (int) Math.ceil(t))
                                                                 .toArray())
                               .rangeBandTempSummer(DoubleStream.of(summerTemperature)
                                                                .mapToInt(t -> (int) Math.ceil(t))
                                                                .toArray())
                               .rangeBandTempWinter(DoubleStream.of(winterTemperature)
                                                                .mapToInt(t -> (int) Math.ceil(t))
                                                                .toArray());
    }

    /*TODO
     * This should be reworked (in conjuction with atmo) to remove CL and F from naturally occuring and instead
     * treat them similar to Oxygen. Also the Ammonia is dependent on free water as written right now
     */

    private static boolean testLife(int baseTemperature,
                                    double atmoPressure,
                                    int hydrosphere,
                                    Set<AtmosphericGases> atmoshericComposition) {

        double lifeIndex = 0;

        if (baseTemperature < 100 || baseTemperature > 450) lifeIndex -= 5;
        else if (baseTemperature < 250 || baseTemperature > 350) lifeIndex -= 1;
        else lifeIndex += 3;

        if (atmoPressure < 0.1) lifeIndex -= 10;
        else if (atmoPressure > 5) lifeIndex -= 1;

        if (hydrosphere < 1) lifeIndex -= 3;
        else if (hydrosphere > 3) lifeIndex += 1;

        if (atmoshericComposition.stream().anyMatch(s -> s.getName().equals("NH3") && Dice.d6() < 3)) lifeIndex += 4;

        return lifeIndex > 0; //Nod to Gaia-theory, if there is any chance of life it will aways be life present
    }

    private static double findGreenhouseGases(Set<AtmosphericGases> atmoshericComposition, double atmoPressure) {
        double tempGreenhouseGasEffect = 0;

        for (AtmosphericGases gas : atmoshericComposition) {

            switch (gas.getName()) {
                case "CO2":
                    tempGreenhouseGasEffect += gas.getPercentageInAtmo() * atmoPressure;
                    break;
                case "CH4":
                    tempGreenhouseGasEffect += gas.getPercentageInAtmo() * atmoPressure * 4;
                    break;
                case "SO2":
                case "NH3":
                case "NO2":
                case "H2S":
                    tempGreenhouseGasEffect += gas.getPercentageInAtmo() * atmoPressure * 8;
                    break;
                case "H2SO4":
                    tempGreenhouseGasEffect += gas.getPercentageInAtmo() * atmoPressure * 16;
                    break;
                default:
                    tempGreenhouseGasEffect += gas.getPercentageInAtmo() * atmoPressure * 0;
                    break;
            }
        }
        return tempGreenhouseGasEffect;
    }

    private static double findAlbedo(boolean InnerZone,
                                     double atmoPressure,
                                     HydrosphereDescription hydrosphereDescription,
                                     int hydrosphere) {

        int mod = 0;
        int[] randAlbedoArray;
        Double[] albedoBase = new Double[]{0.75, 0.85, 0.95, 1.05, 1.15};

        if (InnerZone) {
            randAlbedoArray = new int[]{0, 2, 4, 7, 10};

            if (atmoPressure < 0.05) mod = 2;
            if (atmoPressure > 50) {
                mod = -4;
            } else if (atmoPressure > 5) {
                mod = -2;
            }
            if (hydrosphere > 50
                    && hydrosphereDescription.equals(HydrosphereDescription.ICE_SHEET)
                    && mod > -2)
                mod = -2;
            if (hydrosphere > 90
                    && hydrosphereDescription.equals(HydrosphereDescription.ICE_SHEET)
                    && mod > -4)
                mod = -4;

        } else {
            randAlbedoArray = new int[]{0, 4, 6, 8, 10};
            if (atmoPressure > 1) mod = 1;
        }
        return TableMaker.makeRoll(Dice._2d6() + mod, randAlbedoArray, albedoBase) + (Dice.d10() - 1) * 0.01;
    }

    private static int findTheHydrosphere(HydrosphereDescription hydrosphereDescription, int radius) {

        //         zeroHydro = () -> 0;
        //         superficialHydro = () -> Dice.d10()/2;
        //         vLowHydro = Dice::d10;
        //         lowHydro = () -> Dice.d10() + 10;
        //         mediumHydro = () -> Dice.d20() + 20;
        //         highHydro = () -> Dice.d20()+ Dice.d20()+ Dice.d20() +37;
        //         vHighHydro = () -> 100;

        List<Supplier<Integer>> hydroList = Arrays.asList(() -> Dice.d10() / 2,
                                                          () -> Dice.d10() / 2 + 5,
                                                          () -> Dice.d10() + 10,
                                                          () -> Dice.d20() + 20,
                                                          () -> Dice.d20() + Dice.d20() + Dice.d20() + 37,
                                                          () -> 100);
        int[] wetSmallPlanetHydro = {2, 5, 9, 10, 12, 13};
        int[] wetMediumPlanetHydro = {2, 4, 7, 9, 11, 12};
        int[] wetLargePlanetHydro = {0, 2, 3, 4, 7, 12};

        Integer tempHydro = 0;

        if (hydrosphereDescription.equals(HydrosphereDescription.LIQUID)
                || hydrosphereDescription.equals(HydrosphereDescription.ICE_SHEET)) {
            if (radius < 2000) {
                tempHydro = TableMaker.makeRoll(Dice._2d6(), wetSmallPlanetHydro, hydroList).get();
            } else if (radius < 4000) {
                tempHydro = TableMaker.makeRoll(Dice._2d6(), wetMediumPlanetHydro, hydroList).get();
            } else if (radius < 7000) {
                tempHydro = TableMaker.makeRoll(Dice._2d6(), wetLargePlanetHydro, hydroList).get();
            }
        } else if (hydrosphereDescription.equals(HydrosphereDescription.CRUSTAL)) tempHydro = 100;
        else if (hydrosphereDescription.equals(HydrosphereDescription.REMNANTS)) tempHydro = Dice.d6() / 2;

        tempHydro = Math.min(100, tempHydro);

        return tempHydro;
    }

    private static HydrosphereDescription findHydrosphereDescription(boolean InnerZone, int baseTemperature) {
        HydrosphereDescription tempHydroD;
        if (!InnerZone) {
            tempHydroD = HydrosphereDescription.CRUSTAL;
        } else if (baseTemperature > 500) {
            tempHydroD = HydrosphereDescription.NONE;
        } else if (baseTemperature > 370) {
            tempHydroD = HydrosphereDescription.VAPOR;
        } else if (baseTemperature > 245) {
            tempHydroD = HydrosphereDescription.LIQUID;
        } else {
            tempHydroD = HydrosphereDescription.ICE_SHEET;
        }
        return tempHydroD;
    }

    private static String findTectonicGroup(boolean InnerZone, double density) {

        String tempTectonics;
        if (InnerZone) {
            if (density < 0.7) {
                if (Dice.d6() < 4) {
                    tempTectonics = "Silicates core";
                } else {
                    tempTectonics = "Silicates, small metal core";
                }
            } else if (density < 1) {
                tempTectonics = "Iron-nickel, medium metal core";
            } else {
                tempTectonics = "Iron-nickel, large metal core";
            }
        } else {
            if (density < 0.3) {
                tempTectonics = "Ice core";
            } else if (density < 1) {
                tempTectonics = "Silicate core";
            } else {
                if (Dice.d6() < 4) {
                    tempTectonics = "Silicates core";
                } else {
                    tempTectonics = "Silicates, small metal core";
                }
            }
        }
        return tempTectonics;
    }
}
