package com.github.hteph.repository;

import com.github.hteph.repository.objects.ErrorObject;
import com.github.hteph.repository.objects.StellarObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class CentralRegistry {


    private static final Map<String, StellarObject> archive = new HashMap<>();

    public static String putInArchive(StellarObject something){

        if(something.getArchiveID().isEmpty()) something.setArchiveID("Unknown"+ BigDecimal.valueOf(100000*Math.random())
                                                                                           .round(new MathContext(6)));

        //TODO more verification and such stuff are needed

        archive.put(something.getArchiveID(),something);
        return something.getArchiveID();

    }

    public static StellarObject getFromArchive(String archiveID){

        if(archive.containsKey(archiveID)) return archive.get(archiveID);
        else return new ErrorObject("Key not found");
    }


    public static StellarObject getAndRemoveFromArchive(String archiveID) {

        if(archive.containsKey(archiveID)) return archive.remove(archiveID);
        else return new ErrorObject("Key not found");
    }
}
