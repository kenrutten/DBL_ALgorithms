/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import main.Constants;
import main.Label2Pos;
import main.Solver2Pos;
import main.Solver4Pos;

/**
 * The master class is the main class of the project, it reads input from a file
 * or from the Scanner and provides output via standard output.
 *
 * @author Ewout
 */
public class Master {

    // The aspect ratio of the labels, given as input.
    float aspectRatio;

    // The number of points, given as input.
    int numOfPoints;
    // The placement model type, given as input can be POS_2 or POS_4 or POS_SLIDER.
    String model;
    // A list of POINT2D objects that represent the input points on the plane.
    Point2D[] points;

    String[] posPlacement;
    float[] sliderPlacement;
    float height = 1.25f;

    // Read the input via the scanner.
    Master() {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();

        // Obtain the aspectRatio, numOfPoints and model type.
        for (int i = 0; i < 3; i++) {
            String line = sc.nextLine();
            String value = line.substring(line.lastIndexOf(':') + 1).trim();
            if (i == 0) {
                model = value;
            } else if (i == 1) {
                aspectRatio = Float.parseFloat(value);
            } else {
                numOfPoints = (int) Float.parseFloat(value);
            }
        }

        // Create the points array with specified size.
        points = new Point2D[numOfPoints];

        // Retrieve all points from the file and store them in the points array.
        for (int i = 0; i < numOfPoints; i++) {
            String pointInput = sc.nextLine();
            String[] val = pointInput.split("\\s+");
            points[i] = new Point2D.Float((int) Float.parseFloat(val[0]), (int) Float.parseFloat(val[1]));
        }
        this.inputRead();
    }

    // Read the input in a file.
    Master(String pathToFile) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(pathToFile));
            // Obtain the aspectRatio, numOfPoints and model type.
            for (int i = 0; i < 3; i++) {
                String line = reader.readLine();
                String value = line.substring(line.lastIndexOf(':') + 1).trim();
                if (i == 0) {
                    model = value;
                } else if (i == 1) {
                    aspectRatio = Float.parseFloat(value);
                } else {
                    numOfPoints = (int) Float.parseFloat(value);
                }
            }

            // Create the points array with specified size.
            points = new Point2D[numOfPoints];

            // Retrieve all points from the file and store them in the points array.
            for (int i = 0; i < numOfPoints; i++) {
                String pointInput = reader.readLine();
                String[] val = pointInput.split("\\s+");
                points[i] = new Point2D.Float((int) Float.parseFloat(val[0]), (int) Float.parseFloat(val[1]));

            }
            this.inputRead();

        } catch (IOException e) {
            System.out.println("Critical error: " + e.getLocalizedMessage());
        }
    }

    /**
     * This method should be called when all input is read.
     *
     * @pre points, aspect ratio and model are set.
     */
    private void inputRead() {
        if (model.equals(Constants.POS_2)) {
            // Solve for 2 pos model.
            Solver2Pos solver = new Solver2Pos();
            Label2Pos[] solution = solver.solve(points, aspectRatio);
        } 
        else if (model.equals(Constants.POS_4)) {
            Solver4Pos solver4pos = new Solver4Pos();
            Label2Pos[] solution = solver4pos.solve(points, aspectRatio);
        } 
        else {
            // Solve for slider model.
            // TODO
        }
    }

    //  ---------------------------
    public void checkOverlap() {
        if (model.equals("2pos")) {
            checkOverlap2Pos();
        }
        if (model.equals("4pos")) {
            checkOverlap4Pos();
        }
        if (model.equals("1slider")) {
            checkOverlap1Slider();
        }
    }

    //Check if there is overlap between points in 2Pos placement model
    public void checkOverlap2Pos() {
        for (int i = 0; i < numOfPoints; i++) {
            for (int j = 0; j < numOfPoints; j++) {
                if (i != j) {
                    if ((points[i].getY() + height > points[j].getY()
                            || points[j].getY() + height > points[i].getY())
                            && horizontalOverlapPos(i, j)) {
                        //There is overlap
                    }
                }
            }
        }
    }

    //Check if there is overlap between points in 4Pos placement model
    public void checkOverlap4Pos() {
        for (int i = 0; i < numOfPoints; i++) {
            for (int j = 0; j < numOfPoints; j++) {
                if (i != j) {

                }
            }
        }
    }

    //Check if there is overlap between points in 1Slider placement model
    public void checkOverlap1Slider() {
        for (int i = 0; i < numOfPoints; i++) {
            for (int j = 0; j < numOfPoints; j++) {
                if (i != j) {

                }
            }
        }
    }

    public boolean horizontalOverlapPos(int p1, int p2) {
        //Outer y-value of 1st point
        float o1 = calcOuterYValuePos(p1);
        //Outer y-value of 2nd point
        float o2 = calcOuterYValuePos(p2);
        float y1 = (float) points[p1].getY();
        float y2 = (float) points[p2].getY();

        if (o1 > o2 && o1 < y2
                || o1 > y2 && o1 < o2
                || y1 > y2 && y1 < o2
                || y1 > o2 && y1 < y2) {
            return true;
        } else {
            return false;
        }
    }

    public float calcOuterYValuePos(int point) {

        if (posPlacement[point].equals("NE")
                || posPlacement[point].equals("SE")) {
            return (float) points[point].getY() + (height * aspectRatio);
        } else {
            return (float) points[point].getY() - (height * aspectRatio);
        }
    }

    //Initialize the array for either a pos placement or the slider placement
    public void initializePlacement() {
        if (model.equals("2pos") || model.equals("4pos")) {
            posPlacement = new String[numOfPoints];
        } else if (model.equals("1slider")) {
            sliderPlacement = new float[numOfPoints];
        }
    }

    public static void main(String args[]) {
        Master m = new Master();
    }
}
