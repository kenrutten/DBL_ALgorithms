

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

/**
 * The master class is the main class of the project, it reads input from a file
 * or from the Scanner and provides output via standard output.
 *
 * @author Ewout
 */
public class Master {

    // The aspect ratio of the labels, given as input.
    double aspectRatio;

    // The number of points, given as input.
    int numOfPoints;
    // The placement model type, given as input can be POS_2 or POS_4 or POS_SLIDER.
    String model;
    // A list of POINT2D objects that represent the input points on the plane.
    Point2D[] points;
    String aR;
    String[] posPlacement;
    double[] sliderPlacement;
    double height = 1.25f;
    LabelPos[] posSolution;
    LabelSlider[] sliderSolution;

    // Read the input via the scanner.
    public Master() {
        Scanner sc = new Scanner(System.in);

        // Obtain the aspectRatio, numOfPoints and model type.
        for (int i = 0; i < 3; i++) {
            String line = sc.nextLine();
            String value = line.substring(line.lastIndexOf(':') + 1).trim();
            switch (i) {
            case 0:
                model = value;
                break;
            case 1:
                aR=value;
                aspectRatio = Double.parseDouble(value);
                break;
            default:
                numOfPoints = Integer.parseInt(value);
                break;
            }
        }

        // Create the points array with specified size.
        points = new Point2D[numOfPoints];

        // Retrieve all points from the file and store them in the points array.
        for (int i = 0; i < numOfPoints; i++) {
            String pointInput = sc.nextLine();
            String[] val = pointInput.split("\\s+");
            points[i] = new Point2D.Double((int)Double.parseDouble(val[0]), (int)Double.parseDouble(val[1]));
        }
        inputRead();
        printOutput();
    }

    /**
     * This method should be called when all input is read.
     *
     * @pre points, aspect ratio and model are set.
     */
    private void inputRead() {
        switch (model) {
        case Constants.POS_2:
            {
                // Solve for 2 pos model.
                Solver2Pos solver = new Solver2Pos();
                posSolution = solver.solve(points, aspectRatio);
                break;
            }
        case Constants.POS_4:
            {
                //if (numOfPoints <= 25){
                //Solver4PosSmall solver = new Solver4PosSmall();
                //posSolution = solver.solve(points, aspectRatio);
                //} else {
                Solver4Pos solver = new Solver4Pos();
                posSolution = solver.solve(points, aspectRatio);
                //}
                break;
            }
        default:
            {
                SliderSolver solver = new SliderSolver();
                sliderSolution = solver.solve(points, aspectRatio);
                break;
            }
        }
    }
    
    private void printOutput() {
        
        System.out.println("placement model: " + model);
        System.out.println("aspect ratio: " + aR);
        System.out.println("number of points: " + numOfPoints);
        
        if (model.equals(Constants.POS_SLIDER)) {
            System.out.println("height: " + sliderSolution[0].height);
            for (int i = 0; i < numOfPoints; i++) {
                System.out.println((int) sliderSolution[i].point.getX() + " " + 
                        (int) sliderSolution[i].point.getY() + " " + 
                        sliderSolution[i].placement);
                
            }
        } else {
            System.out.println("height: " + posSolution[0].height);
            for (int i = 0; i < numOfPoints; i++) {
                System.out.println((int) posSolution[i].point.getX() + " " + 
                        (int) posSolution[i].point.getY() + " " + 
                        posSolution[i].posType);
                
            }
        }
    }

    public static void main(String args[]) {
        Master m = new Master();
    }
}
