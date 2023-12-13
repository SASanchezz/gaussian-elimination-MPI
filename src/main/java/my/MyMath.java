package my;

import java.math.BigInteger;

public class MyMath {
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        return b.equals(BigInteger.ZERO) ? a : gcd(b, a.mod(b.abs()));
    }
}
