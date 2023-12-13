package my;

import java.io.Serializable;
import java.math.BigInteger;

public class Fraction implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigInteger numerator;
    private BigInteger denominator;

    public Fraction(BigInteger numerator, BigInteger denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
    public Fraction(double number) {
        String[] numberParts = String.valueOf(number).split("\\.");
        BigInteger numerator = new BigInteger(numberParts[0] + numberParts[1]);
        BigInteger denominator = new BigInteger("1" + "0".repeat(numberParts[1].length()-1));
        this.numerator = numerator;
        this.denominator = denominator;
    }
    public Fraction(int numerator) {
        this.numerator = BigInteger.valueOf(numerator);
        this.denominator = BigInteger.ONE;
    }
    public Fraction(BigInteger numerator) {
        this.numerator = numerator;
        this.denominator = BigInteger.ONE;
    }
    public Fraction() {
        this.numerator = BigInteger.ZERO;
        this.denominator = BigInteger.ONE;
    }

    public Fraction add(Fraction other) {
        BigInteger newNumerator = (this.numerator.multiply(other.denominator)).add(other.numerator.multiply(this.denominator));
        BigInteger newDenominator = this.denominator.multiply(other.denominator);
        return new Fraction(newNumerator, newDenominator).shorten();
    }

    public Fraction subtract(Fraction other) {
        BigInteger newNumerator = (this.numerator.multiply(other.denominator)).subtract(other.numerator.multiply(this.denominator));
        BigInteger newDenominator = this.denominator.multiply(other.denominator);
        return new Fraction(newNumerator, newDenominator).shorten();
    }

    public Fraction multiply(Fraction other) {
        BigInteger newNumerator = this.numerator.multiply(other.numerator);
        BigInteger newDenominator = this.denominator.multiply(other.denominator);
        return new Fraction(newNumerator, newDenominator).shorten();
    }

    public Fraction divide(Fraction other) {
        BigInteger newNumerator = this.numerator.multiply(other.denominator);
        BigInteger newDenominator = this.denominator.multiply(other.numerator);
        return new Fraction(newNumerator, newDenominator).shorten();
    }

    public Fraction add(int other) {
        return this.add(new Fraction(other));
    }
    public Fraction subtract(int other) {
        return this.subtract(new Fraction(other));
    }
    public Fraction multiply(int other) {
        return this.multiply(new Fraction(other));
    }
    public Fraction divide(int other) {
        return this.divide(new Fraction(other));
    }

    public Fraction shorten() {
        BigInteger gcd = MyMath.gcd(this.numerator, this.denominator);
        if (gcd.equals(BigInteger.ZERO)) {
            return new Fraction(BigInteger.ZERO, BigInteger.ONE);
        }
        return new Fraction(this.numerator.divide(gcd), this.denominator.divide(gcd));
    }

    public Fraction negate() {
        return new Fraction(this.numerator.negate(), this.denominator);
    }

    public Fraction abs() {
        return new Fraction(this.numerator.abs(), this.denominator.abs());
    }

    public boolean equals(Fraction other) {
        return this.numerator.equals(other.numerator) && this.denominator.equals(other.denominator);
    }

    @Override
    public String toString() {
        if (this.denominator.equals(BigInteger.ONE)) {
            return String.valueOf(this.numerator);
        }
        return this.numerator + "/" + this.denominator;
    }
}
