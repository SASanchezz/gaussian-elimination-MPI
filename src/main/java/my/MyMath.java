package my;

import java.math.BigInteger;

public class MyMath {
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        return b.equals(BigInteger.ZERO) ? a : gcd(b, a.mod(b.abs()));
    }

    public static int[] reverse(int[] arr) {
        int[] reversed = new int[arr.length];
        for (int i=arr.length-1; i>=0; i--) {
            reversed[arr.length-1-i] = arr[i];
        }
        return reversed;
    }
}
