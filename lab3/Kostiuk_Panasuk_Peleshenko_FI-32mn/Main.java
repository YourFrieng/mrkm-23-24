import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;


public class Main {
    static public ArrayList<BigInteger> findPrimeDivisors(BigInteger num){
        ArrayList<BigInteger> divisors = new ArrayList<>();
        BigInteger limit = num;
        BigInteger counter = BigInteger.TWO;
        while(counter.compareTo(limit) <= 0)
        {
            if (limit.mod(counter).compareTo(BigInteger.ZERO) == 0){
                divisors.add(counter);
                while(limit.mod(counter).compareTo(BigInteger.ZERO) == 0)
                {
                    limit = limit.divide(counter); // shorten the loop
                }
            }
            counter = counter.add(BigInteger.ONE);
        }
        return divisors;
    }

    public static String generateBlumBlumBits(BigInteger r1, int bits) {
        StringBuilder blumBits = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        BigInteger P = new BigInteger("D5BBB96D30086EC484EBA3D7F9CAEB07", 16);
        BigInteger Q = new BigInteger("425D2B9BFDB25B9CF6C416CC6E37B59C1F", 16);
        BigInteger n = P.multiply(Q);
        BigInteger two = new BigInteger("2");
        BigInteger r2;
        while (blumBits.length() != bits) {
            r2 = r1.modPow(two, n);
            String last = r2.toString(2);
            temp.append(last.charAt(last.length() - 1));
            if (temp.length() == 8) {
                blumBits.append(temp);
                temp = new StringBuilder();
            }
            r1 = r2;
        }
        return blumBits.toString();
    }

    public static BigInteger power(BigInteger N, BigInteger ecsponent) {
        BigInteger staticN = N;
        String bitPow = ecsponent.toString(2);
        for (int i = 0; i < bitPow.length(); i++) {
            N = N.pow(2);
            if (bitPow.charAt(i) == '1') {
                N = N.multiply(staticN);
            }
        }
        return N;
    }
    public static BigInteger ithRoot(BigInteger N, BigInteger K) {
        BigInteger K1 = K.subtract(BigInteger.ONE);
        BigInteger S = N.add(BigInteger.ONE);
        BigInteger U = N;
        while (U.compareTo(S) < 0) {
            S = U;
            U = power(U, K1);
            N = N.divide(U);
            BigInteger Utemp = U.multiply(K1);
            Utemp = Utemp.add(U);
            U = Utemp.divide(K);
        }
        return S;
    }
    private static void generateSchnorrKey(BigInteger r1) {
        boolean div = false;
        BigInteger p = BigInteger.ONE;
        BigInteger q1 = BigInteger.ONE;
        BigInteger q2 = BigInteger.ONE;
        BigInteger g = BigInteger.ONE;
        while (!div) {
            String a = generateBlumBlumBits(r1, 24);
            q1 = new BigInteger(a, 2);
            if (q1.isProbablePrime(100)) div = true;
            r1 = r1.add(BigInteger.ONE);
        }
        while (div) {
            String a = generateBlumBlumBits(r1, 24);
            q2 = new BigInteger(a, 2);
            p = (q1.multiply(q2)).add(BigInteger.ONE);
            if (p.isProbablePrime(100)) div = false;
            r1 = r1.add(BigInteger.ONE);
        }
        BigInteger gTemp = BigInteger.ONE;
        while (gTemp.equals(BigInteger.ONE)) {
            gTemp = ithRoot(p.add(BigInteger.ONE), q1);
            p = (p.pow(2)).add(BigInteger.ONE);
        }
        g = gTemp;
    }

    public static void main(String[] args) {
        BigInteger m = new BigInteger("AAAAFFFFDDDD", 16);

        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(
                new BigInteger("10001", 16), new SecureRandom(), 2048,
                80));

        AsymmetricCipherKeyPair BobKeyPair = generator.generateKeyPair();
        RSAKeyParameters BobPubKey = (RSAKeyParameters)BobKeyPair.getPublic();//Bob has this key
        RSAPrivateCrtKeyParameters BobPrivKey = (RSAPrivateCrtKeyParameters)BobKeyPair.getPrivate();

        //ALICE Part
        BigInteger r = new BigInteger("AFFAA", 16);
        for (; r.compareTo(BobPubKey.getModulus().divide(BigInteger.TWO)) < 0; r = r.add(BigInteger.ONE)) {
            BigInteger gcdRes = BobPubKey.getModulus().gcd(r);
            if (BigInteger.ONE.equals(gcdRes))
                break;
        }
        BigInteger m_stroke = (m.multiply(r.modPow(BobPubKey.getExponent(), BobPubKey.getModulus()))).mod(BobPubKey.getModulus());

        //BOB Part
        BigInteger sign_stroke = m_stroke.modPow(BobPrivKey.getExponent(), BobPubKey.getModulus());

        //ALICE Part
        BigInteger sign = sign_stroke.multiply(r.modInverse(BobPubKey.getModulus())).mod(BobPubKey.getModulus());

        //BOB verify
        System.out.println("Verify sign:");
//        BigInteger mPowD = m.modPow(BobPrivKey.getExponent(), BobPubKey.getModulus());
//        System.out.println(sign.equals(mPowD));
        BigInteger unsignedMessage = sign.modPow(BobPubKey.getExponent(), BobPubKey.getModulus());
        System.out.println(m.equals(unsignedMessage));
        

        generateSchnorrKey(new BigInteger("AFFAA", 16));
    }
}