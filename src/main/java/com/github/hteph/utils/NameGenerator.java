package com.github.hteph.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is released under GNU general public license
 * <p>
 * Description: This class generates random names from syllables, and provides programmer a
 * simple way to set a group of rules for generator to avoid unpronounceable and bizarre names.
 * <p>
 * SYLLABLE FILE REQUIREMENTS/FORMAT:
 * 1) all syllables are separated by line break.
 * 2) Syllable should not contain or start with whitespace, as this character is ignored and only first part of the syllable is read.
 * 3) + and - characters are used to set rules, and using them in other way, may result in unpredictable results.
 * 4) Empty lines are ignored.
 * <p>
 * SYLLABLE CLASSIFICATION:
 * Name is usually composed from 3 different class of syllables, which include prefix, middle part and suffix.
 * To declare syllable as a prefix in the file, insert "-" as a first character of the line.
 * To declare syllable as a suffix in the file, insert "+" as a first character of the line.
 * everything else is read as a middle part.
 * <p>
 * NUMBER OF SYLLABLES:
 * Names may have any positive number of syllables. In case of 2 syllables, name will be composed from prefix and suffix.
 * In case of 1 syllable, name will be chosen from amongst the prefixes.
 * In case of 3 and more syllables, name will begin with prefix, is filled with middle parts and ended with suffix.
 * <p>
 * ASSIGNING RULES:
 * I included a way to set 4 kind of rules for every syllable. To add rules to the syllables, write them right after the
 * syllable and SEPARATE WITH WHITESPACE. (example: "aad +v -c"). The order of rules is not important.
 * <p>
 * RULES:
 * 1) +v means that next syllable must definitely start with a Vowel.
 * 2) +c means that next syllable must definitely start with a consonant.
 * 3) -v means that this syllable can only be added to another syllable, that ends with a Vowel.
 * 4) -c means that this syllable can only be added to another syllable, that ends with a consonant.
 * So, our example: "aad +v -c" means that "aad" can only be after consonant and next syllable must start with Vowel.
 * Beware of creating logical mistakes, like providing only syllables ending with consonants, but expecting only Vowels, which will be detected
 * and RuntimeException will be thrown.
 * <p>
 * TO START:
 * Create a new NameGenerator object, provide the syllable file, and create names using compose() method.
 */
public class NameGenerator {
    private ArrayList<String> prefixList = new ArrayList<>();
    private ArrayList<String> middlePartList = new ArrayList<>();
    private ArrayList<String> suffixList = new ArrayList<>();

    final private static char[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'y'};
    final private static char[] CONSONANTS = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y'};

    private String fileName;

    /**
     * Create new random name generator object. refresh() is automatically called.
     *
     * @param fileName insert file name, where syllables are located
     * @throws IOException
     */
    public NameGenerator(String fileName) throws IOException {
        this.fileName = fileName;
        refresh();
    }

    /**
     * Change the file. refresh() is automatically called during the process.
     *
     * @param fileName insert the file name, where syllables are located.
     * @throws IOException
     */
    public void changeFile(String fileName) throws IOException {
        if (fileName == null) throw new IOException("File name cannot be null");
        this.fileName = fileName;
        refresh();
    }

    /**
     * Refresh names from file. No need to call that method, if you are not changing the file during the operation of program, as this method
     * is called every time file name is changed or new NameGenerator object created.
     *
     * @throws IOException
     */
    private void refresh() throws IOException {

        FileReader input = new FileReader(fileName);
        BufferedReader bufRead;
        String line = "";
        bufRead = new BufferedReader(input);

        while (line != null) {
            line = bufRead.readLine();
            if (line != null && !line.equals("")) {
                if (line.charAt(0) == '-') {
                    prefixList.add(line.substring(1).toLowerCase());
                } else if (line.charAt(0) == '+') {
                    suffixList.add(line.substring(1).toLowerCase());
                } else {
                    middlePartList.add(line.toLowerCase());
                }
            }
        }
        bufRead.close();
    }


