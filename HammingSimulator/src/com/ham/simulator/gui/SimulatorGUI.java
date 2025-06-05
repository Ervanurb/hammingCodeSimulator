package com.ham.simulator.gui;

import com.ham.simulator.HammingCode;
import com.ham.simulator.Memory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimulatorGUI extends JFrame {

    private JTextField dataInputTextField;
    private JComboBox<String> dataBitLengthComboBox;
    private JTextField errorPositionTextField;
    private JTextArea outputTextArea;

    private Memory memory;
    private List<Integer> currentEncodedData; // Belleğe yazılan en son kodlanmış veri
    private List<Integer> currentOriginalData; // Belleğe yazılan en son orijinal veri

    public SimulatorGUI() {
        setTitle("Hamming SEC-DED Code Simülatörü");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); 

        memory = new Memory();
        currentEncodedData = new ArrayList<>();
        currentOriginalData = new ArrayList<>();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        // --- Kontrol Paneli (Girişler ve Butonlar) ---
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Kontroller"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Veri Girişi
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Veri (İkili Sayı):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dataInputTextField = new JTextField("00110100"); // Örnek varsayılan değer [cite: 1]
        controlPanel.add(dataInputTextField, gbc);

        // Bit Uzunluğu Seçimi
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Veri Bit Uzunluğu:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        dataBitLengthComboBox = new JComboBox<>(new String[]{"8-bit", "16-bit", "32-bit"});
        dataBitLengthComboBox.setSelectedItem("16-bit"); // Varsayılan
        controlPanel.add(dataBitLengthComboBox, gbc);

        // Hata Konumu
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(new JLabel("Hata Konumu (1-indexed):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        errorPositionTextField = new JTextField("1"); // Varsayılan hata konumu
        controlPanel.add(errorPositionTextField, gbc);

        // Butonlar
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JButton encodeAndWriteButton = new JButton("Hamming Kodu Uygula ve Belleğe Yaz");

        encodeAndWriteButton.addActionListener(e -> encodeAndWrite());
        controlPanel.add(encodeAndWriteButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton readFromMemoryButton = new JButton("Bellekten Oku");
        readFromMemoryButton.addActionListener(e -> readFromMemory());
        controlPanel.add(readFromMemoryButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        JButton introduceErrorButton = new JButton("Hata Oluştur"); 
        introduceErrorButton.addActionListener(e -> introduceError());
        controlPanel.add(introduceErrorButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        JButton detectAndCorrectButton = new JButton("Hata Tespit Et ve Düzelt"); 
        detectAndCorrectButton.addActionListener(e -> detectAndCorrect());
        controlPanel.add(detectAndCorrectButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        JButton clearOutputButton = new JButton("Çıktı Alanını Temizle");
        clearOutputButton.addActionListener(e -> outputTextArea.setText(""));
        controlPanel.add(clearOutputButton, gbc);


        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // --- Çıktı Paneli ---
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Simülasyon Çıktısı"));
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(outputPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void encodeAndWrite() {
        String dataString = dataInputTextField.getText();
        if (!dataString.matches("[01]+")) {
            JOptionPane.showMessageDialog(this, "Lütfen sadece '0' ve '1' içeren ikili veri girin.", "Geçersiz Giriş", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedBitLength = Integer.parseInt(((String) dataBitLengthComboBox.getSelectedItem()).replace("-bit", ""));
        if (dataString.length() != selectedBitLength) {
            JOptionPane.showMessageDialog(this, "Girilen veri uzunluğu seçilen bit uzunluğu ile uyuşmuyor (" + selectedBitLength + " bit bekleniyor).", "Uzunluk Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Integer> dataBits = dataString.chars()
                .map(c -> c - '0')
                .boxed()
                .collect(Collectors.toList());

        currentOriginalData = new ArrayList<>(dataBits); 
        
        try {
            List<Integer> encoded = HammingCode.encode(dataBits);
            memory.write(encoded);
            currentEncodedData = new ArrayList<>(encoded); 

            outputTextArea.append("Orijinal Veri (" + selectedBitLength + " bit): " + formatBitList(dataBits) + "\n");
            outputTextArea.append("Hamming Kodlanmış Veri (" + encoded.size() + " bit): " + formatBitList(encoded) + "\n\n");
        } catch (Exception ex) {
            outputTextArea.append("Hata: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private void readFromMemory() {
        if (memory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bellekte okunacak veri yok. Lütfen önce veri yazın.", "Bellek Boş", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Integer> readData = memory.read();
        outputTextArea.append("Bellekten Okunan Veri: " + formatBitList(readData) + "\n\n");
    }

    private void introduceError() {
        if (currentEncodedData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hata oluşturmak için önce Hamming kodu uygulayıp belleğe veri yazmalısınız.", "Veri Yok", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int errorPosition;
        try {
            errorPosition = Integer.parseInt(errorPositionTextField.getText());
            if (errorPosition <= 0 || errorPosition > currentEncodedData.size()) {
                JOptionPane.showMessageDialog(this, "Hata konumu 1 ile " + currentEncodedData.size() + " arasında olmalıdır.", "Geçersiz Konum", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir hata konumu (sayı) girin.", "Geçersiz Giriş", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Bellekten oku, hatayı ekle ve tekrar belleğe yaz (simülasyon için)
        List<Integer> dataToCorrupt = new ArrayList<>(currentEncodedData); 
        List<Integer> corruptedData = HammingCode.introduceError(dataToCorrupt, errorPosition);
        memory.write(corruptedData); 

        outputTextArea.append("Yapay Hata Oluşturuldu (Konum " + errorPosition + "): " + formatBitList(corruptedData) + "\n\n");
    }

    private void detectAndCorrect() {
        if (memory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bellekte kontrol edilecek veri yok. Lütfen önce veri yazın.", "Bellek Boş", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Integer> dataToProcess = memory.read();
        outputTextArea.append("Kontrol Edilen Veri: " + formatBitList(dataToProcess) + "\n");

        int syndrome = HammingCode.calculateSyndrome(dataToProcess);
        outputTextArea.append("Sendrom Kelimesi: " + syndrome + "\n");

        if (syndrome == 0) {
            outputTextArea.append(">> Hata yok.\n");
        } else {
            if (HammingCode.isPowerOfTwo(syndrome)) { 
                outputTextArea.append(">> Çiftli hata tespit edildi (Konum: " + syndrome + "). Düzeltilemiyor.\n");
            } else {
                outputTextArea.append(">> Tekli hata tespit edildi, Konum: " + syndrome + "\n");
                List<Integer> correctedData = HammingCode.correctError(dataToProcess, syndrome);
                outputTextArea.append(">> Düzeltilmiş Veri: " + formatBitList(correctedData) + "\n");
                outputTextArea.append(">> Orijinal ile Düzeltilmiş Eşleşiyor mu? " + (correctedData.equals(currentEncodedData) ? "Evet" : "Hayır") + "\n");
                 
                memory.write(correctedData);
            }
        }
        outputTextArea.append("\n");
    }


    // Yardımcı metot: Bit listesini okunabilir stringe dönüştürür.
    private String formatBitList(List<Integer> bits) {
        return bits.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimulatorGUI().setVisible(true);
        });
    }
}