import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

    public static void main(String[] args) {
        try {
            // Parse input from JSON file
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("testcase.json"));
            
            // Extract keys n and k
            JSONObject keys = (JSONObject) jsonObject.get("keys");
            int n = Integer.parseInt(keys.get("n").toString());
            int k = Integer.parseInt(keys.get("k").toString());

            // Parse points from the JSON file
            Map<Integer, BigInteger> points = new HashMap<>();
            for (int i = 1; i <= n; i++) {
                String index = String.valueOf(i);
                if (jsonObject.containsKey(index)) {
                    JSONObject point = (JSONObject) jsonObject.get(index);
                    int x = i;
                    int base = Integer.parseInt(point.get("base").toString());
                    String value = point.get("value").toString();
                    BigInteger y = new BigInteger(value, base);  // Decode the value based on the base
                    points.put(x, y);  // (x, y) pairs
                }
            }

            // Find the constant term (c) using Lagrange Interpolation
            BigInteger secret = lagrangeInterpolation(points, k);
            System.out.println("Constant term (secret): " + secret);
            
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // Lagrange interpolation to find the secret 'c'
    public static BigInteger lagrangeInterpolation(Map<Integer, BigInteger> points, int k) {
        BigInteger result = BigInteger.ZERO;
        BigInteger primeModulus = BigInteger.valueOf(1000000007);  // A large prime modulus
        
        for (Integer i : points.keySet()) {
            BigInteger xi = BigInteger.valueOf(i);
            BigInteger yi = points.get(i);
            
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (Integer j : points.keySet()) {
                if (!i.equals(j)) {
                    BigInteger xj = BigInteger.valueOf(j);
                    
                    numerator = numerator.multiply(xj.negate()).mod(primeModulus);
                    denominator = denominator.multiply(xi.subtract(xj)).mod(primeModulus);
                }
            }

            // Compute the modular inverse of the denominator
            BigInteger denominatorInverse;
            try {
                denominatorInverse = denominator.modInverse(primeModulus);
            } catch (ArithmeticException e) {
                System.out.println("Denominator not invertible for xi = " + xi);
                return BigInteger.ZERO;  // Handle case if denominator is not invertible
            }

            BigInteger term = yi.multiply(numerator).mod(primeModulus)
                                .multiply(denominatorInverse).mod(primeModulus);
            
            result = result.add(term).mod(primeModulus);
        }
        
        return result;
    }
}
