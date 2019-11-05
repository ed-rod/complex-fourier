/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGPathElementBridge;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;

/**
 * A path definition that reads from an SVG file.
 *
 * @author erodri02
 */
public class SvgPathDef extends DirectPathDef
{
   SvgPathDef( final String resourceName, final String pathId )
   {
      super( readData( resourceName, pathId ) );
   }

   private static List< Point2D.Double > translateAndRescale( final List< Point2D.Double > points, final int dim )
   {
      final List< Point2D.Double > rescaled = new ArrayList<>( points.size() );

      double minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE, maxy = Integer.MIN_VALUE;
      for( final Point2D.Double point : points )
      {
         minx = Math.min( minx, point.x );
         miny = Math.min( miny, point.y );
         maxx = Math.max( maxx, point.x );
         maxy = Math.max( maxy, point.y );
      }
      final double extent = Math.max( maxx - minx, maxy - miny );
      final double sf = ( 2 * dim ) / extent;
      final double translateX = ( maxx + minx ) / 2;
      final double translateY = ( maxy + miny ) / 2;

      for( final Point2D.Double point : points )
      {
         final double newX = ( point.x - translateX ) * sf;
         final double newY = ( point.y - translateY ) * ( -sf );
         rescaled.add( new Point2D.Double( newX, newY ) );
      }
      return rescaled;
   }

   private static Point2D.Double[] readData( final String resourceName, final String pathId )
   {
      try
      {
         final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( resourceName );

         final String parser = XMLResourceDescriptor.getXMLParserClassName();
         final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory( parser );
         final SVGDocument doc = f.createSVGDocument( null, stream );

         final SVGOMPathElement path = getPathElement( doc, pathId );

         final UserAgent userAgent = new UserAgentAdapter();
         final DocumentLoader loader = new DocumentLoader( userAgent );
         final BridgeContext bctx = new BridgeContext( userAgent, loader );
         bctx.setDynamicState( BridgeContext.STATIC );
         final GVTBuilder builder = new GVTBuilder();
         builder.build( bctx, doc );

         final SVGPathElementBridge bridge = new SVGPathElementBridge();
         bridge.createGraphicsNode( bctx, path );
         path.setSVGContext( bridge );

         final int pointCount = 2000;
         final float totalLength = path.getTotalLength();
         final List< Point2D.Double > points = new ArrayList<>( pointCount );
         for( int i = 0; i < pointCount; i++ )
         {
            final float pos = (float) i / pointCount;
            final SVGPoint p = path.getPointAtLength( pos * totalLength );
            points.add( new Point2D.Double( p.getX(), p.getY() ) );
         }
         final List< Point2D.Double > rescaled = translateAndRescale( points, 600 );
         return rescaled.toArray( new Point2D.Double[ rescaled.size() ] );
      }
      catch( final IOException exception )
      {
         return new Point2D.Double[ 0 ];
      }
   }

   private static SVGOMPathElement getPathElement( final SVGDocument doc, final String pathId )
   {
      final NodeList pathElements = doc.getElementsByTagName( "path" ); //$NON-NLS-1$
      for( int i = 0; i < pathElements.getLength(); i++ )
      {
         final Node pathElement = pathElements.item( i );
         final Node idAttribute = pathElement.getAttributes().getNamedItem( "id" ); //$NON-NLS-1$
         if( idAttribute != null )
         {
            if( idAttribute.getTextContent().equals( pathId ) )
            {
               // Found it!
               return (SVGOMPathElement) pathElement;
            }
         }
      }
      return null;
   }
}
