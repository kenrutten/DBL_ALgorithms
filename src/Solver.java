/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.geom.Point2D;

/**
 *
 * @author Ewout
 */
public abstract class Solver {
     /**
     * Calculates label position and dimension for every input point.
     *
     * @param input The point input.
     * @param aspectRatio The aspect ratio the labels should adhere to.
     * @return An array containing Label objects.
     */
    public abstract Label[] solve(Point2D[] input, double aspectRatio);

}
