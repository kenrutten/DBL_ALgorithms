package main;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.*;
import java.io.*;
import java.util.*;
import main.Solver;



/**
 *
 * @author Ewout
 */
public class Solver4Pos extends Solver {

    // The estimated max height that a label will take.
    final float MAX_HEIGHT = 100.0f;
        
    @Override
    public Label2Pos[] solve(Point2D[] inp, float aspectRatio) {

        // The input points.
        List<Point2D> points = Arrays.asList(inp);
        
        // The best solution found so far.
        Rectangle2D[] bestSolution = new Rectangle2D[points.size()];
        // Holds the current solution.
        Rectangle2D[] labels       = new Rectangle2D[points.size()];

        // Sort the input on the X then Y coordinate both ascending.
        Collections.sort(points, new PointCmp());
        
        // Start with a height of 50.0f use binary search on the height to come to a solution.
        float height = MAX_HEIGHT / 2.0f;
        
        // Looping 7 times will produce a correct solution within 1px.
        final int LOOPCOUNT = 7;
        
        for (int k = 0; k < LOOPCOUNT; k++) {

            float width  = aspectRatio * height;

            int numOfElements  = 0;
            // Gets set to true when there is no solution possible for the current height. 
            boolean impossible = false;
            
            for (int i = 0; i < points.size(); i++) {
                Point2D point = points.get(i);
                // The label is first positioned on the right.
                Rectangle2D pos = new Rectangle2D.Float((float) point.getX(), (float) point.getY(), width, height);

                boolean intersects = false;

                // Compare the label with the already placed labels.
                for (int j = 0; j < numOfElements; j++) {
                    if (pos.intersects(labels[j])) {
                        intersects = true;
                        break;
                    }
                }
                // If it intersects place the label on the left.
                if (intersects) {
                    pos = new Rectangle2D.Float((float) point.getX() - width, (float) point.getY(), width, height);
                    
                    // Compare the label with the points not yet calculated (points to its left).
                    for (int j = i; j < points.size(); j++) {
                        if (pos.contains(points.get(j))) {
                            impossible = true;
                            break;
                        }
                    }
                    // There is no solution for this height, break out of the loop.
                    if (impossible) {
                        break;
                    }
                }

                labels[i] = pos;
                numOfElements++;
            }
            // Use binary search on the height to find a solution.
            if (impossible) {
                height = height + ((MAX_HEIGHT - height) * 0.5f) ;
            } else {
                // Remember the best solution.
                bestSolution = labels.clone();
                height = height + ((MAX_HEIGHT - height) * 1.5f);
            }
        }
        // Convert all Rectangle2D objects to Label2Pos objects.
        Label2Pos[] l = new Label2Pos[points.size()];
        
        for (int i = 0; i < bestSolution.length ; i++) {
            Label2Pos pos = new Label2Pos();
            pos.height    = height;
            pos.point     = points.get(i);
            pos.posType   = (bestSolution[i].getCenterX() > pos.point.getX()) ? Constants.NORTHWEST : Constants.NORTHEAST;
        }

        return l;
    }
}
