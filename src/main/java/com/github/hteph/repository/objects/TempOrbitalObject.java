package com.github.hteph.repository.objects;

public class TempOrbitalObject {

        private double orbitDistance;
        private char orbitObject;

        public TempOrbitalObject(double orbitDistance) {
            this.orbitDistance = orbitDistance;
            this.orbitObject = '-';
        }

    public double getOrbitDistance() {
        return orbitDistance;
    }

    public char getOrbitObject() {
        return orbitObject;
    }

    public void setOrbitObject(char orbitObject) {
        this.orbitObject = orbitObject;
    }


}
