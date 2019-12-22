/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afinitypropogeyion_titova;

import java.util.ArrayList;

/**
 *
 * @author titova_ekaterina
 */
public class Graph {
    public int n; // the number of vertices
    public ArrayList<Edge>[] outEdges; // array of out edges of corresponding vertices
    public ArrayList<Edge>[] inEdges; // array of in edges of corresponding vertices
    public ArrayList<Edge> edges = new ArrayList(); // all edges
    
}
