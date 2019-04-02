
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
 * @author Ken Rutten
 */
public class Solver4Pos extends Solver {

    // The estimated max height that a label will take.
    boolean ceilingfound = false;
    double high = 0.1d;
    double low = 0.0d;
    double optimalHeight;
    int numOfPlacedLabels;
    Rectangle2D currentLabel;
    List<Point2D> sortedX; //all points sorted on X-value
    List<Point2D> unsorted;
    Rectangle2D[] bestSolution;
    int numOfPoints;
    
    
        
    @Override
    public LabelPos[] solve(Point2D[] inputPoints, double aspectRatio) {
        numOfPoints = inputPoints.length;
        // The best solution found so far.
        bestSolution = new Rectangle2D[numOfPoints];
        // Holds the current solution.
        Rectangle2D[] label = new Rectangle2D[numOfPoints];
        
        unsorted = Arrays.asList(inputPoints.clone());
        //Create list of all input points
        sortedX = Arrays.asList(inputPoints.clone());
        // Sort the input on the X coordinate ascending.
        Collections.sort(sortedX, new Comparator<Point2D>() {
            public int compare(Point2D p1, Point2D p2) {
                return Double.compare(p1.getX(), p2.getX());
            }
        });
        
        
        
        // Start with a height of 50.0f use binary search on the height to come to a solution.
        double height = high / 2.0d;
        int i = 0;
        while (high - low >= 0.01) {
            boolean impossible = false;
            double width = height * aspectRatio;
            numOfPlacedLabels = 0;
            
            for (int j = 0; j < numOfPoints; j++) {
                Point2D point = sortedX.get(j);//point that is currently being investigated
                // The label is first positioned in the SOUTHEAST
                currentLabel = new Rectangle2D.Double(point.getX()-width, point.getY()-height, width, height);
                
                //boolean OverlapSW = checkLabelOverlap(label);
                // Compare the label with the already placed label.
                
                // If there is overlap between the current label and already
                    //set label, place the label in the East.
                if (checkPointOverlap(numOfPoints, j) || checkLabelOverlap(label)) {
                    currentLabel = new Rectangle2D.Double(point.getX()-width, point.getY(), width, height);
                    // Compare the label with the points not yet calculated (points to its left).
                    //boolean OverLapNW = checkLabelOverlap(label);
                    
                    if (checkPointOverlap(numOfPoints, j) || checkLabelOverlap(label)){
                        currentLabel = new Rectangle2D.Double(point.getX(), point.getY()-height, width, height);
                        if (checkPointOverlap(numOfPoints, j) || checkLabelOverlap(label)) {
                            currentLabel = new Rectangle2D.Double(point.getX(), point.getY(), width, height);
                             if (checkPointOverlap(numOfPoints, j) || checkLabelOverlap(label)) {
                                 impossible = true;
                                 ceilingfound = true;
                                 break;
                             }
                            
                        }
                    }
                    
                }
                label[j] = currentLabel; //Add the current label
                numOfPlacedLabels++;
            }
            if (!impossible) {
                bestSolution = label.clone();
                optimalHeight = height;
            }
            
            // Use binary search on the height to find a solution.
            height = searchHeight(height, impossible);
            i++;
        }
        ceilingfound = true;
         
        return configure(numOfPoints);
    }
    
    public LabelPos[] configure(int numOfPoints) {
        LabelPos[] result = new LabelPos[numOfPoints];
        for (int i = 0; i < numOfPoints; i++) {
            LabelPos singleLabel = new LabelPos();
            boolean found = false;
            high = numOfPoints-1;
            low = 0;
            int searchValue = (int) (high-low)/2;
            int j;
            double xValue = unsorted.get(i).getX();
            double yValue = unsorted.get(i).getY();
            singleLabel.point = unsorted.get(i);
            singleLabel.height = optimalHeight;
            while (!found) {
                if (sortedX.get(searchValue).getX() > xValue) {
                    searchValue = (int) Math.floor(searchHeight(searchValue, true));
                } else if (sortedX.get(searchValue).getX() < xValue){
                    searchValue = (int) Math.ceil(searchHeight(searchValue, false));
                } else {
                    j = searchValue;
                    while (j >=0 && sortedX.get(j).getX() == xValue && !found) {
                        if (sortedX.get(j).getY() == yValue){
                            if (bestSolution[j].getCenterX() > xValue && bestSolution[j].getCenterY() > yValue) {
                                singleLabel.posType = Constants.NORTHEAST;
                            } else if (bestSolution[j].getCenterX() > xValue && bestSolution[j].getCenterY() < yValue){
                                singleLabel.posType = Constants.SOUTHEAST;
                            } else if (bestSolution[j].getCenterX() < xValue && bestSolution[j].getCenterY() > yValue){
                                singleLabel.posType = Constants.NORTHWEST;
                            } else if (bestSolution[j].getCenterX() < xValue && bestSolution[j].getCenterY() < yValue){
                                singleLabel.posType = Constants.SOUTHWEST;
                            }
                            found = true;
                        }
                        j--;
                    }
                    j = searchValue;
                    while (j < numOfPoints && sortedX.get(j).getX() == xValue && !found) {
                        if (sortedX.get(j).getY() == yValue){
                            if (bestSolution[j].getCenterX() > xValue && bestSolution[j].getCenterY() > yValue) {
                                singleLabel.posType = Constants.NORTHEAST;
                            } else if (bestSolution[j].getCenterX() > xValue && bestSolution[j].getCenterY() < yValue){
                                singleLabel.posType = Constants.SOUTHEAST;
                            } else if (bestSolution[j].getCenterX() < xValue && bestSolution[j].getCenterY() > yValue){
                                singleLabel.posType = Constants.NORTHWEST;
                            } else if (bestSolution[j].getCenterX() < xValue && bestSolution[j].getCenterY() < yValue){
                                singleLabel.posType = Constants.SOUTHWEST;
                            }
                            found = true;
                        }
                        j++;
                    }
                }
            }
            result[i] = singleLabel;
        } 
        return result;
    }
    
    public boolean checkLabelOverlap(Rectangle2D[] labels) {
        for (int k = numOfPlacedLabels-1; k >= 0; k--) {
            if (labels[k].getMaxX() >= currentLabel.getMinX()) {
                if (currentLabel.intersects(labels[k])) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    public boolean checkPointOverlap(int numOfPoints, int j) {
        for (int k = j+1; k < numOfPoints; k++) {
            if (currentLabel.getMaxX() >= sortedX.get(k).getX()){
                if (currentLabel.contains(sortedX.get(k))) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    
    public double searchHeight(double currentHeight, boolean decrease) {
        if (ceilingfound) {
            if (decrease) {
                high = currentHeight;
                return (high + low) / 2.0d;
            } else {
                // Remember the best solution.
                low = currentHeight;
                return (high + low) / 2.0d;
            }
        } else {
            low = currentHeight;
            high = 2.0d * currentHeight;
            return high;
        }
        
    }
    
}
