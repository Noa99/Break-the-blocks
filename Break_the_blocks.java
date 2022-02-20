import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.sound.sampled.*;
import javax.swing.*;

public class Break_the_blocks extends JFrame {
  final int windowWidth = 500;
  final int windowHeight = 700;

  public static void main(String[] args) {
    new Break_the_blocks();
  }

  public Break_the_blocks() {
    Dimension dimOfScreen = Toolkit.getDefaultToolkit().getScreenSize();

    setBounds(dimOfScreen.width / 2 - windowWidth / 2,
        dimOfScreen.height / 2 - windowHeight / 2,
        windowWidth, windowHeight);
    setResizable(false);
    setTitle("Break the blocks");
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    MyJPanel panel = new MyJPanel();
    Container c = getContentPane();
    c.add(panel);
    setVisible(true);
  }

  public class MyJPanel extends JPanel implements
      ActionListener, MouseMotionListener, MouseListener {
    Dimension dimOfPanel;
    Timer timer;
    ImageIcon iconMe;
    Image imgMe;
    JButton restartButton, finishButton;
    Clip clip, clip2, clip3;
    boolean isRestartButtonClicked = false;
    boolean isFinishButtonClicked = false;

    // ???@????
    int playerWidth, playerHeight;
    int playerX, playerY, temPlayeryX;
    int gap = 100;
    int ballX, ballY, tempBallX;
    boolean ballIsMoving;
    int dirX = 1, dirY = -1;
    int addX = 3, addY = 3;

    // blocks関数
    int numOfBlocks = 30;
    int numOfAlive = numOfBlocks;
    int[] blockX = new int[numOfBlocks];
    int[] blockY = new int[numOfBlocks];
    int blockWidth = 40, blockHeight = 25;
    boolean[] isBlockAlive = new boolean[numOfAlive];

    // スコア
    int score = 0;

    // winメッセージ
    int congX = 0, congY = 300;
    int winX = 20, winY = 370;

    // Game Over
    int gameOverX = 0, gameOverY = 300;

    // timer
    int timerCount = 0;
    int timeSec = 0, timeDeci = 0;

    // speed
    int speed = 1;

    public MyJPanel() {
      setBackground(Color.black);
      addMouseListener(this);
      addMouseMotionListener(this);

      timer = new Timer(10, this);
      timer.start();

      restartButton = new JButton("RESTART");
      restartButton.addActionListener(this);
      restartButton.setVisible(false);

      finishButton = new JButton("FINISH");
      finishButton.addActionListener(this);
      finishButton.setVisible(false);

      iconMe = new ImageIcon("jiki.png");
      imgMe = iconMe.getImage();
      playerWidth = imgMe.getWidth(this);
      playerHeight = imgMe.getHeight(this);

      String hitSound = "hitSound.wav";
      String failedSound = "failed.wav";
      String wonSound = "won.wav";
      AudioInputStream audioInputStream;
      try {
        audioInputStream = AudioSystem.getAudioInputStream(new File(hitSound).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        audioInputStream = AudioSystem.getAudioInputStream(new File(failedSound).getAbsoluteFile());
        clip2 = AudioSystem.getClip();
        clip2.open(audioInputStream);
        audioInputStream = AudioSystem.getAudioInputStream(new File(wonSound).getAbsoluteFile());
        clip3 = AudioSystem.getClip();
        clip3.open(audioInputStream);

      } catch (Exception e) {
        System.err.println(e.getMessage());
      }

      setIsBlockAlive();
      initBlocks();
      initPlayer();
      initBall();
      add(restartButton);
      add(finishButton);
    }

    public void paintComponent(Graphics g) {
      dimOfPanel = getSize();

      super.paintComponent(g);

      drawBlocks(g);// ブロック
      drawPlayer(g);// 自機
      drawBall(g);// ボール
      g.setFont(new Font("SansSerif", Font.PLAIN, 15));
      g.drawString("Score: " + score, 15, 30);
      g.drawString("Time:  " + timeSec + ". " + timeDeci, 95, 30);
      g.drawString("Speed:  " + speed, 400, 30);

      restartButton.setBounds(dimOfPanel.width / 2 - 150, dimOfPanel.height / 2 + 100, 100, 30);
      finishButton.setBounds(dimOfPanel.width / 2 + 50, dimOfPanel.height / 2 + 100, 100, 30);
    }

    public void drawBlocks(Graphics g) {
      for (int i = 0; i < numOfBlocks; i++) {
        if (isBlockAlive[i]) {
          if (i < 10) {
            g.setColor(Color.yellow);
          } else if (i < 20) {
            g.setColor(Color.blue);
          } else {
            g.setColor(Color.green);
          }
          g.fillRect(blockX[i], blockY[i], blockWidth, blockHeight);

          // ブロックへの当たり判定
          if (numOfAlive > 0) {
            if ((ballX + 14 >= blockX[i] && ballX <= blockX[i] + blockWidth) &&
                (ballY + 14 >= blockY[i] && ballY <= blockY[i] + blockHeight)) {
              // base
              if (((blockX[i] + blockWidth) >= ballX + 7) &&
                  (ballX + 7 >= blockX[i]) &&
                  (blockY[i] + blockHeight >= ballY) &&
                  (dirY == -1)) {
                dirY = 1;
                removeBlock(i);
              }
              // upper
              else if (((blockX[i] + blockWidth) >= ballX + 7) &&
                  (ballX + 7 >= blockX[i]) &&
                  (blockY[i] <= ballY + 14) &&
                  (dirY == 1)) {
                dirY = -1;
                removeBlock(i);
              }
              // left
              else if ((blockY[i] + blockHeight >= ballY + 7) &&
                  (blockY[i] <= ballY + 7) &&
                  (ballX + 14 >= blockX[i]) &&
                  (dirX == 1)) {
                dirX = -1;
                removeBlock(i);
              }
              // right
              else if ((blockY[i] <= ballY + 7) &&
                  (blockY[i] + blockHeight >= ballY + 7) &&
                  (ballX <= blockX[i] + blockWidth) &&
                  (dirX == -1)) {
                dirX = 1;
                removeBlock(i);
              }
            }
          }
        }
      }
    }

    public void removeBlock(int blockNumber) {
      isBlockAlive[blockNumber] = false;
      numOfAlive--;
      score += 10;
      clip.start();
      clip.setFramePosition(0);
    }

    public void drawPlayer(Graphics g) {
      if (Math.abs(temPlayeryX - playerX) < gap) {
        if (playerX < 0) {
          playerX = 0;
        } else if (playerX + playerWidth > dimOfPanel.width) {
          playerX = dimOfPanel.width - playerWidth;
        }
        temPlayeryX = playerX;
        g.drawImage(imgMe, temPlayeryX, playerY, this);
      } else {
        g.drawImage(imgMe, temPlayeryX, playerY, this);
      }
    }

    public void drawBall(Graphics g) {
      g.setColor(Color.white);

      if (ballIsMoving) {
        if (ballX > dimOfPanel.width - 14) {
          dirX = -1;
        } else if (ballX < 0) {
          dirX = 1;
        }
        if ((ballY + 14 >= windowHeight - 150) && (ballY + 14 < windowHeight - 145)
            && (ballX + 7 > playerX && ballX < playerX + playerWidth)) {
          dirY = -1;
        } else if (ballY < 0) {
          dirY = 1;
        } else if (ballY + 14 > windowHeight - 150) {
          dirY = 1;
        }
        if (ballY > windowHeight) {
          ballIsMoving = false;
        }

        ballX = ballX + dirX * addX;
        ballY = ballY + dirY * addY;

        g.fillOval(ballX, ballY, 14, 14);

      } else {
        if (Math.abs(tempBallX - ballX) < gap) {
          if (playerX <= 0) {
            ballX = (playerWidth / 2) - 7;
          } else if (ballX > dimOfPanel.width - (playerWidth / 2) - 7) {
            ballX = dimOfPanel.width - (playerWidth / 2) - 7;
          }
          tempBallX = ballX;
          g.fillOval(tempBallX, ballY, 14, 14);
        } else {
          g.fillOval(tempBallX, ballY, 14, 14);
        }
      }
      if (numOfAlive > 0 && ballY >= dimOfPanel.height) {

        clip2.start();
        gameOver(g);
      } else if (numOfAlive == 0) {
        clip3.start();
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        g.drawString("!!Congratulation!!", congX, congY);
        g.drawString("You are winner", winX, winY);
        congX += 1;
        winX += 1;
        if (congX >= dimOfPanel.width) {
          congX = 0;
        }
        if (winX >= dimOfPanel.width) {
          winX = 0;
        }
        restartButton.setVisible(true);
        finishButton.setVisible(true);
        if (isFinishButtonClicked) {
          System.exit(0);
          clip3.stop();
          clip3.setFramePosition(0);
        } else if (isRestartButtonClicked) {
          restartGame(g);
          clip3.stop();
          clip3.setFramePosition(0);
        }
      }
    }

    public void gameOver(Graphics g) {
      g.setFont(new Font("SansSerif", Font.BOLD, 30));
      g.drawString("GAME OVER", gameOverX, gameOverY);
      gameOverX += 1;
      if (gameOverX >= dimOfPanel.width) {
        gameOverX = 0;
      }
      restartButton.setVisible(true);
      finishButton.setVisible(true);

      if (isFinishButtonClicked) {
        System.exit(0);
        clip2.stop();
        clip2.setFramePosition(0);
      } else if (isRestartButtonClicked) {
        restartGame(g);
        clip2.stop();
        clip2.setFramePosition(0);
      }
    }

    public void restartGame(Graphics g) {
      super.paintComponent(g);
      restartButton.setVisible(false);
      finishButton.setVisible(false);
      setIsBlockAlive();
      initBlocks();
      initPlayer();
      initBall();

      score = 0;
      numOfBlocks = 30;
      numOfAlive = numOfBlocks;
      dirX = 1;
      dirY = -1;
      addX = 3;
      addY = 3;
      isRestartButtonClicked = false;
      isFinishButtonClicked = false;
      for (int i = 0; i < numOfAlive; i++) {
        isBlockAlive[i] = true;
      }
      timerCount = 0;
      timeSec = 0;
      timeDeci = 0;
      speed = 1;
    }

    public void actionPerformed(ActionEvent e) {
      if (ballIsMoving) {
        timerCount++;
        timeSec = timerCount / 100;
        timeDeci = timerCount % 100;
        if (timerCount % 1000 == 0 && addX < 8) {
          speed++;
          addX += 1;
          addY += 1;
        }
      }

      if (e.getActionCommand() == "FINISH") {
        isFinishButtonClicked = true;
      }
      if (e.getActionCommand() == "RESTART") {
        isRestartButtonClicked = true;
      }
      repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
      if (ballY > 0 && ballY < windowHeight - 150) {
        if (Math.abs(temPlayeryX - playerX) < gap) {
          ballIsMoving = true;
        }
      }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
      playerX = e.getX() - (playerWidth / 2);
      if (!ballIsMoving) {
        ballX = e.getX() - 7;
      }
    }

    public void mouseDragged(MouseEvent e) {
    }

    private void setIsBlockAlive() {
      for (int i = 0; i < numOfAlive; i++) {
        isBlockAlive[i] = true;
      }
    }

    public void initBlocks() {
      for (int i = 0; i < 1; i++) {
        blockX[i] = ((blockWidth + 1) * i) + 35;
        blockY[i] = 70;
      }
      for (int i = 0; i < 10; i++) {
        blockX[i] = ((blockWidth + 1) * i) + 35;
        blockY[i] = 70;
      }
      for (int i = 10; i < 20; i++) {
        blockX[i] = ((blockWidth + 1) * (i - 10)) + 35;
        blockY[i] = 70 + blockHeight + 1;
      }
      for (int i = 20; i < 30; i++) {
        blockX[i] = ((blockWidth + 1) * (i - 20)) + 35;
        blockY[i] = 70 + (blockHeight * 2) + 2;
      }
    }

    public void initPlayer() {
      playerX = temPlayeryX = (windowWidth / 2) - (playerWidth / 2);
      playerY = windowHeight - 150;
    }

    public void initBall() {
      ballX = tempBallX = playerX + (playerWidth / 2) - 7;
      ballY = playerY - playerHeight - 4;
      ballIsMoving = false;
    }
  }

  public void play(String string) {
  }
}