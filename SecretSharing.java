import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SecretSharing {

    private static BigInteger decode(String base, String value) {
        try {
            return new BigInteger(value, Integer.parseInt(base));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unsupported base: " + base);
        }
    }

    private static BigInteger lagrange(List<Pair<Integer, BigInteger>> points, BigInteger prime) {
        BigInteger secret = BigInteger.ZERO;
        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = BigInteger.valueOf(points.get(i).getKey());
            BigInteger yi = points.get(i).getValue();
            BigInteger li = BigInteger.ONE;
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    BigInteger xj = BigInteger.valueOf(points.get(j).getKey());
                    li = li.multiply(xj).multiply(xj.subtract(xi).modInverse(prime)).mod(prime);
                }
            }
            secret = secret.add(yi.multiply(li)).mod(prime);
        }
        return secret;
    }

    private static BigInteger findPrime(List<Pair<Integer, BigInteger>> points) {
        BigInteger max = BigInteger.ZERO;
        for (Pair<Integer, BigInteger> p : points) {
            max = max.max(p.getValue());
        }
        BigInteger prime = max.multiply(BigInteger.valueOf(2));
        while (!prime.isProbablePrime(100)) {
            prime = prime.add(BigInteger.ONE);
        }
        return prime;
    }

    public static void main(String[] args) throws IOException {
        String[] files = {"testcase1.json", "testcase2.json"};

        for (String file : files) {
            System.out.println("Processing: " + file);

            try (FileReader fr = new FileReader(file)) {
                JSONObject json = new JSONObject(new JSONTokener(fr));
                int k = json.getJSONObject("keys").getInt("k");
                List<Pair<Integer, BigInteger>> points = new ArrayList<>();

                for (String key : json.keySet()) {
                    if (!key.equals("keys")) {
                        JSONObject p = json.getJSONObject(key);
                        points.add(new Pair<>(Integer.parseInt(key), decode(p.getString("base"), p.getString("value"))));
                    }
                }

                if (points.size() > k) {
                    points = points.subList(0, k);
                }

                BigInteger prime = findPrime(points);
                BigInteger secret = lagrange(points, prime);
                System.out.println("Secret for " + file + ": " + secret);
                System.out.println("--------------------");
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Error in " + file + ": " + e.getMessage());
            }
        }
    }

    private static class Pair<K, V> {
        final K key;
        final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}