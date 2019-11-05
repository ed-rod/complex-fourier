/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGPathElementBridge;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;

/**
 * TODO Insert description sentence here.
 *
 * @author erodri02
 */
public class SvgPathDef extends DirectPathDef
{
   private static enum SvgCommand
   {
      M( 'M', "MoveTo (Abs)", 2 ),
      m( 'm', "MoveTo (Rel)", 2 ),

      Z( 'Z', "ClosePath", 0 ),
      z( 'z', "ClosePath", 0 ),

      H( 'H', "HorizTo (Abs)", 1 )
      {
         @Override
         Point2D.Double getPoint( final Point2D.Double last, final List< Double > current )
         {
            return super.calculatePoint( last, current.get( 0 ), last.y );
         }
      },
      h( 'h', "HorizTo (Rel)", 1 )
      {
         @Override
         Point2D.Double getPoint( final Point2D.Double last, final List< Double > current )
         {
            return super.calculatePoint( last, current.get( 0 ), last.y );
         }
      },

      V( 'V', "VertTo (Abs)", 1 )
      {
         @Override
         Point2D.Double getPoint( final Point2D.Double last, final List< Double > current )
         {
            return super.calculatePoint( last, last.x, current.get( 0 ) );
         }
      },
      v( 'v', "VertTo (Rel)", 1 )
      {
         @Override
         Point2D.Double getPoint( final Point2D.Double last, final List< Double > current )
         {
            return super.calculatePoint( last, last.x, current.get( 0 ) );
         }
      },

      L( 'L', "LineTo (Abs)", 2 ),
      l( 'l', "LineTo (Rel)", 2 ),

      C( 'C', "CurveTo (Abs)", 6 ),
      c( 'c', "CurveTo (Rel)", 6 ),

      S( 'S', "SmoothCurveTo (Abs)", 4 ),
      s( 's', "SmoothCurveTo (Rel)", 4 ),

      Q( 'Q', "QuadTo (Abs)", 4 ),
      q( 'q', "QuadTo (Rel)", 4 ),

      T( 'T', "SmoothQuadTo (Abs)", 2 ),
      t( 't', "SmoothQuadTo (Rel)", 2 );

      private final boolean isRelative;

      private final char command;

      private final String name;

      private final int itemCount;

      private SvgCommand( final char command, final String name, final int itemCount )
      {
         this.command = command;
         this.name = name;
         this.itemCount = itemCount;
         this.isRelative = ( this.command >= 'a' ) && ( this.command <= 'z' );
      }

      Point2D.Double getPoint( final Point2D.Double last, final List< Double > current )
      {
         // Typically, the new point is the last two values in the list
         final double newX = current.get( current.size() - 2 );
         final double newY = current.get( current.size() - 1 );
         return calculatePoint( last, newX, newY );
      }

      char getCommand()
      {
         return this.command;
      }

      String getName()
      {
         return this.name;
      }

      int getItemCount()
      {
         return this.itemCount;
      }

      boolean isRelative()
      {
         return this.isRelative;
      }

      @Override
      public String toString()
      {
         return getName();
      }

      private Point2D.Double calculatePoint( final Point2D.Double last, final double newX, final double newY )
      {
         if( isRelative() )
         {
            return new Point2D.Double( last.x + newX, last.y + newY );
         }
         return new Point2D.Double( newX, newY );
      }

      static SvgCommand parse( final String token )
      {
         if( token.length() == 1 )
         {
            final char commandChar = token.charAt( 0 );
            for( final SvgCommand svgCommand : SvgCommand.values() )
            {
               if( commandChar == svgCommand.getCommand() )
               {
                  return svgCommand;
               }
            }
         }
         return null;
      }
   }

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
         final DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

         final String parser = XMLResourceDescriptor.getXMLParserClassName();
         final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory( parser );
         // final Document doc = f.createDocument( stream );
         // final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         // final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         // final Document doc = dBuilder.parse( stream );
         final SVGDocument doc = f.createSVGDocument( null, stream );
         doc.normalizeDocument();

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

         final int pointCount = 1000;
         final float totalLength = path.getTotalLength();
         final List< Point2D.Double > points = new ArrayList<>( pointCount );
         for( int i = 0; i < pointCount; i++ )
         {
            final float pos = (float) i / pointCount;
            final SVGPoint p = path.getPointAtLength( pos * totalLength );
            points.add( new Point2D.Double( p.getX(), p.getY() ) );
         }
         // final String data = path.getAttributes().getNamedItem( "d" ).getTextContent();
         // System.out.println( data );
         // final List< Point2D.Double > points = parsePathDef( data );
         final List< Point2D.Double > rescaled = translateAndRescale( points, 300 );
         return rescaled.toArray( new Point2D.Double[ rescaled.size() ] );
      }
      catch( final IOException exception )
      {

      }
      return new Point2D.Double[]
      {
         new Point2D.Double( -100, 100 ),
         new Point2D.Double( 0, -100 ),
         new Point2D.Double( 100, 100 )
      };
   }

   private static SVGOMPathElement getPathElement( final SVGDocument doc, final String pathId )
   {
      final NodeList pathElements = doc.getElementsByTagName( "path" );
      for( int i = 0; i < pathElements.getLength(); i++ )
      {
         final Node pathElement = pathElements.item( i );
         final Node idAttribute = pathElement.getAttributes().getNamedItem( "id" );
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

   private static List< Point2D.Double > parsePathDef( final String pathDef )
   {

      final LinkedList< String > tokens = new LinkedList<>();
      final StringTokenizer tokenizer = new StringTokenizer( pathDef, " ," );
      while( tokenizer.hasMoreTokens() )
      {
         tokens.add( tokenizer.nextToken() );
      }

      final List< Point2D.Double > points = new ArrayList<>();
      SvgCommand lastCommand = SvgCommand.M;
      Point2D.Double lastPos = new Point2D.Double( 0, 0 );
      for( ; !tokens.isEmpty(); )
      {
         final SvgCommand possibleCommand = SvgCommand.parse( tokens.peekFirst() );
         final SvgCommand currentCommand = possibleCommand != null ? possibleCommand : lastCommand;
         if( possibleCommand != null )
         {
            tokens.removeFirst();
         }

         final List< Double > commandValues = readNextN( tokens, currentCommand.getItemCount() );
         final Point2D.Double currentPoint = currentCommand.getPoint( lastPos, commandValues );
         points.add( currentPoint );

         lastCommand = currentCommand;
         lastPos = currentPoint;
      }

      return points;
   }

   private static List< Double > readNextN( final LinkedList< String > tokens, final int count )
   {
      final List< Double > list = new ArrayList<>( count );
      for( int i = 0; i < count; i++ )
      {
         list.add( Double.parseDouble( tokens.removeFirst() ) );
      }
      return list;
   }
}
