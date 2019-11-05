/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Generates a list of fourier elements that will approximate a path.
 *
 * @author erodri02
 */
public class FourierGenerator
{
   private final int frequencies;

   /**
    * Initializes a new FourierGenerator object.
    *
    * @param frequencies the number of frequencies to calculate. Ranges from -frequencies to +frequencies.
    */
   public FourierGenerator( final int frequencies )
   {
      this.frequencies = frequencies;
   }

   /**
    * Generates the fourier elements.
    *
    * @param path the path definition that we are trying to approximate.
    * @return a list of fourier elements that will approximate the path.
    */
   public List< FourierElement > generateElements( final PathDef path )
   {
      final List< FourierElement > elements = new ArrayList<>();
      final double dt = 0.001;

      for( int n = -this.frequencies; n <= this.frequencies; n++ )
      {
         double x = 0;
         double y = 0;
         for( double t = 0; t < 1; t += dt )
         {
            final Point2D.Double ft = path.getAt( t );
            final Complex power = Complex.ePow2PiT( t * ( -n ) );
            final Complex ftc = new Complex( ft.x, ft.y );
            final Complex product = power.mul( ftc );
            final double termX = product.r * dt;
            final double termY = product.i * dt;
            x += termX;
            y += termY;
         }
         final Complex coefficient = new Complex( x, y );
         final FourierElement elem = new FourierElement( n, coefficient );
         elements.add( elem );
      }
      Collections.sort( elements, new Comparator< FourierElement >()
      {
         @Override
         public int compare( final FourierElement o1, final FourierElement o2 )
         {
            return Math.abs( o1.getFrequency() ) - Math.abs( o2.getFrequency() );
         }
      } );
      return elements;
   }
}
