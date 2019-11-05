/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

/**
 * TODO Insert description sentence here.
 *
 * @author erodri02
 */
public class AngleTest
{
   public static void main( final String[] args )
   {
      for( int i = 0; i < 360; i += 30 )
      {
         final double angle = Math.toRadians( i );
         final double x = Math.cos( angle );
         final double y = Math.sin( angle );
         final double ix = Math.toDegrees( Math.acos( x ) );
         final double iy = Math.toDegrees( Math.asin( y ) );
         final double ri = y < 0 ? 360 - ix : ix;
         System.out.printf( "%+.2f, %+.2f\t\t(%d)\t%.1f\t%.1f\t%.1f\n", x, y, i, ri, ix, iy );
      }
   }
}
