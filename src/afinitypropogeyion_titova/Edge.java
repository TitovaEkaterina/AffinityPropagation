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
public class Edge {
    
    public int source;
    public int destination;
    public double similarity;
    public double responsibility;
    public double availability;

    public Edge(int source, int destination, double similarity) {
        this.source = source;
        this.destination = destination;
        this.similarity = similarity;
        this.availability = 0;
        this.responsibility = 0;
    }

    public boolean less(Edge rhs) {
        return similarity < rhs.similarity;
    }
    
}
