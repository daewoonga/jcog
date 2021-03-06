/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcog.opencog;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author seh
 */
public class Atom implements Serializable, Comparable<Atom> {
    public final static List<Atom> emptyAtomsList = Collections.unmodifiableList(new LinkedList());

    public static String newIDString() {
        return UUID.randomUUID().toString();
    }
    
    public final UUID uuid;

    public Atom(UUID u) {
        this.uuid = u;
    }
    
    public Atom() {
        this(UUID.randomUUID());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o instanceof Atom) {
            return uuid.equals(((Atom)o).uuid);
        }
        return false;
    }

    @Override
    public int compareTo(Atom t) {
        return uuid.compareTo(t.uuid);
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    public int getDegree() {
        return 0;
    }

    public List<Atom> getNodes() {
        return emptyAtomsList;
    }

    
}
