package com.ham.simulator;

import com.ham.simulator.gui.SimulatorGUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimulatorGUI().setVisible(true);
        });
    }
}