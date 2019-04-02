


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.*;
import java.io.*;
import java.util.*;
  
// Comparator that sorts points on X + Y.
class PointCmp implements Comparator<Point2D> {

    @Override
    public int compare(Point2D o1, Point2D o2) {
        double c1 = o1.getX() + o1.getY();
        double c2 = o2.getX() + o2.getY();
        if (c1 == c2) {
            return 0;
        } else {
            return (c1 < c2) ? 1 : -1;
        }
    }
}
// Comparator that sorts points on Y desc then on X asc
class PointCmpY implements Comparator<Point2D> {

    @Override
    public int compare(Point2D o1, Point2D o2) {
        
        if (o1.getY() == o2.getY()) {
            return 0;
        }
        return (o1.getY() < o2.getY()) ? 1 : -1;
    }
}

/**
 *
 * @author Ewout
 */
public class Solver2Pos extends Solver {
        
    @Override
    public LabelPos[] solve(Point2D[] inp, float aspectRatio) {
        int numOfPoints = inp.length;
        Point2D[] clone = inp.clone();
        // The input points.
        List<Point2D> points = Arrays.asList(inp);
        
        // Find the approx maximum height first to improve performance.
        int maxHeight = (int)Math.abs((points.get(points.size() - 1).getY() - points.get(0).getY()));
        maxHeight = shift(maxHeight);
        
        // The best solution found so far.
        Rectangle2D[] bestSolution = new Rectangle2D.Float[numOfPoints];
        // Holds the current solution.
        Rectangle2D[] labels       = new Rectangle2D.Float[numOfPoints];

        // Use binary search on the height to come to a solution.
        float height = maxHeight / 2.0f;
        float lastHeight = maxHeight;
        boolean higher = false;
        boolean lower = false;
        
        // Sort the input on the X + Y descending.
        Collections.sort(points, new PointCmp());
        
        // Loop until delta < 0.1
        while(Math.abs(lastHeight - height) > 0.1) {

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

                    // Compare the label with the already placed labels.
                    for (int j = 0; j < numOfElements; j++) {
                        if (pos.intersects(labels[j])) {
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
                float h = height;
                if(higher)height = height - (float)(Math.abs(lastHeight - height)/ 2.0);
                else height /= 2;
                lower = true;
                lastHeight = h;

            } else {
                // Remember the best solution.
                bestSolution = labels.clone();
                float h = height;
                if(lower) height = height + (float)(Math.abs(lastHeight - height)/ 2.0);
                else height *= 2;
                higher = true;
                lastHeight = h;
            }
        }
        
        // Convert all Rectangle2D objects to LabelPos objects.
        LabelPos[] l = new LabelPos[numOfPoints];

        float finalHeight = (float)bestSolution[0].getHeight();//roundFourth((float)bestSolution[0].getHeight());

        for (int i = 0; i < numOfPoints ; i++) {
            LabelPos pos = new LabelPos();

            pos.height    = finalHeight;
            pos.point     = points.get(i);
            pos.posType   = (bestSolution[i].getCenterX() > pos.point.getX()) ? Constants.NORTHEAST : Constants.NORTHWEST;
            //l[i] = pos;
            for (int j = 0; j < clone.length; j++) {
                Point2D point = clone[j];
                //System.out.println(point);
                if (point.getX() == pos.point.getX() && point.getY() == pos.point.getY()) {
                    l[j] = pos;
                }
                    
            }
        }
        return l;
    }
    public static float roundFourth(float f) {
        return (float)(Math.round(f * 4) / 4.0f);
    }
    // Shifts int to nearest power of 2.
    public static int shift(int f) {
        f = f - 1;
        f |= f >> 1;
        f |= f >> 2;
        f |= f >> 4;
        f |= f >> 8;
        f |= f >> 16;
        return f + 1;
    }
    /*
    float approximateMaxHeight(List<Point2D> points) {
        
        // Do not use.
        
        /*
        // Sort the input on the Y coordinate ascending.
        Collections.sort(points, new PointCmpY());

        // Approx max height, initially is highest point Y - lowest Point Y.
        float hMax = (float)(points.get(0).getY() - points.get(points.size() - 1).getY());
        
        return hMax;
        
        System.out.println("temp hMax: " + hMax);
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            
            float minDisRight = Float.MAX_VALUE;
            float minDisLeft  = Float.MAX_VALUE;
            
            Point2D closestLeft  = null;
            Point2D closestRight = null;
            
            // Loop all points that have a higher Y val than this point.
            for (int j = 0; j < i; j++) {
                Point2D point2 = points.get(j);
                
                float distance = (float)Math.hypot(point.getX() - point2.getX(), point.getY() - point2.getY());
                
                if (point.getX() < point2.getX()) {
                    // Set the minimal right distance.
                    if (distance < minDisRight) {
                        minDisRight = distance;
                        closestRight = point2;
                    }
                } else if(point.getX() > point2.getX()) {
                    // Set the minimal left distance.
                    if (distance < minDisLeft) {
                        minDisLeft = distance;
                        closestLeft = point2;
                    }
                } else {
                    // This else only triggers when point2 is directly above point
                    
                    // Set both distances if needed.
                    if (distance < minDisRight) {
                        minDisRight = distance;
                        closestRight = point2;
                    }
                    if (distance < minDisLeft) {
                        minDisLeft = distance;
                        closestLeft = point2;
                    }
                }
            }
            if (closestLeft != null && closestRight != null) {
                float deltaHRight = (float)(closestRight.getY() - point.getY());
                float deltaHLeft  = (float)(closestLeft.getY() - point.getY());
                
                if (hMax > minDisRight && hMax > minDisLeft) {
                    System.out.println("point: " + point.getX() + ":" + point.getY());
                    System.out.println("closest l: " + closestLeft.getX() + ":" + closestLeft.getY() + " r: "  + closestRight.getX() + ":" + closestRight.getY());
                    System.out.println("hMax:" + Math.min(Math.min(minDisRight,minDisLeft), hMax));
                }
                    
                // Update hMax if needed.
                hMax = Math.min(Math.max(minDisRight,minDisLeft), hMax);
            }
        }
        return hMax;
    }*/
}
