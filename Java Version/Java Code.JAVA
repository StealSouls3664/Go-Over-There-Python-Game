import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Go Over There - Java port of the pygame move_towards demo.
 *
 * Mouse: click to set a new target position.
 * Keys:  R = restart, ESC = quit.
 */
public class GoOverThere extends JPanel {
    static final double MIN_SPEED = 0.25;
    static final double MAX_SPEED = 5;
    static final int WIDTH = 1000, HEIGHT = 600;
    static final int CIRCLE_RADIUS = 5;
    static final Color BG = new Color(31, 143, 65);
    static final Color BALL = new Color(118, 207, 145);

    static class Ball {
        double x, y, speed;
        Ball(double x, double y, double speed) { this.x = x; this.y = y; this.speed = speed; }

        // Equivalent of Vector2.move_towards_ip
        void moveTowards(double tx, double ty, double maxDist) {
            double dx = tx - x, dy = ty - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist == 0 || dist <= maxDist) {
                x = tx; y = ty;
            } else {
                x += dx / dist * maxDist;
                y += dy / dist * maxDist;
            }
        }
    }

    final int maxBalls;
    final Random random = new Random();
    final List<Ball> balls = new ArrayList<>();
    Point2D target = null;
    double deltaTime = 0; // milliseconds, like pygame's clock.tick()
    long lastNanos = System.nanoTime();
    final double[] recent = new double[10];
    int recentIdx = 0;
    JFrame frame;

    static class Point2D { double x, y; Point2D(double x, double y) { this.x = x; this.y = y; } }

    GoOverThere(int maxBalls) {
        this.maxBalls = maxBalls;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        reset();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                target = new Point2D(e.getX(), e.getY());
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
                if (e.getKeyCode() == KeyEvent.VK_R) reset();
            }
        });

        new Timer(1000 / 60, e -> tick()).start();
    }

    void reset() {
        target = null;
        balls.clear();
        for (int i = 0; i < maxBalls; i++) {
            balls.add(new Ball(random.nextInt(WIDTH + 1), random.nextInt(HEIGHT + 1),
                    MIN_SPEED + random.nextDouble() * (MAX_SPEED - MIN_SPEED)));
        }
    }

    void tick() {
        long now = System.nanoTime();
        deltaTime = (now - lastNanos) / 1_000_000.0;
        lastNanos = now;
        recent[recentIdx++ % recent.length] = deltaTime;

        if (target != null) {
            for (Ball b : balls) b.moveTowards(target.x, target.y, b.speed * deltaTime);
        }
        repaint();

        double avg = 0;
        for (double d : recent) avg += d;
        avg /= recent.length;
        double fps = avg > 0 ? 1000.0 / avg : 0;
        frame.setTitle(String.format("fps: %.2f, ball count: %d", fps, balls.size()));
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(BALL);
        for (Ball b : balls) {
            g2.fillOval((int) Math.round(b.x - CIRCLE_RADIUS), (int) Math.round(b.y - CIRCLE_RADIUS),
                    CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);
        }
    }

    static int askBallCount() {
        while (true) {
            String s = JOptionPane.showInputDialog(null,
                    "Enter the number of balls to add to the pit:", "Go Over There", JOptionPane.QUESTION_MESSAGE);
            if (s == null) System.exit(0);
            try {
                int n = Integer.parseInt(s.trim());
                if (n >= 0) return n;
            } catch (NumberFormatException ignored) { }
            JOptionPane.showMessageDialog(null, "Please enter a non-negative whole number.");
        }
    }

    public static void main(String[] args) {
        int n;
        if (args.length > 0) {
            n = Integer.parseInt(args[0]);
        } else {
            n = askBallCount();
        }
        final int count = n;
        SwingUtilities.invokeLater(() -> {
            GoOverThere panel = new GoOverThere(count);
            JFrame f = new JFrame("Go Over There");
            panel.frame = f;
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setResizable(false);
            f.add(panel);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
