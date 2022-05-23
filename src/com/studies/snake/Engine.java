package com.studies.snake;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.sound.sampled.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Engine extends JPanel implements ActionListener {
    private final int BOARD_WIDTH = 300;
    private final int BOARD_HEIGHT = 300;
    private final int ELEMENT_SIZE = 10;
    private final int MAX_ELEMENTS = 900;
    private final int RANDOM_POSITION = 29;
    private final int NO_OF_STONES = 3;

    private final int[] snakeX = new int[MAX_ELEMENTS];
    private final int[] snakeY = new int[MAX_ELEMENTS];
    private final int[] stoneX = new int[NO_OF_STONES];
    private final int[] stoneY = new int[NO_OF_STONES];

    private int elements;
    private int hits;
    private int moves;
    private int meatX;
    private int meatY;
    private long start;
    private long end;
    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true;
    private boolean ifWall = false;
    private boolean ifStone = false;
    private boolean ifSnake = false;
    private Timer timer;
    private Image body;
    private Image meat;
    private Image head;
    private Image background;
    private Image stone;
    private LocalDate myDate;
    private LocalTime myTime;

    public Engine() {
        initializeBoard();
    }

    private void initializeBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        loadImages();
        initializeGame();
    }

    private void loadImages() {
        ImageIcon backgroundImageIcon = new ImageIcon("resources/images/background.jpg");
        background = backgroundImageIcon.getImage();
        ImageIcon bodyImageIcon = new ImageIcon("resources/images/body.png");
        body = bodyImageIcon.getImage();
        ImageIcon headImageIcon = new ImageIcon("resources/images/head.png");
        head = headImageIcon.getImage();
        ImageIcon meatImageIcon = new ImageIcon("resources/images/meat.png");
        meat = meatImageIcon.getImage();
        ImageIcon stoneImageIcon = new ImageIcon("resources/images/stone.png");
        stone = stoneImageIcon.getImage();
    }

    private void initializeGame() {
        final int GAMEPLAY_DELAY = 120;
        elements = 4;
        hits = 0;
        moves = 0;
        for (int i = 0; i < elements; i++) {
            snakeX[i] = 50 - i * 10;
            snakeY[i] = 50;
        }
        locateMeat();
        locateStones();
        timer = new Timer(GAMEPLAY_DELAY, this);
        timer.start();
        start = System.currentTimeMillis();
        myDate = LocalDate.now();
        myTime = LocalTime.now();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            doDrawing(g);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doDrawing(Graphics g) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        g.drawImage(background, 0, 0, null);
        if (inGame) {
            g.drawImage(meat, meatX, meatY, this);
            for (int i = 0; i < NO_OF_STONES; i++) {
                g.drawImage(stone, stoneX[i], stoneY[i], this);
            }
            for (int i = 0; i < elements; i++) {
                if (i == 0) {
                    g.drawImage(head, snakeX[i], snakeY[i], this);
                } else {
                    g.drawImage(body, snakeX[i], snakeY[i], this);
                }
            }
            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }
    }

    private void saveResults() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        String formattedDate = myDate.format(dateFormat);
        String formattedTime = myTime.format(timeFormat);
        double total = (double)(end - start) / 1000;
        try {
            FileWriter myWriter = new FileWriter("results.txt");
            myWriter.write("\"SNAKE\" Game\nDate: " + formattedDate + ", " + formattedTime +
                    "\n\nSummary:\nGame ended in " + total + "s\nYou died hitting ");
            if (ifWall) {
                myWriter.write("a wall");
            } else if (ifStone) {
                myWriter.write("an obstacle");
            } else if (ifSnake) {
                myWriter.write("yourself");
            }
            myWriter.write("\nScored points: " + hits + "\nMaximum length of your snake: " + elements +
                    "\nTotal number of moves: " + moves);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playAudio(String pathName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File file = new File(pathName);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();
    }

    private void gameOver(Graphics g) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        playAudio("resources/sounds/gameOver.wav");
        String gameOverMessage = "Game Over";
        String scoreMessage = "Your score has been saved to a file.";
        Font gameOverFont = new Font("Helvetica", Font.BOLD, 15);
        Font scoreFont = new Font("Helvetica", Font.BOLD, 12);
        FontMetrics fontMetrics = getFontMetrics(gameOverFont);
        g.setColor(new Color(26, 13, 0));
        g.setFont(gameOverFont);
        g.drawString(gameOverMessage, (BOARD_WIDTH - fontMetrics.stringWidth(gameOverMessage)) / 2, BOARD_HEIGHT / 2);
        g.setFont(scoreFont);
        g.drawString(scoreMessage,((BOARD_WIDTH - fontMetrics.stringWidth(gameOverMessage)) / 2) - 55, (BOARD_HEIGHT / 2) + 20);
        saveResults();
    }

    private void checkMeat() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if ((snakeX[0] == meatX) && (snakeY[0] == meatY)) {
            playAudio("resources/sounds/eaten.wav");
            elements++;
            hits += 1;
            locateMeat();
        }
    }

    private void move() {
        for (int i = elements; i > 0; i--) {
            snakeX[i] = snakeX[(i - 1)];
            snakeY[i] = snakeY[(i - 1)];
        }
        if (leftDirection) {
            snakeX[0] -= ELEMENT_SIZE;
        }
        if (rightDirection) {
            snakeX[0] += ELEMENT_SIZE;
        }
        if (upDirection) {
            snakeY[0] -= ELEMENT_SIZE;
        }
        if (downDirection) {
            snakeY[0] += ELEMENT_SIZE;
        }
    }

    private void checkCollision() {
        for (int i = elements; i > 0; i--) {
            if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                inGame = false;
                ifSnake = true;
                break;
            }
        }
        if (snakeY[0] >= BOARD_HEIGHT) {
            inGame = false;
            ifWall = true;
        }
        if (snakeY[0] < 0) {
            inGame = false;
            ifWall = true;
        }
        if (snakeX[0] >= BOARD_WIDTH) {
            inGame = false;
            ifWall = true;
        }
        if (snakeX[0] < 0) {
            inGame = false;
            ifWall = true;
        }
        if ((snakeX[0] == stoneX[0]) && (snakeY[0] == stoneY[0])) {
            inGame = false;
            ifStone = true;
        }
        if ((snakeX[0] == stoneX[1]) && (snakeY[0] == stoneY[1])) {
            inGame = false;
            ifStone = true;
        }
        if ((snakeX[0] == stoneX[2]) && (snakeY[0] == stoneY[2])) {
            inGame = false;
            ifStone = true;
        }
        if (!inGame) {
            timer.stop();
            end = System.currentTimeMillis();
        }
    }

    private void locateMeat() {
        int randomNumber = (int) (Math.random() * RANDOM_POSITION);
        meatX = ((randomNumber * ELEMENT_SIZE));
        randomNumber = (int) (Math.random() * RANDOM_POSITION);
        meatY = ((randomNumber * ELEMENT_SIZE));
    }

    private void locateStones() {
        for (int i = 0; i < NO_OF_STONES; i++) {
            stoneX[i] = 0;
            stoneY[i] = 0;
        }
        do {
            int randomNumber = (int) (Math.random() * RANDOM_POSITION);
            int randomPosition = randomNumber * ELEMENT_SIZE;
            if (meatX != randomPosition) {
                stoneX[0] = randomPosition;
            }
            randomNumber = (int) (Math.random() * RANDOM_POSITION);
            randomPosition = randomNumber * ELEMENT_SIZE;
            if (meatY != randomPosition) {
                stoneY[0] = randomPosition;
            }
        } while (stoneX[0] == 0 && stoneY[0] == 0);

        do {
            int randomNumber = (int) (Math.random() * RANDOM_POSITION);
            int randomPosition = randomNumber * ELEMENT_SIZE;
            if (meatX != randomPosition && stoneX[0] != randomPosition) {
                stoneX[1] = randomPosition;
            }
            randomNumber = (int) (Math.random() * RANDOM_POSITION);
            randomPosition = randomNumber * ELEMENT_SIZE;
            if (meatY != randomPosition && stoneY[0] != randomPosition) {
                stoneY[1] = randomPosition;
            }
        } while (stoneX[1] == 0 && stoneY[1] == 0);

        do {
            int randomNumber = (int) (Math.random() * RANDOM_POSITION);
            int randomPosition = randomNumber * ELEMENT_SIZE;
            if (meatX != randomPosition && stoneX[0] != randomPosition
                    && stoneX[1] != randomPosition) {
                stoneX[2] = randomPosition;
            }
            randomNumber = (int) (Math.random() * RANDOM_POSITION);
            randomPosition = randomNumber * ELEMENT_SIZE;
            if (meatY != randomPosition && stoneY[0] != randomPosition
                    && stoneY[1] != randomPosition) {
                stoneY[2] = randomPosition;
            }
        } while (stoneX[2] == 0 && stoneY[2] == 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            try {
                checkMeat();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
            checkCollision();
            move();
        }
        repaint();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
                moves += 1;
            }
            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
                moves += 1;
            }
            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
                moves += 1;
            }
            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
                moves += 1;
            }
        }
    }
}