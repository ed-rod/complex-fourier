/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * TODO Insert description sentence here.
 *
 * @author erodri02
 */
public class Tester
{
   private static final int INCREMENT = 10;

   private static final int SECONDS_PER_CYCLE = 6; //

   public static void main( final String[] args )
   {
      SwingUtilities.invokeLater( () -> start() );
   }

   private static void start()
   {
      initializeLaf();
      final List< FourierElement > elements = new ArrayList<>();
      final PathDef path = createPathDefinition();
      final FourierGenerator generator = new FourierGenerator( 1 );
      setElements( elements, path, generator );

      updateElements( elements, 0 );

      final JPanel panel = createFourierPanel( path, elements );
      final JFrame frame = createFrame( panel );

      final AtomicLong t = new AtomicLong( 0 );
      final AtomicInteger frequency = new AtomicInteger( 1 );

      final Timer timer = new Timer( INCREMENT, new ActionListener()
      {
         @Override
         public void actionPerformed( final ActionEvent evt )
         {
            final double lastTime = convertTime( t.get() - INCREMENT );
            final double time = convertTime( t.get() );

            // Check to see if we've completed another cycle
            if( Math.floor( time ) > Math.floor( lastTime ) )
            {
               final int f = frequency.getAndIncrement();
               System.out.printf( "f=%d  f2=%f\n", f, Math.pow( 1.2, f ) );
               int frequencies = (int) Math.floor( Math.pow( 1.2, f ) );
               frequencies = Math.max( frequencies, f );
               final FourierGenerator newGenerator = new FourierGenerator( frequencies );
               setElements( elements, path, newGenerator );
            }

            // Update the elements for the current time
            updateElements( elements, time );

            panel.invalidate();
            panel.revalidate();
            panel.repaint();
            t.addAndGet( INCREMENT );
         }

         private double convertTime( final long timeInMillis )
         {
            return timeInMillis / ( SECONDS_PER_CYCLE * 1_000.0 );
         }
      } );
      timer.start();

      frame.addWindowListener( new WindowAdapter()
      {
         @Override
         public void windowClosing( final WindowEvent e )
         {
            timer.stop();
         }
      } );
   }

   private static void updateElements( final List< FourierElement > elements, final double time )
   {
      for( final FourierElement element : elements )
      {
         element.updateTime( time );
      }
   }

   private static JFrame createFrame( final JComponent panel )
   {
      final JFrame frame = new JFrame( "Complex Fourier" );
      frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
      frame.pack();
      frame.setSize( 500, 500 );
      frame.setLocationRelativeTo( null );
      frame.setVisible( true );

      frame.setContentPane( panel );

      return frame;
   }

   private static JPanel createFourierPanel( final PathDef path, final List< FourierElement > elements )
   {
      final JPanel panel = new JPanel( new BorderLayout() );
      panel.add( new FourierWidget( path, elements ) );
      return panel;
   }

   private static void setElements( final List< FourierElement > elements, final PathDef path, final FourierGenerator generator )
   {
      elements.clear();
      elements.addAll( generator.generateElements( path ) );
   }

   private static PathDef createPathDefinition()
   {
      final SvgPathDef svgPathDef = new SvgPathDef( "nail_gear.svg", "path16" );
      return svgPathDef;

      // return new DirectPathDef( new Point2D.Double( 0, -400 ),
      // new Point2D.Double( 400, 0 ),
      // new Point2D.Double( 0, 400 ),
      // new Point2D.Double( -400, 0 ) );
   }

   private static void initializeLaf()
   {
      try
      {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      }
      catch( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException exception )
      {
         // Ignore and use default Look and Feel
      }
   }

   private static final class LineSegment
   {
      private final Line2D.Double line;

      private final double age;

      private LineSegment( final Double line, final double age )
      {
         this.line = line;
         this.age = age;
      }
   }

   private static final class Trail
   {
      private static final Color SHAPE_C = new Color( 255, 200, 0, 255 );

      private static final Stroke SHAPE_S = new BasicStroke( 2f );

      private final Deque< LineSegment > lines = new LinkedList<>();

      private Point2D.Double firstPoint;

