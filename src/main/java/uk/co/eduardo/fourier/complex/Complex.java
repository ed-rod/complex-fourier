/*
 * Copyright (c) PRGX.
 * All Rights Reserved.
 */
package uk.co.eduardo.fourier.complex;

/**
 * Represents a complex number.
 *
 * @author erodri02
 */
public class Complex
{
   /**
    * Real part of number
    */
   public double r;

   /**
    * Imaginary part.
    */
   public double i;

   /**
    * Initializes a new Complex object.
    *
    * @param r the real part
    * @param i the imaginary part.
    */
   public Complex( final double r, final double i )
   {
      this.r = r;
      this.i = i;
   }

   /**
    * Computes e^(2PI * t * i)
    *
    * @param t The time element. Ranges from 0 to 1. At 0, computes e^0i. At 1, computes e^2PIi
    * @return e raised to the ci where c = t * 2 * PI
    */
   public static Complex ePow2PiT( final double t )
   {
      final double c = 2 * Math.PI * t;
      return ePowI( c );
   }

   /**
    * Computes e^(ci)
    *
    * @param c the exponent to C
    * @return The complex number that is equal to e^(ic)
    */
   public static Complex ePowI( final double c )
   {
      // e^(ci) = (cos c) + i(sin c)
      final double r = Math.cos( c );
      final double i = Math.sin( c );
      return new Complex( r, i );
   }

   /**
    * Creates a new complex number as the product of this number and the given number.
    *
    * @param other the other complex number.
    * @return this * other.
    */
   public Complex mul( final Complex other )
   {
      final double newR = ( this.r * other.r ) - ( this.i * other.i );
      final double newI = ( this.r * other.i ) + ( this.i * other.r );
      return new Complex( newR, newI );
   }

   /**
    * Creates a new complex number as the sum of this number and the given other.
    *
    * @param other the other complex number.
    * @return this + other
    */
   public Complex add( final Complex other )
   {
      final double newR = this.r + other.r;
      final double newI = this.i + other.i;
      return new Complex( newR, newI );
   }

   /**
    * Gets the magnitued of the complex number.
    *
    * @return the magnitude.
    */
   public double magnitude()
   {
      return Math.sqrt( ( this.r * this.r ) + ( this.i * this.i ) );
   }
}
