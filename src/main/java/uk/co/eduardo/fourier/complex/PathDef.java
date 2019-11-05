/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * TODO Insert description sentence here.
 *
 * @author erodri02
 */
public interface PathDef
{
   /**
    * @param pos the position between 0 and 1 (inclusive)
    * @return the interpolated point at that position.
    */
   Point2D.Double getAt( final double pos );

   /**
    * Gets a shape that can be drawn on screen.
    *
    * @return returns a shape for the path.
    */
   Shape getShape();
}
