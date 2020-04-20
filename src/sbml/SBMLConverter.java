/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbml;

import java.io.File;
import java.util.List;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import utils.DataWriter;

/**
 * SBMLConverter: Converting SBML to reaction format supported by RSSALib
 * @author Vo Hong Thanh
 * @version 1.0
*/

public class SBMLConverter {
    /** Convert SBML file .
        * @param sbmlFile The input sbml file.
        * @return A reaction file with the same name with the sbml file
    */       
    public static String convert(String sbmlFile) throws Exception {
        //create converted file
        String outputFileName = sbmlFile.substring(0, sbmlFile.lastIndexOf(".")) + ".txt";
        File outputFile = new File(outputFileName);
        
//        if (!outputFile.exists()) {
            //reader for reading sbml
            SBMLReader reader;
            reader = new SBMLReader();
            SBMLDocument sbmlDoc;
            Model model;

            sbmlDoc = reader.readSBML(sbmlFile);
            
            StringBuilder errorMess = new StringBuilder();
                    
            int errors = sbmlDoc.checkConsistency();
            boolean isSeverity = false;
            
            if (errors > 0) {
                for (int e = 0; e < sbmlDoc.getNumErrors(); e++) {
                    SBMLError err = sbmlDoc.getError(e);
                    
                    if(err.isError() || err.isFatal() || err.isSystem() || err.isInternal())
                    {
                        isSeverity |= true;
                    
                        errorMess.append("Line " + err.getLine() + ": " + err.getMessage());  
                        errorMess.append(System.getProperty("line.separator"));
                    }
                    else{                    
                        System.out.println("(" + err.getSeverity() + ") Line " + err.getLine() + "(" + err.getSeverity()+ "): " +  sbmlDoc.getError(e).getMessage()); 
                    }
                }                
            }
            
            if(isSeverity){
                throw new RuntimeException(errorMess.toString());
            } 
            else {
                //writer for writing converted file
                DataWriter writer = new DataWriter(outputFile);
                
                writer.writeLine("#####");
                writer.writeLine("##### Conversion of SBML model " + sbmlFile);
                                
                writer.writeLine("##### SBML level: " + sbmlDoc.getLevel());
                
                model = sbmlDoc.getModel();
                writer.writeLine("##### Model name: " + model.getName());
                writer.writeLine("#####");
                //model.getParameterCount();
                int compartmentCount = model.getCompartmentCount();
                if(compartmentCount > 1)
                {
                    errorMess.append("Error - RSSALib hasn't supported multicomparment!");
                    throw new RuntimeException(errorMess.toString());
                }
                
                writer.writeLine("");
                writer.writeLine("##### Comparment: " + model.getCompartment(0).getId());
                
                writer.writeLine("");
                writer.writeLine("##### Parameters");
                int parameterCount = model.getParameterCount();//.getListOfParameters();
                for (int p = 0; p < parameterCount; p++) {
                    Parameter para = model.getParameter(p);
                    writer.writeLine(para.getId() + " = " + para.getValue());
                }

                writer.writeLine("");
                writer.writeLine("##### Species");
                int speciesCount = model.getSpeciesCount();                
                for (int s = 0; s < speciesCount; s++) {
                    Species species = model.getSpecies(s);

                    writer.writeLine( species.getId() + " = " + (int)(species.getInitialAmount()));
                }

                writer.writeLine("");
                writer.writeLine("##### Reactions");
                int reactionCount = model.getReactionCount();
                for (int r = 0; r < reactionCount; r++) {
                    Reaction reaction = model.getReaction(r);

                    int reactantCount = reaction.getReactantCount();
                    if (reactantCount == 0){
                        writer.write("_ ");
                    }           
                    else //if (reactantCount > 0) 
                    {
                        for (int reactantIndex = 0; reactantIndex < reactantCount; reactantIndex++) {
                            SpeciesReference sr = reaction.getReactant(reactantIndex);
                            int stoich = (int)(sr.getStoichiometry());
                            writer.write( (stoich > 1 ? stoich : "") + sr.getSpecies() + " ");
                        }
                    }

                    writer.write("-> ");

                    int productCount = reaction.getProductCount();
                    if (productCount == 0){
                        writer.write("_ ");
                    }                        
                    else //if(productCount > 0) 
                    {
                        for (int productIndex = 0; productIndex < productCount; productIndex++) {
                            SpeciesReference sr = reaction.getProduct(productIndex);
                            int stoich = (int)(sr.getStoichiometry());
                            writer.write( (stoich > 1 ? stoich : "") + sr.getSpecies() + " ");
                        }
                    }
                    
                    KineticLaw ratelaw = reaction.getKineticLaw();
//                    System.out.println("Parsing rate " + ratelaw.getFormula());
                    
                    ASTNode ast = ratelaw.getMath();    
                    //System.out.println("Type of AST tree of rate law " + ast.getType());
                    
                    if(ast.getType().equals(ASTNode.Type.INTEGER) || ast.getType().equals(ASTNode.Type.REAL))
                    {
                        writer.write(", " + ast.toFormula());
                        writer.writeLine();
                    }
                    else if(ast.getType().equals(ASTNode.Type.NAME))
                    {
                        writer.write(", " + ast.getName());
                        writer.writeLine();
                    }
                    else if(ast.getType().equals(ASTNode.Type.TIMES))
                    {        
                        List<ASTNode> children = ast.getChildren();
                        for(ASTNode child : children)
                        {
//                            System.out.println("child " + child.getName() + ", type " + child.getType());
                            boolean isSpecies = false;
                            for (int s = 0; s < speciesCount; s++) {
                                Species species = model.getSpecies(s);
//                                System.out.println("consider species " + species.getId());    
                                if(species.getId().equals(child.getName())){
//                                    System.out.println("is species");
                                    isSpecies = true;
                                    break;
                                }
                            }
                            
                            if(!isSpecies)
                            {
//                                System.out.println("not a species");
                                writer.write(", " + child.getName());
                                break;
                            }
                        }
                        writer.writeLine();
                    }
                    else if(ast.getType().equals(ASTNode.Type.DIVIDE))
                    {                         
                        List<ASTNode> children = ast.getChildren();                        
                        here: 
                        for(ASTNode child : children)
                        {
//                            System.out.println("child " + child);
                            if(child.getType().equals(ASTNode.Type.TIMES))
                            {
                                List<ASTNode> subchildren = child.getChildren();                                
                                
                                for(ASTNode subchild : subchildren)
                                {
//                                    System.out.println("subchild " + subchild);
                                    if(!subchild.isOperator() && !subchild.isNumber() )
                                    {
            //                            System.out.println("child " + child.getName() + ", type " + child.getType());
                                        boolean isSpecies = false;
                                        for (int s = 0; s < speciesCount; s++) {
                                            Species species = model.getSpecies(s);

                                            if(species.getId().equals(subchild.getName())){
                                                isSpecies = true;
                                                break;    
                                            }
                                        }
                                        
                                        if(!isSpecies)
                                        {
                                            writer.write(", " + subchild.getName());
                                            break here;
                                        }
                                    }
                                }
                            }
                            
                        }
                        writer.writeLine();
                    }                    
                    else
                    {
                        errorMess.append("Error - RSSALib converts only Mass-Action kinetics!");
                        throw new RuntimeException(errorMess.toString());
                    }
                }
                writer.flush();
                writer.close();
            }
//        }
        
        return outputFileName;
    }

}
