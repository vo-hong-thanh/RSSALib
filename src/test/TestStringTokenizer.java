/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.ArrayList;
import java.util.StringTokenizer;
import model.Term;
import model.kinetics.KINETIC_TYPE;

/**
 *
 * @author vot2
 */
public class TestStringTokenizer {
    public static void main(String[] args) {
        String s = "P -> _ , HILL(Km, n, P), NCD(0)";
        
        //parse reaction
        StringTokenizer rr = new StringTokenizer(s, "->");

        //build reactants
        System.out.println(" -reactants: " + rr.nextToken());

        int commasIndex;

        String restData = rr.nextToken();
        commasIndex = restData.indexOf(',');

        //build products
        System.out.println(" -products: " + restData.substring(0, commasIndex));

        restData = restData.substring(commasIndex + 1).trim();
        
        String rateData = "";
        String delayData = "";
        
        if(restData.startsWith(KINETIC_TYPE.MASS_ACTION) || 
           restData.startsWith(KINETIC_TYPE.MICHAELIS_MENTEN) || 
           restData.startsWith(KINETIC_TYPE.HILL)) {
            int closeParenthesisIndex = restData.indexOf(')');
            rateData = restData.substring(0, closeParenthesisIndex + 1);

            restData = restData.substring(closeParenthesisIndex + 1);

            commasIndex = restData.indexOf(',');
            delayData = restData.substring(commasIndex + 1);
        }
        else{
            commasIndex = restData.indexOf(',');

            if(commasIndex == -1){
                rateData = restData;                        
            }
            else{
                rateData = restData.substring(0, commasIndex);
                delayData = restData.substring(commasIndex + 1);
            }                    
        }

        //build products
        System.out.println(" -rate: " + rateData);

        //build products
        System.out.println(" -delay: " + delayData);
    }    
}
