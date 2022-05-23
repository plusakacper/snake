package com.studies.snake;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Snake extends JFrame {
    public Snake() {
        initializeInterface();
    }
    private void initializeInterface() {
        add(new Engine());
        setResizable(false);
        pack();
        setTitle("SNAKE Game");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame jFrame = new Snake();
            jFrame.setVisible(true);
        });
    }
}