    /**
     * Compose a new name.
     *
     * @param numberOfSyllables The number of syllables used in name.
     * @return Returns composed name as a String
     * @throws RuntimeException when logical mistakes are detected inside chosen file, and program is unable to complete the name.
     */
    public String compose(int numberOfSyllables) {

        int syls = checkSyllableLibrary(numberOfSyllables);

        //int expecting = 0; // 1 for Vowel, 2 for consonant
        //int last = 0; // 1 for Vowel, 2 for consonant
        String name;

        SYLLABELTYPE expecting= SYLLABELTYPE.NONE;
        SYLLABELTYPE last;

        String randomPrefix = prefixList.get((int) (Math.random() * prefixList.size()));

        if (VowelLast(pureSyl(randomPrefix))) last = SYLLABELTYPE.VOWEL;
        else last = SYLLABELTYPE.CONSONANT;

        if (syls > 2) {
            if (expectsVowel(randomPrefix)) {
                expecting = SYLLABELTYPE.VOWEL;
                if (!containsVocFirst(middlePartList)) throw new RuntimeException("Expecting \"middle\" part starting with Vowel, " +
                                                                                          "but there is none. You should add one, or remove requirement for one.. ");
            }
            if (expectsConsonant(randomPrefix)) {
                expecting = SYLLABELTYPE.CONSONANT;
                if (!containsConsFirst(middlePartList)) throw new RuntimeException("Expecting \"middle\" part starting with consonant, " +
                                                                                           "but there is none. You should add one, or remove requirement for one.. ");
            }
        } else {
            if (expectsVowel(randomPrefix)) {
                expecting = SYLLABELTYPE.VOWEL;
                if (!containsVocFirst(suffixList)) throw new RuntimeException("Expecting \"suffix\" part starting with Vowel, " +
                                                                                      "but there is none. You should add one, or remove requirement for one.. ");
            }
            if (expectsConsonant(randomPrefix)) {
                expecting = SYLLABELTYPE.CONSONANT;
                if (!containsConsFirst(suffixList)) throw new RuntimeException("Expecting \"suffix\" part starting with consonant, " +
                                                                                       "but there is none. You should add one, or remove requirement for one.. ");
            }
        }
        if (VowelLast(pureSyl(randomPrefix)) && !allowVocs(middlePartList))
            throw new RuntimeException("Expecting \"middle\" part that allows last character of prefix to be a Vowel, " +
                                               "but there is none. You should add one, or remove requirements that cannot be fulfilled.. the prefix used, was : \""
                                               + randomPrefix + "\", which" +
                                               "means there should be a part available, that has \"-v\" requirement or no requirements for previous syllables at all.");

        if (consonantLast(pureSyl(randomPrefix)) && !allowCons(middlePartList))
            throw new RuntimeException("Expecting \"middle\" part that allows last character of prefix to be a consonant, " +
                                               "but there is none. You should add one, or remove requirements that cannot be fulfilled.. the prefix used, was : \""
                                               + randomPrefix + "\", which" +
                                               "means there should be a part available, that has \"-c\" requirement or no requirements for previous syllables at all.");

        int[] middleSyllabelsArray = new int[syls-2];
        for (int i = 0; i < middleSyllabelsArray.length; i++) {

            do {
                middleSyllabelsArray[i] = (int) (Math.random() * middlePartList.size());
                //System.out.println("exp " +expecting+" VowelF:"+VowelFirst(middlePartList.get(b[i]))+" syl: "+middlePartList.get(b[i]));
            } while (expecting.equals(SYLLABELTYPE.VOWEL) && !VowelFirst(pureSyl(middlePartList.get(middleSyllabelsArray[i])))
                             || expecting.equals(SYLLABELTYPE.CONSONANT) && !consonantFirst(pureSyl(middlePartList.get(middleSyllabelsArray[i])))
                             || last.equals(SYLLABELTYPE.VOWEL) && hatesPreviousVowels(middlePartList.get(middleSyllabelsArray[i]))
                             || last.equals(SYLLABELTYPE.CONSONANT) && hatesPreviousConsonants(middlePartList.get(middleSyllabelsArray[i])));

            expecting = SYLLABELTYPE.NONE;
            if (expectsVowel(middlePartList.get(middleSyllabelsArray[i]))) {
                expecting = SYLLABELTYPE.VOWEL;
                if (i < middleSyllabelsArray.length - 1 && !containsVocFirst(middlePartList))
                    throw new RuntimeException("Expecting \"middle\" part starting with Vowel, " +
                                                       "but there is none. You should add one, or remove requirement for one.. ");
                if (i == middleSyllabelsArray.length - 1 && !containsVocFirst(suffixList))
                    throw new RuntimeException("Expecting \"suffix\" part starting with Vowel, " +
                                                       "but there is none. You should add one, or remove requirement for one.. ");
            }
            if (expectsConsonant(middlePartList.get(middleSyllabelsArray[i]))) {
                expecting = SYLLABELTYPE.CONSONANT;
                if (i < middleSyllabelsArray.length - 1 && !containsConsFirst(middlePartList))
                    throw new RuntimeException("Expecting \"middle\" part starting with consonant, " +
                                                       "but there is none. You should add one, or remove requirement for one.. ");
                if (i == middleSyllabelsArray.length - 1 && !containsConsFirst(suffixList))
                    throw new RuntimeException("Expecting \"suffix\" part starting with consonant, " +
                                                       "but there is none. You should add one, or remove requirement for one.. ");
            }
            if (VowelLast(pureSyl(middlePartList.get(middleSyllabelsArray[i])))
                        && !allowVocs(middlePartList)
                        && syls > 3)
                throw new RuntimeException("Expecting \"middle\" part that allows last character of last syllable to be a Vowel, " +
                                                   "but there is none. You should add one, or remove requirements that cannot be fulfilled.. the part used, was : \""
                                                   + middlePartList
                                                             .get(middleSyllabelsArray[i]) + "\", which " +
                                                   "means there should be a part available, that has \"-v\" requirement or no requirements for previous syllables at all.");

            if (consonantLast(pureSyl(middlePartList.get(middleSyllabelsArray[i])))
                        && !allowCons(middlePartList)
                        && syls > 3)
                throw new RuntimeException("Expecting \"middle\" part that allows last character of last syllable to be a consonant, " +
                                                   "but there is none. You should add one, or remove requirements that cannot be fulfilled.. the part used, was : \""
                                                   + middlePartList
                                                             .get(middleSyllabelsArray[i]) + "\", which " +
                                                   "means there should be a part available, that has \"-c\" requirement or no requirements for previous syllables at all.");
            if (i == middleSyllabelsArray.length - 3) {
                if (VowelLast(pureSyl(middlePartList.get(middleSyllabelsArray[i]))) && !allowVocs(suffixList))
                    throw new RuntimeException("Expecting \"suffix\" part that allows last character of last syllable to be a Vowel, " +
                                                       "but there is none. You should add one, or remove requirements that cannot be fulfilled.. the part used, was : \""
                                                       + middlePartList
                                                                 .get(middleSyllabelsArray[i]) + "\", which " +
                                                       "means there should be a suffix available, that has \"-v\" requirement or no requirements for previous syllables at all.");

                if (consonantLast(pureSyl(middlePartList.get(middleSyllabelsArray[i]))) && !allowCons(suffixList))
                    throw new RuntimeException("Expecting \"suffix\" part that allows last character of last syllable to be a consonant, " +
                                                       "but there is none. You should add one, or remove requirements that cannot be fulfilled.. the part used, was : \""
                                                       + middlePartList
                                                                 .get(middleSyllabelsArray[i]) + "\", which " +
                                                       "means there should be a suffix available, that has \"-c\" requirement or no requirements for previous syllables at all.");
            }
            if (VowelLast(pureSyl(middlePartList.get(middleSyllabelsArray[i])))) last = SYLLABELTYPE.VOWEL;
            else last = SYLLABELTYPE.CONSONANT;
        }

        String randomSuffix;
        do {
            randomSuffix = suffixList.get((int) (Math.random() * suffixList.size()));
        } while (expecting.equals(SYLLABELTYPE.VOWEL) && !VowelFirst(pureSyl(randomSuffix))
                         || (expecting.equals(SYLLABELTYPE.CONSONANT) && !consonantFirst(pureSyl(randomSuffix)))
                         || (last.equals(SYLLABELTYPE.VOWEL) && hatesPreviousVowels(randomSuffix))
                         || (last.equals(SYLLABELTYPE.CONSONANT) && hatesPreviousConsonants(randomSuffix)));

        name = upper(pureSyl(randomPrefix.toLowerCase()));
        for (int value : middleSyllabelsArray) name = name.concat(pureSyl(middlePartList.get(value).toLowerCase()));
        if (syls > 1) name = name.concat(pureSyl(randomSuffix.toLowerCase()));
        return name;
    }

