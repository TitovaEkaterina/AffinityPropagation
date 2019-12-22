/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afinitypropogeyion_titova;

/**
 *
 * @author titova_ekaterina
 */
public class Statistic {
    private double mean;
    private double sigma;
    
    public void setMean(double mean){
        this.mean = mean;
    }
    
    public void setSigma(double sigma){
        this.sigma = sigma;
    }
    
    public double getSigma(){
        return this.sigma;
    }
    
    public double getMean(){
        return this.mean;
    }
}
