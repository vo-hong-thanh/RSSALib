/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * Time delays asscociated with a reaction
 * @author Vo Hong Thanh
 * @version 1.0 
*/
public class DelayInfo {
    /** Type of delayed reaction
    */
    private String type;
    
    /** Delayed interval
    */
    private double delayvalue;

    /** Creates Time delays asscociated with a reaction
     */
    public DelayInfo() {
        this.type = DELAY_TYPE.NODELAY;
        this.delayvalue = 0;
    }

    /** Creates Time delays asscociated with a reaction
     * @param _type: delayed type
     * @param _delay delayed interval 
     */
    public DelayInfo(String _type, double _delay) {
        this.type = _type;
        this.delayvalue = _delay;
    }

    /** Creates Time delays asscociated with a reaction
     * 
     * @return the delayed type
     */
    public String getDelayType() {
        return type;
    }

    /** Creates Time delays asscociated with a reaction
     * 
     * @return delayed interval
     */
    public double getDelayTime() {
        return delayvalue;
    }
}