    private int checkSyllableLibrary(int syls) {
        if (syls < 1) syls = 1;
        if (syls > 2 && middlePartList.isEmpty())
            throw new RuntimeException("You are trying to create a name with more than 3 parts, which requires middle parts, " +
                                               "which you have none in the file " + fileName
                                               + ". You should add some. Every word, which doesn't have + or - for a prefix is counted as a middle part.");
        if (prefixList.isEmpty())
            throw new RuntimeException("You have no prefixes to start creating a name. add some and use \"-\" prefix, to identify it as a prefix for a name. (example: -asd)");
        if (suffixList.isEmpty())
            throw new RuntimeException("You have no suffixes to end a name. add some and use \"+\" prefix, to identify it as a suffix for a name. (example: +asd)");
        return syls;
    }

    private String upper(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    private boolean containsConsFirst(ArrayList<String> array) {
        for (String s : array) {
            if (consonantFirst(s)) return true;
        }
        return false;
    }

    private boolean containsVocFirst(ArrayList<String> array) {
        for (String s : array) {
            if (VowelFirst(s)) return true;
        }
        return false;
    }

    private boolean allowCons(ArrayList<String> array) {
        for (String s : array) {
            if (hatesPreviousVowels(s) || !hatesPreviousConsonants(s)) return true;
        }
        return false;
    }

    private boolean allowVocs(ArrayList<String> array) {
        for (String s : array) {
            if (hatesPreviousConsonants(s) || !hatesPreviousVowels(s)) return true;
        }
        return false;
    }

    private boolean expectsVowel(String s) {
        return s.substring(1).contains("+v");
    }

    private boolean expectsConsonant(String s) {
        return s.substring(1).contains("+c");
    }

    private boolean hatesPreviousVowels(String s) {
        return s.substring(1).contains("-c");
    }

    private boolean hatesPreviousConsonants(String s) {
        return s.substring(1).contains("-v");
    }

    private String pureSyl(String s) {
        return s.trim().replace("+","").replace("-","");
    }

    private boolean VowelFirst(String s) {
        return (String.copyValueOf(VOWELS).contains(String.valueOf(s.charAt(0)).toLowerCase()));
    }

    private boolean consonantFirst(String s) {
        return (String.copyValueOf(CONSONANTS).contains(String.valueOf(s.charAt(0)).toLowerCase()));
    }

    private boolean VowelLast(String s) {
        return (String.copyValueOf(VOWELS).contains(String.valueOf(s.charAt(s.length() - 1)).toLowerCase()));
    }

    private boolean consonantLast(String s) {
        return (String.copyValueOf(CONSONANTS).contains(String.valueOf(s.charAt(s.length() - 1)).toLowerCase()));
    }

    enum SYLLABELTYPE {NONE,VOWEL, CONSONANT}

}
