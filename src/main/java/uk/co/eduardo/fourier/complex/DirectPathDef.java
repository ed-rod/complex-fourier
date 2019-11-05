/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

/**
 * Path definition that is fromed from a bunch of points. The last point is automatically connected back to the first.
 *
 * @author erodri02
 */
public class DirectPathDef implements PathDef
{
   final List< Line2D.Double > lines = new ArrayList<>();

   final Path2D.Double path = new Path2D.Double();

   DirectPathDef( final Point2D.Double... points )
   {
      if( points.length < 2 )
      {
         throw new IllegalArgumentException( "Need at least two points in the path" ); //$NON-NLS-1$
      }

      // Create lines from all the points (joining the last one back to the beginning)
      Point2D.Double last = null;
      this.path.moveTo( points[ 0 ].x, points[ 0 ].y );
      for( int i = 0; i < points.length; i++ )
      {
         final Point2D.Double current = points[ i ];
         if( i != 0 )
         {
            this.lines.add( new Line2D.Double( last, current ) );
            this.path.lineTo( current.x, current.y );
         }
         last = current;
      }
      // Add a line from the last back to the first
      this.lines.add( new Line2D.Double( last, points[ 0 ] ) );
      this.path.lineTo( points[ 0 ].x, points[ 0 ].y );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Double getAt( final double pos )
   {
      final double lineFraction = 1.0 / this.lines.size();
      final int lineIndex = (int) ( pos * this.lines.size() );
      final Line2D.Double line = this.lines.get( lineIndex );
      // Interpolate along this line
      final double a = pos - ( lineIndex * lineFraction );
      final double p = a * this.lines.size();

      final double dx = ( line.x2 - line.x1 ) * p;
      final double dy = ( line.y2 - line.y1 ) * p;

      return new Point2D.Double( line.x1 + dx, line.y1 + dy );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Shape getShape()
   {
      return this.path;
   }
}
