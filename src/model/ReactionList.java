/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author Hong Thanh
 */
public class ReactionList {
    private Hashtable<Integer, Reaction> reactionCollection ;
    
    public ReactionList()
    {
        reactionCollection = new Hashtable<Integer, Reaction>();
    }

    public void addReaction(Reaction r)
    {
        reactionCollection.put(r.getReactionIndex(), r);
    }

    public int getLength() {
        return reactionCollection.size();
    }

    public Reaction getReaction(int index)
    {
        return reactionCollection.get(index);
    }
    
    public Reaction[] getReactionList() {
        Reaction[] list = new Reaction[reactionCollection.size()];
        int index = 0;
        for (Enumeration<Integer> s = reactionCollection.keys(); s.hasMoreElements();) {
            list[index++] = reactionCollection.get(s.nextElement());
        }
        return list;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Enumeration<Integer> s = reactionCollection.keys(); s.hasMoreElements();) {
            int retrieveIndex = s.nextElement();
            result.append("\n" + reactionCollection.get(retrieveIndex).toString());
        }
        return result.toString();
    }
    
    public String toStringFull(){
        StringBuilder result = new StringBuilder();
        for (Enumeration<Integer> indexList = reactionCollection.keys(); indexList.hasMoreElements();) {
            int reactionIndex = indexList.nextElement();
            Reaction r = reactionCollection.get(reactionIndex);
            
            result.append("\n").append(r);
        
            HashSet<Integer> dependent = r.getDependent();
            if(!dependent.isEmpty()){            
                result.append("\n affected reaction: " );
                for(int dependencyReaction : dependent)
                {
                    result.append(dependencyReaction).append(", ");
                }
                result.delete(result.length() - 2, result.length());
            }
        }
        
        return result.toString();
    }
}
