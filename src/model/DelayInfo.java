/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author vot2
 */
public class DelayInfo {

    private String type;
    private double delayvalue;

    public DelayInfo() {
        this.type = DELAY_TYPE.NODELAY;
        this.delayvalue = 0;
    }

    public DelayInfo(String _type, double _delay) {
        this.type = _type;
        this.delayvalue = _delay;
    }

    public String getDelayType() {
        return type;
    }

    public double getDelayTime() {
        return delayvalue;
    }
}
