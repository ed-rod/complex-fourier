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
public class FourierElement
{
   private final int frequency;

   private final Complex coefficient;

   private final double magnitude;

   private Complex offset;

   private Complex position;

   private Complex finalPos;

   private double time;

   /**
    * Initializes a new FourierElement object.
    *
    * @param frequency the frequency. Number of rotations per time unit.
    * @param coefficient the complex coefficient for this fourier element that determines its magnitued and intial angle.
    */
   public FourierElement( final int frequency, final Complex coefficient )
   {
      this.frequency = frequency;
      this.coefficient = coefficient;
      this.magnitude = coefficient.magnitude();

      this.offset = new Complex( 0, 0 );
      this.position = new Complex( 0, 0 );
      this.finalPos = new Complex( 0, 0 );
   }

   /**
    * Gets the frequency.
    *
    * @return the frequency.
    */
   public int getFrequency()
   {
      return this.frequency;
   }

   /**
    * Gets the coefficient.
    *
    * @return the coefficient.
    */
   public Complex getCoefficient()
   {
      return this.coefficient;
   }

   /**
    * Gets the magnitude.
    *
    * @return the magnitude.
    */
   public double getMagnitude()
   {
      return this.magnitude;
   }

   /**
    * Gets the offset.
    *
    * @return the offset.
    */
   public Complex getOffset()
   {
      return this.offset;
   }

   /**
    * Sets the offset. Also updates the current position with the updated offset.
    *
    * @param offset the offset to set.
    */
   public final void setOffset( final Complex offset )
   {
      this.offset = offset;
      this.finalPos = this.position.add( this.offset );
   }

   /**
    * Gets the current position.
    *
    * @return the current position.
    */
   public final Complex getPosition()
   {
      return this.finalPos;
   }

   /**
    * Calculates the position of this fourier element based on the time. For every integer increment in the time, this fourier
    * element will complete n cycles where n is its frequency.
    * <p>
    * This calculates the final position by calculating this elements position (for the current time) and then adding the offset
    * from the previous elements final positions. Previous elements are those that have a lower frequency than this one.
    *
    * @param t the time
    * @return the final position on the complex plane for this fourier element. This takes into account an offset (from other
    *         fourier elements with lower frequencies).
    */
   public final Complex updateTime( final double t )
   {
      this.time = t;
      final Complex pos = Complex.ePow2PiT( 0 + ( this.frequency * this.time ) );
      this.position = pos.mul( this.coefficient );
      this.finalPos = this.position.add( this.offset );
      return this.finalPos;
   }

   /**
    * Gets the time.
    *
    * @return the time.
    */
   public double getTime()
   {
      return this.time;
   }
}
