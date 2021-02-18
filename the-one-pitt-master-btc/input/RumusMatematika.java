/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

/**
 *
 * @author WINDOWS_X
 */
public class RumusMatematika {

    public RumusMatematika() {
    }
    
    public double bagi(double a, double b){
        return a/b;
    }
    
    public double kali(double a, double b){
        return a*b;
    }
    
    public double bulat(double a, int b){
        double bulat = Math.round(a*Math.pow(10, b))/Math.pow(10, b);
        return bulat;
    }
}
