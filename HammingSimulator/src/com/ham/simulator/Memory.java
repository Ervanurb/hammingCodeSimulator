package com.ham.simulator;

import java.util.ArrayList;
import java.util.List;

public class Memory {
    private List<Integer> storedData; // Bellekte saklanan veri (Hamming kodlanmış)
    public Memory() {
        this.storedData = new ArrayList<>();
    }
    
    public void write(List<Integer> data) {
        this.storedData = new ArrayList<>(data); // Yeni bir kopya oluşturarak yaz
        System.out.println("Belleğe yazıldı: " + storedData);
    }
    
    public List<Integer> read() {
        List<Integer> readCopy = new ArrayList<>(storedData);
        System.out.println("Bellekten okundu: " + readCopy);
        return readCopy;
    }
    
    public boolean isEmpty() {
        return storedData.isEmpty();
    }
   
    public void clear() {
        this.storedData.clear();
        System.out.println("Bellek temizlendi.");
    }
}