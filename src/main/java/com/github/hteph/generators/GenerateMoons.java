package com.github.hteph.generators;

import com.codepoetics.protonpack.StreamUtils;
import com.github.hteph.utils.Dice;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class GenerateMoons {
    static List<String> terrestialMoons(String planetName, String archiveID, BigDecimal distanceToStar, boolean innerZone) {
        int number;
        if (!innerZone) {
            number = (Dice._2d6() - 5) / 2;
        } else {
            number = (int) ((Dice._2d6() - 2) / 1.5);
        }

        if (number < 1) return null;

        double startDistance = 3 + 150 / (1.0 * number) * (Dice._2d6() - 2) / 10.0;
        return StreamUtils.zipWithIndex(DoubleStream
                                                .iterate(startDistance, d -> d + 1 + 150 / (1.0 * number) * (Dice._2d6() - 2) / 10.0)
                                                .limit(number)
                                                .boxed())
                          .map(s -> createMoon(planetName,
                                               archiveID,
                                               distanceToStar,
                                               s.getValue(),
                                               s.getIndex(),
                                               Dice.d6(2) ? 'm' : 'M')) //TODO this could be a bit more involved
                          .collect(Collectors.toList());
    }

    static List<String> jovianMoons(String planetName, String archiveID, BigDecimal distanceToStar, boolean innerZone) {
        int number;

        int innerMoonGroup;
        int majorMoonGroup;
        int outerMoonGroup;

        if (!innerZone) {
            number = (Dice._2d6() - 5) / 2;
        } else {
            number = (int) ((Dice._2d6() - 2) / 1.5);
        }

        if (number < 1) return null;

        double startDistance = 3 + 150 / (1.0 * number) * (Dice._2d6() - 2) / 10.0;
        return StreamUtils.zipWithIndex(DoubleStream
                                                .iterate(startDistance, d -> d + 1 + 150 / (1.0 * number) * (Dice._2d6() - 2) / 10.0)
                                                .limit(number)
                                                .boxed())
                          .map(s -> createMoon(planetName,
                                               archiveID,
                                               distanceToStar,
                                               s.getValue(),
                                               s.getIndex(),
                                               Dice.d6(2) ? 'm' : 'M')) //TODO this could be a bit more involved
                          .collect(Collectors.toList());
    }

    private static String createMoon(String planetName,
                                     String archiveID,
                                     BigDecimal distanceToStar,
                                     double value,
                                     long index,
                                     char size) {

        final int ASCII_STARTING_NUMBER = 97;
        final int ASCII_ENDING_NUMBER = 122;
        char asciiNumber;
        char asciiNumber2;
        String identifier;

        if (ASCII_STARTING_NUMBER + index > ASCII_ENDING_NUMBER) {
            asciiNumber = (char) (ASCII_STARTING_NUMBER + (int) (index / ASCII_STARTING_NUMBER));
            asciiNumber2 = (char) (ASCII_STARTING_NUMBER + (int) (index % ASCII_STARTING_NUMBER));
            identifier = "" + asciiNumber + asciiNumber2;
        } else {
            identifier = "" + (char) (97 + index);
        }

        // TODO here a roche limit check should be made!!
        return GenerateTerrestrialPlanet.generate(archiveID + "." + index,
                                                  planetName + "-" + identifier,
                                                  "A moon",
                                                  "lunar object",
                                                  distanceToStar,
                                                  size,
                                                  archiveID,
                                                  value);
    }
}