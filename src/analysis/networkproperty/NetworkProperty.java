/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.networkproperty;

import utils.ComputingMachine;
import utils.DataWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;

/**
 *
 * @author vot2
 */
public class NetworkProperty {
    //model info
    private StateList states = new StateList();
    private ReactionList reactions = new ReactionList();
    
    //writer
    private DataWriter dataWriter;
    
    public void writeNetworkProperty(String modelFilename, String outputFileName) throws Exception
    {
        //build model
        ComputingMachine.buildModel(modelFilename, states, reactions);
        
        //build reaction dependency graph
        ComputingMachine.buildReactionDependency(reactions);
        
        //build species-reaction dependency graph
        ComputingMachine.buildSpecieReactionDependency(reactions, states);
        
        //writer
        dataWriter = new DataWriter("(Property)" + outputFileName);  
        
        
        //write data
        dataWriter.writeLine("#Species: " + states.getSpeciesList().length);  
        dataWriter.writeLine("Species\t#Affected\tInfo");    
        double averageAffectedReactionBySpecies = 0;
        int maxAffectedReactionBySpecies = Integer.MIN_VALUE;
        int minAffectedReactionBySpecies = Integer.MAX_VALUE;
        
        for (Species s : states.getSpeciesList()) {
            int num = s.getAffectReaction().size();
            
            if(maxAffectedReactionBySpecies <= num){
                maxAffectedReactionBySpecies = num;
            }
            
            if(minAffectedReactionBySpecies >= num){
                minAffectedReactionBySpecies = num;
            }
            
            averageAffectedReactionBySpecies += num;
            
            dataWriter.write(s.getName() + "\t" + num + "\t");   
            
            StringBuilder builder = new StringBuilder();
            for(int reactionIndex : s.getAffectReaction()){
                builder.append(reactionIndex + ", ");                
            }
            int index = builder.lastIndexOf(",");
            builder.delete(index, builder.length());
                    
            dataWriter.writeLine(builder.toString());         
        }
        averageAffectedReactionBySpecies /= states.getSpeciesList().length;
        dataWriter.writeLine("Average reactions affected by species: " + averageAffectedReactionBySpecies);  
        dataWriter.writeLine("Maximum reactions affected by species: " + maxAffectedReactionBySpecies); 
        dataWriter.writeLine("Minimum reactions affected by species: " + minAffectedReactionBySpecies); 
        
        dataWriter.writeLine("----------------------------");    
        dataWriter.writeLine("#Reaction: " + reactions.getReactionList().length);  
        dataWriter.writeLine("Reaction\t#Affected\tInfo");    
        double averageAffectedReactionByReaction = 0;
        int maxAffectedReactionByReaction = Integer.MIN_VALUE;
        int minAffectedReactionByReaction = Integer.MAX_VALUE;
        
        for (Reaction r : reactions.getReactionList()) {
            int num = r.getDependent().size();
            
            if(maxAffectedReactionByReaction <= num){
                maxAffectedReactionByReaction = num;
            }
            
            if(minAffectedReactionByReaction >= num){
                minAffectedReactionByReaction = num;
            }
            
            averageAffectedReactionByReaction += num;
            
            dataWriter.write(r.getReactionIndex() + "\t" + num + "\t");   
            
            StringBuilder builder = new StringBuilder();
            for(int reactionIndex : r.getDependent()){
                builder.append(reactionIndex + ", ");                
            }
            int index = builder.lastIndexOf(",");
            builder.delete(index, builder.length());
                    
            dataWriter.writeLine(builder.toString()); 
        }
        averageAffectedReactionByReaction /= reactions.getReactionList().length;
        dataWriter.writeLine("Average reactions affected by reaction: " + averageAffectedReactionByReaction);  
        dataWriter.writeLine("Maximum reactions affected by reaction: " + maxAffectedReactionByReaction); 
        dataWriter.writeLine("Minimum reactions affected by reactioon : " + minAffectedReactionByReaction);         
    }
    
    public static void main(String[] args) {
        NetworkProperty n = new NetworkProperty();
        try {
            n.writeNetworkProperty("SRG-model.txt", "SRG-model.txt");
        } catch (Exception ex) {
            Logger.getLogger(NetworkProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
