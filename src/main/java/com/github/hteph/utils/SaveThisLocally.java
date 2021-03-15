package com.github.hteph.utils;

import com.github.hteph.generators.StarFactory;
import com.github.hteph.generators.StarSystemGenerator;
import com.github.hteph.repository.objects.Star;
import com.github.hteph.repository.objects.StellarObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


import java.io.*;
import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaveThisLocally {

    public static void saveThis(ArrayList<ArrayList<StellarObject>> testSystem) {

        try (FileOutputStream fos = new FileOutputStream("myGalaxy.ser")){
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(testSystem);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<ArrayList<StellarObject>> restoreSaved() {

        ArrayList<ArrayList<StellarObject>> result = null;

        try (FileInputStream fis = new FileInputStream("myGalaxy.ser")){

            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (ArrayList<ArrayList<StellarObject>>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) { //If the file do not exist, create it and fill it with a new generated system.

            StellarObject star = null;
            star = StarFactory.generate("Test", 'A', null);
            ArrayList<StellarObject> systemList = StarSystemGenerator.Generator((Star) star);
            ArrayList<ArrayList<StellarObject>> testSystem = new ArrayList<>();

            testSystem.add(systemList);
            SaveThisLocally.saveThis(testSystem);
            result = testSystem;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
