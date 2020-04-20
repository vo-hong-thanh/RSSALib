/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.delay;

/**
 * DelayedReactionTime: Delayed Reaction Time
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class DelayedReactionTime implements Comparable {
    int reactionIndex;
    double delay;
    String delayType;

    public DelayedReactionTime(int reactionIndex, String delayType, double delay) {
        this.reactionIndex = reactionIndex;
        this.delay = delay;
        this.delayType = delayType;
    }

    public int getDelayReactionIndex() {
        return reactionIndex;
    }

    public String getDelayType() {
        return delayType;
    }

    public double getDelayTime() {
        return delay;
    }

    public void subtractDelayTime(double amount) {
        delay -= amount;
    }
    
    @Override
    public int compareTo(Object t) {
        if (t instanceof DelayedReactionTime) {
            DelayedReactionTime copyObject = (DelayedReactionTime)t;

            if (delay > copyObject.delay) {
                return 1;
            } else if (delay == copyObject.delay) {
                return 0;
            } else {
                return -1;
            }
        }
        return -1;
    }
    
    public String toString()
    {
        return "(Reaction: " + reactionIndex + ", type: " + delayType + ", delay amount: " + delay+ ")";
    }
}
