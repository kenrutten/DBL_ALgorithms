

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 20173936
 */
class PointCmp2 implements Comparator<Point2D> {

    @Override
    public int compare(Point2D a, Point2D b) {
        return (a.getX() > b.getX()) ? -1 : (a.getX() < b.getX()) ? 1 : 0;
    }
}

public class SliderSolver extends Solver {

    /**
     *
     * @param input
     * @param aspectRatio
     * @return
     */
    private LabelSlider[] l;
    private double height;
    private double width;
    private double aspectRatio;
    private Point2D[] inputs;
    private Rectangle2D pos;  
    private double posX, posY, posW;

    public boolean inside(Rectangle2D rect, Point2D point) {
        return point.getX() > rect.getX() && point.getX() < rect.getX() + rect.getWidth() && point.getY() >= rect.getY() && point.getY() < rect.getY() + rect.getHeight();
    }

    public void updatePos() {
            posX = pos.getX();
            posY = pos.getY();
            posW = pos.getWidth();
    }
    
    @Override
    public LabelSlider[] solve(Point2D[] input, float aR) {
        aspectRatio = aR;
        inputs = Arrays.copyOf(input, input.length);
        Collections.sort(Arrays.asList(input), new PointCmp2());
        this.aspectRatio = aspectRatio;
        l = new LabelSlider[input.length];
        boolean higher = false;
        boolean lower = false;
        boolean possible = true;
        int factor = 2;
        height = 4;
        double change = height;
        boolean max = false;
        adjustWidth();
        Map<Rectangle2D, Double> labels = new HashMap<>();
        Map<Rectangle2D, Double> solution = new HashMap<>();
        Map<Rectangle2D, Double> bestSol = new HashMap<>();
        double bestHeight = 0;
        int iterations = 0;
        for (int i = 0; i < input.length; i++) {
            if (i == 0) {
                max = true;
                labels.clear();
                solution.clear();
                //System.out.println("Height: " + height + "     Change: " + change + "     Factor: " + factor);
            }
            Point2D point = input[i];
            int x = (int) point.getX();
            int y = (int) point.getY();
            pos = new Rectangle2D.Double(x, y, width, height);
            updatePos(); 
            boolean intersect = false;
            double offset = 0;
            for (Rectangle2D rect : labels.keySet()) {
                if (posX <= rect.getX()) {
                    if (pos.intersects(rect)) {
                        intersect = true;
                        offset =  ((posX + posW) - rect.getX());
//                        if (height == 2) {
//                            System.out.println("POSRECT");
//                            System.out.println(pos.getBounds());
//                            System.out.println(rect.getBounds());
//                            System.out.println(offset);
//                        }
                        break;
                    }
                }
                
            }
            if (!intersect) {
                for (Point2D p : input) {
                    if (posX <= p.getX() && !(p.getY() < posY)) {
                        if (inside(pos, p)) {
                            intersect = true;
                            offset = ((posX + posW) - p.getX());
//                            if (height == 2) {
//                                System.out.println("POSPOINT");
//                                System.out.println(pos.getBounds());
//                                System.out.println(test.getX() + "    " + test.getY());
//                                System.out.println(offset);
//                            }
                            break;
                        }
                    }
                }
            }
            if (intersect) {
                Point2D current = new Point2D.Double(posX, posY);
                pos = new Rectangle2D.Double((x) - offset, (int) y, width, height);
                updatePos();
                for (Rectangle2D rect : labels.keySet()) {
                    if (posX <= rect.getX()) {
                        if (pos.intersects(rect)) {
//                        if (height == 2) {
//                            System.out.println("IMPRECT");
//                            
//                            System.out.println(pos.getBounds());
//                            System.out.println(rect.getBounds());
//                            System.out.println(offset);
//                        }
                            possible = false;
                            break;
                        }
                    }
                }
                if (possible) {
                    for (Point2D p : input) {
                        if (!(p.getX() == current.getX() && p.getY() == current.getY()) && !(p.getY() < posY)) {
                            if (inside(pos, p)) {
//                                if (height == 2) {
//                                    System.out.println("IMPPOINT");
//                                    System.out.println(pos.getBounds());
//                                    System.out.println(test.getX() + "    " + test.getY());
//                                    System.out.println(offset);
//                                }
                                possible = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (!possible) {
                if (higher) {
                    lower = true;
                }
                if (iterations >= 5) {
                    break;
                }

                if (higher && lower) {
                    factor *= 2;
                    iterations++;
                }
                lower = true;

                height -= change / factor;
                if (!higher) {
                    change = height;
                }

                adjustWidth();
                i = -1;
                possible = true;
                continue;
            } else {
                labels.put(pos, offset);
            }

            if (i == input.length - 1) {
                labels.keySet().forEach((rect) -> {
                    solution.put(rect, labels.get(rect));
                });
                if (height > bestHeight) {
                    bestHeight = height;
                    bestSol.clear();

                    labels.keySet().forEach((rect) -> {
                        bestSol.put(rect, labels.get(rect));
                    });
                }

                if (iterations < 5) {
                    if (lower && higher) {
                        iterations++;
                        factor *= 2;
                    }

                    if (!lower) {
                        height += change;
                        change = height;
                    } else {
                        height += change / factor;
                    }
                    higher = true;

                    adjustWidth();
                    i = -1;
                }
                
                
            }
        }
        convertToLabel(bestSol);
        return l;
    }

    public void convertToLabel(Map<Rectangle2D, Double> list) {
        for (Rectangle2D rect : list.keySet()) {
            LabelSlider label = new LabelSlider();
            double px = rect.getX() + list.get(rect);
            double py = rect.getY();
            label.height =(float) rect.getHeight();
            label.point = new Point2D.Double(px, py);
            label.placement = 1 - (list.get(rect) / rect.getWidth());
            for (int j = 0; j < list.size(); j++) {
                if (px == inputs[j].getX() && py == inputs[j].getY()) {
                    l[j] = label;
                    break;
                }
            }
        }
    }

    void adjustWidth() {
        width = height * aspectRatio;
        width = Math.ceil(width * 1000000d) / 1000000d;
    }
}
