package main;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.*;
import java.io.*;
import java.util.*;



/**
 *
 * @author Ewout
 */
public class Solver4Pos extends Solver {

    // The estimated max height that a label will take.
    final float MAX_HEIGHT = 100.0f;
        
    @Override
    public LabelPos[] solve(Point2D[] inputPoints, float aspectRatio, int numOfPoints) {

        // The input points.
        List<Point2D> points = Arrays.asList(inputPoints);
        
        // The best solution found so far.
        Rectangle2D[] bestSolution = new Rectangle2D[points.size()];
        // Holds the current solution.
        Rectangle2D[] labels       = new Rectangle2D[points.size()];

        // Sort the input on the X then Y coordinate both ascending.
        Collections.sort(points, new PointCmp());
        
        // Start with a height of 50.0f use binary search on the height to come to a solution.
        float height = MAX_HEIGHT / 2.0f;
        
        
        // Convert all Rectangle2D objects to LabelPos objects.
        LabelPos[] pos = new LabelPos[points.size()];
        
        for (int i = 0; i < bestSolution.length ; i++) {
            pos[i].height    = height;
            pos[i].point     = points.get(i);
            pos[i].posType   = "NE, NW, SE, SW";//Has to be dealth with;
        }

        return pos;
    }
}
