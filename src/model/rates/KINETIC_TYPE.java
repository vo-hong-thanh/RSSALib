/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.rates;

/**
 * KINETIC_TYPE: types of kinetic rare law
 * @author Vo Hong Thanh
 * @version 1.0
*/
public interface KINETIC_TYPE {    
    public static String MASS_ACTION = "MASS";
    public static String MICHAELIS_MENTEN = "MM";    
    public static String HILL = "HILL";   
    public static String INHIBITORYHILL = "INHIBITORYHILL";
}