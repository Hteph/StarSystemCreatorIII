package com.github.hteph.repository.objects;


import com.github.hteph.utils.enums.Breathing;
import com.github.hteph.utils.enums.HydrosphereDescription;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class Planet extends  StellarObject {

    private static final long serialVersionUID = 1L;

    private OrbitalFacts orbitalFacts;
    private BigDecimal mass;
    private int radius;
    private BigDecimal gravity;
    private BigDecimal density;
    private BigDecimal orbitalPeriod;
    private BigDecimal axialTilt;
    private boolean tidelocked;
    private BigDecimal rotationalPeriod;
    private String tectonicCore;
    private BigDecimal magneticField;
   private HydrosphereDescription hydrosphereDescription;
    private int hydrosphere;
   private Set<AtmosphericGases> atmosphericComposition;
    private BigDecimal atmoPressure;
    private int surfaceTemp;
    private int[] rangeBandTemperature;
    private int[] rangeBandTempSummer;
    private int[] rangeBandTempWinter;
    private BigDecimal nightTempMod;
    private BigDecimal dayTempMod;
    private String tectonicActivityGroup;
    private BigDecimal orbitalInclination;
    private boolean boilingAtmo;
    private List<String> moonList;
    private BigDecimal lunarTidal;
    private boolean planetLocked;
    private BigDecimal lunarOrbitalPeriod;
    private BigDecimal lunarOrbitDistance; //in planetRadii
    private String classificationName;
   private Breathing lifeType;

    public String getAtmosphericCompositionParsed() {

        return atmosphericComposition.stream().map(AtmosphericGases::toString).collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return "Planet " + super.getName() + ": " + super.getDescription() + ", radius=" + radius
                       + ", hydrosphereDescription=" + "something"  +", hydro%=" + hydrosphere + ", pressure="
                       + atmoPressure + ",\n surfaceTemp=" + surfaceTemp + ", lifeType="  + "\n Atmo" + "Atmo";
    }

}