/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcog.math;

/**
 *
 * @author seh
 */
public class RandomNumber {

    public static int getInt(int min, int max) {
        return min + ((int)Math.round(Math.random() * (max-min)));
    }
}