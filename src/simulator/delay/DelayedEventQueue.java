/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.delay;

import java.util.LinkedList;

/**
 *
 * @author vo
 */
public class DelayedEventQueue {
    private LinkedList<DelayedReactionTime> eventQueue;

    public DelayedEventQueue() {
        eventQueue = new LinkedList<>();
    }

    public void subtractReaminDelayTime(double amount)
    {
        for(DelayedReactionTime d : eventQueue)
        {
            d.subtractDelayTime(amount);
        }
    }
    
    public DelayedReactionTime peekTop() {
        if(eventQueue.isEmpty()){
            return null;
        }
        else{
            return eventQueue.get(0);
        }
    }
    
    public DelayedReactionTime getPosition(int pos)
    {
        return eventQueue.get(pos);
    }

    public void removeTop() {
        eventQueue.remove(0);
    }

    public void add(DelayedReactionTime event) {
        if(eventQueue.isEmpty()){
            eventQueue.add(event);  
        }
        else{
            int i = eventQueue.size() - 1;
            
            if(event.compareTo(eventQueue.get(i)) > 0){
                eventQueue.addLast(event);
            }
            else{
                while(i >= 0 && event.compareTo(eventQueue.get(i)) < 0){
                    i--;
                }
                if(i < 0){
                    eventQueue.addFirst(event);
                }
                else{
                    eventQueue.add(i, event);
                }
            }            
        }        
    }
    
    public int size(){
        return eventQueue.size();
    }

    public void removeAll() {
        eventQueue.clear();
    }
}
