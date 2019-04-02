
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
 * @author Ken
 */
public class Solver4Pos extends Solver {

    // The estimated max height that a label will take.
    boolean ceilingfound = false;
    float high = 1.0f;
    float low = 0.0f;
    float optimalHeight;
    final int LOOPCOUNT = 16;
    int numOfPlacedLabels;
    Rectangle2D currentLabel;
    List<Point2D> sortedX; //all points sorted on X-value
    List<Point2D> sortedY; //all points sorted on Y-value
    List<Point2D> unsorted;
    Rectangle2D[] bestHorizontal;
    Rectangle2D[] bestVertical;
    int numOfPoints;
    
    
        
    @Override
    public LabelPos[] solve(Point2D[] inputPoints, float aspectRatio) {
        numOfPoints = inputPoints.length;
        // The best solution found so far.
        bestHorizontal = new Rectangle2D[numOfPoints];
        bestVertical = new Rectangle2D[numOfPoints];
        // Holds the current solution.
        Rectangle2D[] horizontalLabel = new Rectangle2D[numOfPoints];
        // Holds the current solution.
        Rectangle2D[] verticalLabel = new Rectangle2D[numOfPoints];
        
        unsorted = Arrays.asList(inputPoints.clone());
        //Create list of all input points
        sortedX = Arrays.asList(inputPoints.clone());
        // Sort the input on the X coordinate ascending.
        Collections.sort(sortedX, new Comparator<Point2D>() {
            public int compare(Point2D p1, Point2D p2) {
                return Double.compare(p1.getX(), p2.getX());
            }
        });
        
        //Create list of all input points
        sortedY = Arrays.asList(inputPoints.clone());
        // Sort the input on the Y coordinate ascending.
        Collections.sort(sortedY, new Comparator<Point2D>() {
            public int compare(Point2D p1, Point2D p2) {
                return Double.compare(p1.getY(), p2.getY());
            }
        });
        
        
        // Start with a height of 50.0f use binary search on the height to come to a solution.
        float height = high / 2.0f;
        int i = 0;
        while (high - low >= 0.25) {
            boolean impossible = false;
            float width = height * aspectRatio;
            numOfPlacedLabels = 0;
            
            for (int j = 0; j < numOfPoints; j++) {
                Point2D point = sortedX.get(j);//point that is currently being investigated
                // The label is first positioned in the West
                currentLabel = new Rectangle2D.Float((float) point.getX()-width, (float) point.getY()-height, width, 2*height);
                
                boolean XOverlap = checkHorizontalLabelOverlap(horizontalLabel);
                // Compare the label with the already placed horizontalLabel.
                
                // If there is overlap between the current label and already
                    //set label, place the label in the East.
                if (XOverlap) {
                    currentLabel = new Rectangle2D.Float((float) point.getX(), (float) point.getY()-height, width, 2*height);
                    // Compare the label with the points not yet calculated (points to its left).
                    if (checkPointOverlap(numOfPoints, j) || checkHorizontalLabelOverlap(horizontalLabel)) {
                        impossible = true;
                        ceilingfound = true;
                        break;
                    }
                }
                horizontalLabel[j] = currentLabel; //Add the current label
                
                point = sortedY.get(j);
                // The label is first positioned in the West
                if (XOverlap){
                    currentLabel = new Rectangle2D.Float((float) point.getX(), (float) point.getY()-height, width, height);
                } else {
                    currentLabel = new Rectangle2D.Float((float) point.getX()-width, (float) point.getY()-height, width, height);
                }
                //currentLabel = new Rectangle2D.Float((float) point.getX()-width, (float) point.getY()-height, 2*width, height);
                boolean YOverlap = checkVerticalLabelOverlap(verticalLabel);
                // Compare the label with the already placed horizontalLabel.
                // If there is overlap between the current label and already
                    //set label, place the label in the East.
                if (YOverlap) {
                    if (XOverlap) {
                        currentLabel = new Rectangle2D.Float((float) point.getX(), (float) point.getY(), width, height);
                    } else {
                        currentLabel = new Rectangle2D.Float((float) point.getX()-width, (float) point.getY(), width, height);
                    }
                    
                    // Compare the label with the points not yet calculated (points above).
                    //impossible = checkVerticalPointOverlap(numOfPoints, j);
                    if (checkVerticalPointOverlap(numOfPoints, j) || checkVerticalLabelOverlap(verticalLabel)) {
                        impossible = true;
                        ceilingfound = true;
                        break;
                    }
                }
                verticalLabel[j] = currentLabel; //Add the current label

                numOfPlacedLabels++;
            }
            
            if (!impossible) {
                bestHorizontal = horizontalLabel.clone();
                bestVertical = verticalLabel.clone();
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
            boolean[] east = new boolean[numOfPoints];
            boolean[] north = new boolean[numOfPoints];
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
                            if (bestHorizontal[j].getMaxX() > xValue) {
                                east[j] = true;
                            } else {
                                east[j] = false;
                            }
                            found = true;
                        }
                        j--;
                    }
                    j = searchValue;
                    while (j < numOfPoints && sortedX.get(j).getX() == xValue && !found) {
                        if (sortedX.get(j).getY() == yValue){
                            if (bestHorizontal[j].getMaxX() > xValue) {
                                east[j] = true;
                            } else {
                                east[j] = false;
                            }
                            found = true;
                        }
                        j++;
                    }
                }
            }
            found = false;
            high = numOfPoints-1;
            low = 0;
            searchValue = (int) (high-low)/2;
            while (!found) {
                 if (sortedY.get(searchValue).getY() > yValue) {
                    searchValue = (int) Math.floor(searchHeight(searchValue, true));
                } else if (sortedY.get(searchValue).getY() < yValue){
                    searchValue = (int) Math.ceil(searchHeight(searchValue, false));
                } else {
                    j = searchValue;
                    while (j >= 0 && sortedY.get(j).getY() == yValue && !found) {
                        if (sortedY.get(j).getX() == xValue){
                            if (bestHorizontal[j].getMaxY() > yValue) {
                                north[j] = true;
                            } else {
                                north[j] = false;
                            }
                            found = true;
                        }
                        j--;
                    }
                    j = searchValue;
                    while (j < numOfPoints && sortedY.get(j).getY() == yValue && !found) {
                        if (sortedY.get(j).getX() == xValue){
                            if (bestHorizontal[j].getMaxY() > yValue) {
                                north[j] = true;
                            } else {
                                north[j] = false;
                            }
                            found = true;
                        }
                        j++;
                    }
                }
            }
            if (north[i] && east[i]){
                singleLabel.posType = Constants.NORTHEAST;
            } else if (!north[i] && east[i]) {
                singleLabel.posType = Constants.SOUTHEAST;
            } else if (north[i] && !east[i]) {
                singleLabel.posType = Constants.NORTHWEST;
            } else {
                singleLabel.posType = Constants.SOUTHWEST;
            }
            result[i] = singleLabel;
        } 
        return result;
    }
    
    public boolean checkVerticalLabelOverlap(Rectangle2D[] labels) {
        for (int k = numOfPlacedLabels-1; k >=0; k--) {
            if (labels[k].getMaxY() >= currentLabel.getMinY()) {
                if (currentLabel.intersects(labels[k])) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    public boolean checkHorizontalLabelOverlap(Rectangle2D[] labels) {
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
    
    public boolean checkVerticalPointOverlap(int numOfPoints, int j) {
        for (int l = j+1; l < numOfPoints; l++) {
            if (currentLabel.getMaxY() >= sortedY.get(l).getY()){
                if (currentLabel.contains(sortedY.get(l))) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    public boolean checkPointOverlap(int numOfPoints, int j) {
        for (int l = j+1; l < numOfPoints; l++) {
            if (currentLabel.getMaxX() >= sortedX.get(l).getX()){
                if (currentLabel.contains(sortedX.get(l))) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    
    public float searchHeight(float currentHeight, boolean decrease) {
        if (ceilingfound) {
            if (decrease) {
                high = currentHeight;
                return (high + low) / 2;
            } else {
                // Remember the best solution.
                low = currentHeight;
                return (high + low) / 2;
            }
        } else {
            low = currentHeight;
            high = 2*currentHeight;
            return high;
        }
        
    }
    
}