      private void add( final Point2D.Double point, final double age )
      {
         // If we don't have any lines yet, we start one off
         if( this.lines.size() == 0 )
         {
            if( this.firstPoint == null )
            {
               this.firstPoint = point;
            }
            else
            {
               this.lines.add( new LineSegment( new Line2D.Double( this.firstPoint, point ), age ) );
            }
         }
         else
         {
            final Point2D.Double lastPoint = (Point2D.Double) this.lines.getLast().line.getP2();
            this.lines.add( new LineSegment( new Line2D.Double( lastPoint, point ), age ) );
         }
      }

      private void prune( final double currentTime )
      {
         // Remove all segments that are more than one time unit old
         final Iterator< LineSegment > it = this.lines.iterator();
         boolean older = true;
         while( it.hasNext() && older )
         {
            final LineSegment line = it.next();
            final double diff = currentTime - line.age;
            older = diff >= 1;
            if( older )
            {
               it.remove();
            }
         }
      }

      private void draw( final Graphics2D g2d, final double currentTime )
      {
         for( final LineSegment line : this.lines )
         {
            final double diff = currentTime - line.age;
            final int alpha = 255 - (int) ( 255 * diff );
            final Color color = new Color( SHAPE_C.getRed(), SHAPE_C.getGreen(), SHAPE_C.getBlue(), alpha );

            g2d.setStroke( SHAPE_S );
            g2d.setColor( color );
            g2d.draw( line.line );
         }
      }
   }

   private static final class FourierWidget extends JComponent
   {
      private static final Color SHAPE_C = new Color( 200, 200, 200, 128 );

      private static final Stroke SHAPE_S = new BasicStroke( 1f );

      private static final Color CIRCLE_C = new Color( 0, 255, 128, 128 );

      private static final Stroke CIRCLE_S = new BasicStroke( 1f );

      private static final Color LINE_C = new Color( 255, 255, 255, 255 );

      private static final Stroke LINE_S = new BasicStroke( 1.5f );

      private Font displayFont;

      private final PathDef path;

      private final List< FourierElement > elements;

      private final Trail trail = new Trail();

      private FourierWidget( final PathDef path, final List< FourierElement > elements )
      {
         this.path = path;
         this.elements = elements;
      }

      @Override
      protected void paintComponent( final Graphics g )
      {
         if( this.displayFont == null )
         {
            this.displayFont = getFont().deriveFont( 36f ).deriveFont( Font.BOLD );
            setFont( this.displayFont );
         }
         final Graphics2D g2d = (Graphics2D) g;
         final int width = getWidth();
         final int height = getHeight();
         g2d.setColor( Color.BLACK );
         g2d.fillRect( -width, -height, width * 2, height * 2 );
         g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
         g2d.translate( width / 2, height / 2 );

         // Draw the counter.
         final int frequencies = this.elements.size() / 2;
         g2d.setColor( Color.WHITE );
         g2d.drawString( Integer.toString( frequencies ), -( width / 2 ) + 30, ( height / 2 ) - 30 );

         // Draw the shape
         g2d.setColor( SHAPE_C );
         g2d.setStroke( SHAPE_S );
         g2d.draw( this.path.getShape() );

         Complex lastOffset = new Complex( 0, 0 );
         for( final FourierElement element : this.elements )
         {
            element.setOffset( lastOffset );
            final Complex pos = element.getPosition();

            // Draw this element
            final double centreX = element.getOffset().r;
            final double centreY = element.getOffset().i;
            final double mag = element.getMagnitude();

            final double posX = element.getPosition().r;
            final double posY = element.getPosition().i;

            final Ellipse2D.Double ellipse = new Ellipse2D.Double( centreX - mag, centreY - mag, 2 * mag, 2 * mag );
            g2d.setStroke( CIRCLE_S );
            g2d.setColor( CIRCLE_C );
            g2d.draw( ellipse );

            final Line2D.Double line = new Line2D.Double( centreX, centreY, posX, posY );
            g2d.setStroke( LINE_S );
            g2d.setColor( LINE_C );
            g2d.draw( line );

            lastOffset = pos;
         }
         final double currentTime = getTime();
         this.trail.add( new Point2D.Double( lastOffset.r, lastOffset.i ), currentTime );
         this.trail.prune( currentTime );
         this.trail.draw( g2d, currentTime );
      }

      private double getTime()
      {
         if( this.elements.isEmpty() )
         {
            return 0;
         }
         return this.elements.get( 0 ).getTime();
      }
   }
}
