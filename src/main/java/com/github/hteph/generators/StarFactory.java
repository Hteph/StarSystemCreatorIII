package com.github.hteph.generators;

import com.github.hteph.repository.objects.Star;
import com.github.hteph.repository.objects.StellarObject;
import com.github.hteph.tables.StarClassificationTable;
import com.github.hteph.utils.Dice;
import com.github.hteph.utils.NameGenerator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StarFactory {

    public static StellarObject generate(String systemName, char systemPosition, Star star) {

        NameGenerator randomNameGenerator = null;
        try {
            randomNameGenerator = new NameGenerator("RomanNames");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.info("Name generation failure", e);
        }

        double mass = generateMass();
        //TODO This is for type V stars, should be expanded to handle other types, if the mass is too low to generate a decent temperture,
        // make a unique stellar object instead, a white dwarf or something
        int temperature = 100 * (int) (((500 + 4800 * Math.pow(mass, 0.5)) * (0.8 + Math.random() * 0.4)) / 100);
        if (temperature < 100 || temperature > 55000) {
            //generate something unique instead
        }
        double diameter = Math.pow(mass, 2 / 3.0);//Solar relative units
        double lumosity = Math.pow(mass, 3.5); //Solar relative units
        String starClass = StarClassificationTable.findStarClass(temperature);
        double maxAge = 10 * Math.pow(1 / mass, 2.5);
        double age = (0.3 + Math.random() * 0.6) * Math.min(maxAge, 13);// in billion of earth years
        double halfAgeBalance = 2 * age / maxAge;
        lumosity *= Math.pow(halfAgeBalance, 0.5);
        diameter *= Math.pow(halfAgeBalance, 1 / 3.0);

        //TODO abundance should be done nicer!
        int abundance = generateAbundance(age);

        String description = " A star of " + starClass + " type";

        //TODO allow for multiple Starsystems, ie archiveID not hardcoded

        String starName;
        try {
            starName = randomNameGenerator.compose((2 + Dice.d6() / 2));
        } catch (Exception e) {
            starName = "Unknown";
        }

        return Star.builder()
                   .archiveID(systemName + " " + systemPosition)
                   .name(starName)
                   .description(description)
                   .lumosity(BigDecimal.valueOf(lumosity))
                   .mass(BigDecimal.valueOf(mass))
                   .diameter(BigDecimal.valueOf(diameter))
                   .classification(starClass)
                   .abundance(abundance)
                   .build();
    }

    private static int generateAbundance(double age) {
        int abundance;
        int[] abundanceArray = new int[]{0, 10, 13, 19, 22};
        int retVal = Arrays.binarySearch(abundanceArray, (int) (Dice._2d6() + age));
        if (retVal < 0) abundance = 2 - retVal + 1;
        else abundance = 2 - retVal;
        return abundance;
    }

    private static double generateMass() {
        //old code
//        double mass;
//        int testDice =Dice._3d6()-3;
//        double randN =testDice/(15.0+Math.random()/10); //turning the dice roll into a continous sligthly skewed randomnumber.
//        mass = 0.045/(0.001+Math.pow(randN,5)); // <-----------------------------------------MOST IMPORTANT STARTING POINT
        return Dice._3d6() * Math.random() * Math.random() * Math.random();
    }
}

