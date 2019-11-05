/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.geom.Point2D;

/**
 * TODO Insert description sentence here.
 *
 * @author erodri02
 */
public class GenTest
{
   public static void main( final String[] args )
   {
      final PathDef path = new DirectPathDef( new Point2D.Double( -100, -100 ),
                                              new Point2D.Double( 100, -100 ),
                                              new Point2D.Double( 100, 100 ),
                                              new Point2D.Double( -100, 100 ) );

      final FourierGenerator generator = new FourierGenerator( 1 );
      generator.generateElements( path );
   }
}
