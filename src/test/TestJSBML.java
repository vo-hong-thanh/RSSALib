/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author vot2
 */
public class TestJSBML {

    public static void main(String[] args) throws Exception {
        System.out.println("[Working directory: " + (new File(".")).getCanonicalPath() + "]");

        String sbmlFile = "00001-sbml-l3v1.xml";
        SBMLReader reader;
        reader = new SBMLReader();
        SBMLDocument sbmlDoc;
        Model model;

        System.out.println("Reading sbml file " + sbmlFile);

        sbmlDoc = reader.readSBML(sbmlFile);
        System.out.println("SBML level: " + sbmlDoc.getLevel());

        int errors = sbmlDoc.checkConsistency();
        if (errors > 0) {
            System.out.println("There are " + sbmlDoc.getNumErrors() + " errors");
            for (int e = 0; e < sbmlDoc.getNumErrors(); e++) {
                SBMLError err = sbmlDoc.getError(e);
                err.getCategory();

                System.out.println(sbmlDoc.getError(e).getMessage());
            }
        } else {
            model = sbmlDoc.getModel();
            System.out.println("Model name : " + model.getName());

            //model.getParameterCount();
            int compartmentCount = model.getCompartmentCount();
            System.out.println("#comparment : " + compartmentCount);

            for (int c = 0; c < compartmentCount; c++) {
                Compartment compartment = model.getCompartment(c);

                System.out.println(" Compartment " + c + ": " + compartment.getId());
            }

            int reactionCount = model.getReactionCount();
            System.out.println("#reaction : " + reactionCount);
            for (int r = 0; r < reactionCount; r++) {
                Reaction reaction = model.getReaction(r);

                System.out.print(" Reaction " + reaction.getId() + ": ");

                int reactantCount = reaction.getReactantCount();
                if (reactantCount > 0) {
                    for (int reactantIndex = 0; reactantIndex < reactantCount; reactantIndex++) {
                        SpeciesReference sr = reaction.getReactant(reactantIndex);
                        System.out.print(sr.getStoichiometry() + "*" + sr.getSpecies() + " ");
                    }
                }

                System.out.print("-> ");

                int productCount = reaction.getProductCount();
                if (productCount > 0) {
                    for (int productIndex = 0; productIndex < productCount; productIndex++) {
                        SpeciesReference sr = reaction.getProduct(productIndex);
                        System.out.print(sr.getStoichiometry() + "*" + sr.getSpecies() + " ");
                    }
                }
                System.out.println();

                KineticLaw ratelaw = reaction.getKineticLaw();
                //atelaw.getFormula();
                System.out.println(" rate " + ratelaw.getFormula());
                
                int children = ratelaw.getChildCount();
                System.out.println(" child counts " + children);
                for(int ch = 0; ch < children; ch++)
                {
                    System.out.println(" + child " + ch + ": " + ratelaw.getChildAt(ch).toString());
                }
                
                        
            }

            int speciesCount = model.getSpeciesCount();
            System.out.println("#species : " + speciesCount);
            for (int s = 0; s < speciesCount; s++) {
                Species species = model.getSpecies(s);

                System.out.println(" Species " + s + ": " + species.getId() + ", inital amount " + species.getInitialAmount());
            }
        }
    }
}
