package com.ham.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HammingCode {
     //Veri bit sayısı (k) verildiğinde, gerekli parite bit sayısını (m) hesaplar.
    public static int calculateParityBits(int k) {
        int m = 0;
        while (Math.pow(2, m) < (k + m + 1)) {
            m++;
        }
        return m;
    }

     //Veriye Hamming kodunu uygular ve kodlanmış veriyi döndürür.
    public static List<Integer> encode(List<Integer> dataBits) {
        int k = dataBits.size();
        int m = calculateParityBits(k);
        int n = k + m; // Toplam kod kelimesi uzunluğu

        List<Integer> encodedData = new ArrayList<>(Collections.nCopies(n + 1, 0)); // 1-indexed

        // Veri bitlerini yerleştir
        int dataBitIndex = 0;
        for (int i = 1; i <= n; i++) {
            if (!isPowerOfTwo(i)) {
                encodedData.set(i, dataBits.get(dataBitIndex));
                dataBitIndex++;
            }
        }

        // Parite bitlerini hesapla ve yerleştir
        for (int p = 0; p < m; p++) {
            int parityPosition = (int) Math.pow(2, p);
            int parity = 0;
            for (int i = 1; i <= n; i++) {
                if (((i >> p) & 1) == 1 && i != parityPosition) {
                    parity ^= encodedData.get(i);
                }
            }
            encodedData.set(parityPosition, parity);
        }
        return encodedData.subList(1, encodedData.size()); // 0. indeksi atla
    }

    //Kodlanmış veri üzerinde hata olup olmadığını kontrol eder ve sendromu hesaplar.
    public static int calculateSyndrome(List<Integer> receivedData) {
        int n = receivedData.size();
        int m = 0;
        
        while (Math.pow(2, m) < (n + 1)) { 
            m++;
        }

        int syndrome = 0;
        for (int p = 0; p < m; p++) {
            int parityPosition = (int) Math.pow(2, p);
            int parity = 0;
            for (int i = 1; i <= n; i++) {
                if (((i >> p) & 1) == 1) {
                    // Aldığımız verideki bitleri kontrol ediyoruz
                    parity ^= receivedData.get(i - 1); // Listeler 0-indexed, Hamming kodları 1-indexed olduğu için i-1
                }
            }
            if (parity != 0) {
                syndrome |= parityPosition;
            }
        }
        return syndrome;
    }
    
     //Belirtilen konumdaki biti tersine çevirerek yapay hata oluşturur.
    public static List<Integer> introduceError(List<Integer> data, int errorPosition) {
        List<Integer> corruptedData = new ArrayList<>(data);
        if (errorPosition > 0 && errorPosition <= corruptedData.size()) {
            int currentValue = corruptedData.get(errorPosition - 1);
            corruptedData.set(errorPosition - 1, 1 - currentValue);
        }
        return corruptedData;
    }
   
    //Sendrom kelimesine göre hatalı biti düzeltir (eğer tekli hata ise).
    public static List<Integer> correctError(List<Integer> corruptedData, int syndrome) {
        List<Integer> correctedData = new ArrayList<>(corruptedData);
        if (syndrome > 0 && syndrome <= correctedData.size()) { // Tekli hata varsa ve düzeltilebilir konumdaysa
            int currentValue = correctedData.get(syndrome - 1);
            correctedData.set(syndrome - 1, 1 - currentValue);
        }
        return correctedData;
    }

    
     // Bir sayının 2'nin kuvveti olup olmadığını kontrol eder.
    public static boolean isPowerOfTwo(int n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    public static void main(String[] args) {
        // Örnek kullanım:
        List<Integer> data8Bit = new ArrayList<>(List.of(0, 0, 1, 1, 0, 1, 0, 0)); // 8-bit veri
        System.out.println("Orijinal 12-bit Veri: " + data8Bit);

        List<Integer> encoded12Bit = encode(data8Bit);
        System.out.println("Hamming Kodlanmış 12-bit Veri: " + encoded12Bit); // Burada 1-indexed Hamming bitlerini 0-indexed List'e çevirdiğimiz için boyut değişecek.

        // Hata oluşturma (Örn: 5. bit)
        int errorPos = 5; // 1-indexed
        List<Integer> corrupted12Bit = introduceError(encoded12Bit, errorPos);
        System.out.println("Hata Oluşturulmuş Veri (Bit " + errorPos + " hatalı): " + corrupted12Bit);

        int syndrome = calculateSyndrome(corrupted12Bit);
        System.out.println("Sendrom Kelimesi: " + syndrome);

        if (syndrome == 0) {
            System.out.println("Hata yok.");
        } else if (syndrome > 0 && syndrome <= encoded12Bit.size()) {
            System.out.println("Tekli Hata Tespit Edildi, Konum: " + syndrome);
            List<Integer> corrected12Bit = correctError(corrupted12Bit, syndrome);
            System.out.println("Düzeltilmiş Veri: " + corrected12Bit);
            System.out.println("Düzeltme başarılı mı? " + corrected12Bit.equals(encoded12Bit));
        } else {
            System.out.println("Çiftli hata veya tespit edilemeyen hata. (Sendrom = " + syndrome + ")");
        }

        // Çiftli hata senaryosu (Hamming SEC-DED'de tespit edilebilir ama düzeltilemez)
        System.out.println("\n--- Çiftli Hata Senaryosu ---");
        List<Integer> originalForDoubleError = new ArrayList<>(List.of(0, 0, 1, 1, 0, 1, 0, 0));
        List<Integer> encodedForDoubleError = encode(originalForDoubleError);

        List<Integer> corruptedDoubleError = introduceError(encodedForDoubleError, 3); // İlk hata
        corruptedDoubleError = introduceError(corruptedDoubleError, 7); // İkinci hata
        System.out.println("Çift Hata Oluşturulmuş Veri: " + corruptedDoubleError);

        int syndromeDouble = calculateSyndrome(corruptedDoubleError);
        System.out.println("Çift Hata Sendromu: " + syndromeDouble);
        if (syndromeDouble == 0) {
            System.out.println("Hata yok (Yanlış tespit)."); // Çiftli hata durumunda sendrom sıfır olmamalıdır
        } else {
            System.out.println("Hata tespit edildi. Düzeltme denemesi yapılabilir, ancak çiftli hata ise yanlış düzeltme olabilir.");
            if (isPowerOfTwo(syndromeDouble)) { // Sendrom parite bitinin konumuna denk geliyorsa çiftli hata olabilir
                System.out.println("Çiftli hata veya tekli hata parite bitinde.");
            } else { // Sendrom veri bitinin konumuna denk geliyorsa tekli hata olabilir
                System.out.println("Tekli hata olabilir.");
            }
        }
    }
}
