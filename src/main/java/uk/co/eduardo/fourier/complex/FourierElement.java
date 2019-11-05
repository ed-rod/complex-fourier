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

   private final double initialAngle;

   private Complex offset;

   private Complex position;

   private Complex finalPos;

   private double time;

   public FourierElement( final int frequency, final Complex coefficient )
   {
      this.frequency = frequency;
      this.coefficient = coefficient;
      this.magnitude = coefficient.magnitude();
      double val = coefficient.r / this.magnitude;
      val = Math.min( Math.max( val, -1 ), 1 );
      this.initialAngle = coefficient.i < 0 ? ( 2 * Math.PI ) - Math.acos( val ) : Math.acos( val );

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
