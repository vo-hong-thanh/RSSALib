/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.StringTokenizer;

/**
 *
 * @author Hong Thanh
 */
public class Test {
    public static void main(String arg[])    
    {
        String dataLine = "M -> _ , MM(Vm, Km)";
        System.out.println("data info: " + dataLine);
        
        StringTokenizer rr = new StringTokenizer(dataLine, "->");
        String reactants = rr.nextToken();
        System.out.println("- reactants: " + reactants);
        
        String restInfo = rr.nextToken();
        int fistCommas = restInfo.indexOf(',');
                
        String products = restInfo.substring(0, fistCommas);
        System.out.println("- products: " + products);
                
        String rateInfo = restInfo.substring(fistCommas + 1);
        System.out.println("- kinetics: " + rateInfo);     
        
        int openParenthesisIndex = rateInfo.indexOf('(');
        int closeParenthesisIndex = rateInfo.indexOf(')');
        
        if(openParenthesisIndex == -1){
            System.out.println(" default: mass-action " + rateInfo);     
            double rateConstant;
            if(isNumeric(rateInfo)){                  
                rateConstant = Double.parseDouble(rateInfo);
                System.out.println("  numeric value " + rateConstant);   
            }
            else{
                System.out.println("  nonnumeric value (lookup)" + rateInfo);   
            }
        }
        else{
            String kineticInfo = rateInfo.substring(0, openParenthesisIndex).trim();
            String rateData = rateInfo.substring(openParenthesisIndex+1, closeParenthesisIndex).trim();
            
            System.out.println("  kinetic info: " + kineticInfo + ", rate data: " + rateData); 
        }   
        
        
        String s = null;
        System.out.println("s = " + s);
    }    
    private static boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }    
}
