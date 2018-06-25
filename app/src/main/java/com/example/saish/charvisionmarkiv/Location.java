package com.example.saish.charvisionmarkiv;

import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.api.services.vision.v1.model.Word;

import java.util.List;

public class Location {



    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_RIGHT =2;
    public static final int BOTTOM_LEFT = 3;


    //method to get x cordinate from a  word od Vth vertex
    public static int getx(Word word, int corner)
    {
        String vertex = word.getBoundingBox().getVertices().get(corner).toString();

        int x1 = vertex.indexOf("x") + 3;
        int x=0;
        for(int h = x1 ; !((Character) vertex.charAt(h)).equals(',');h++)
        {
            Character c = vertex.charAt(h);
            int cint = c.charValue() - ((Character) '0').charValue();
            x = x*10 + cint;
        }
        return x;
    }

    //method to get y cordinate from a  vertexstring
    public static int gety(Word word, int corner)
    {
        String vertex = word.getBoundingBox().getVertices().get(corner).toString();

        int y1 = vertex.indexOf("y") + 3;
        int y=0;
        for(int h = y1 ; !((Character) vertex.charAt(h)).equals('}');h++)
        {
            Character c = vertex.charAt(h);
            int cint = c.charValue() - ((Character) '0').charValue();
            y = y*10 + cint;
        }
        return y;
    }


    public static void createNameBox(Word date)
    {
        int x_top_left = Location.getx(date,Location.TOP_LEFT);
        int x_bottom_left = Location.getx(date,Location.BOTTOM_LEFT);
        int x_bottom_right = Location.getx(date,Location.BOTTOM_RIGHT);
        int y_bottom_left = Location.gety(date,Location.BOTTOM_LEFT);
        int y_bottom_right = Location.gety(date,Location.BOTTOM_RIGHT);
        int y_top_left = Location.gety(date,Location.TOP_LEFT);



        CropBox.DATE_X = x_bottom_left + (x_bottom_left-x_top_left);
        CropBox.DATE_Y = y_bottom_left;

        CropBox.DATE_WIDTH = ((int) ((x_bottom_right - x_bottom_left) * 11.5));
        CropBox.DATE_HEIGHT = ((int) ((y_bottom_left - y_top_left) * 4.1));
        int z = 10 * 100 + 1001 -5;

    }


}
