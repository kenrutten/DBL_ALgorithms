package main;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.*;
import java.io.*;
import java.util.*;

// Comparator that sorts points on X asc then on Y asc
class PointCmp implements Comparator<Point2D> {

    @Override
    public int compare(Point2D o1, Point2D o2) {
        int r1 = (o1.getX() < o2.getX()) ? -1 : (o1.getX() > o2.getX()) ? 1 : 0;

        if (r1 != 0) {
            return r1;
        }
        int r2 = (o1.getY() < o2.getY()) ? -1 : (o1.getY() > o2.getY()) ? 1 : 0;

        return r2;
    }
}

/**
 *
 * @author Ewout
 */
public class Solver2Pos extends Solver {

    // The estimated max height that a label will take.
    final float MAX_HEIGHT = 100.0f;
        
    @Override
    public LabelPos[] solve(Point2D[] inp, float aspectRatio, int numOfPoints) {

        // The input points.
        List<Point2D> points = Arrays.asList(inp);
        
        // The best solution found so far.
        Rectangle2D[] bestSolution = new Rectangle2D[numOfPoints];
        // Holds the current solution.
        Rectangle2D[] labels       = new Rectangle2D[numOfPoints];

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
            
            for (int i = 0; i < numOfPoints; i++) {
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
                    for (int j = i; j < numOfPoints; j++) {
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
        // Convert all Rectangle2D objects to LabelPos objects.
        LabelPos[] l = new LabelPos[numOfPoints];
        LabelPos pos = new LabelPos();
        pos.height    = height;
        
        for (int i = 0; i < numOfPoints ; i++) {
            pos.point     = points.get(i);
            pos.posType   = (bestSolution[i].getCenterX() > pos.point.getX()) ? Constants.NORTHWEST : Constants.NORTHEAST;
            l[i] = pos;
        }

        return l;
    }
}
