package cn.org.hentai.client.test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import static sun.misc.Version.init;

/**
 * Created by matrixy on 2018-04-09.
 */
public class Capture
{
    JFrame frame;
    ScreenCanvas canvas;

    public static LinkedList<BufferedImage> screens = new LinkedList<BufferedImage>();

    public Capture()
    {
        init();
    }

    private void init()
    {
        frame = new JFrame();
        frame.setTitle("Capture");

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);

        canvas = new ScreenCanvas();

        frame.getContentPane().add(canvas);
        frame.setSize(800, 600);
        frame.setLocation(1920 - 800, 1080 - 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        new Thread(new Runnable() {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Robot robot = new Robot();
                        BufferedImage img = robot.createScreenCapture(new Rectangle(0, 0, 800, 600));
                        synchronized (screens)
                        {
                            screens.add(img);
                        }
                        Thread.sleep(20);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                while (true)
                {
                    try
                    {
                        BufferedImage img = null;
                        if (screens.size() > 0)
                        {
                            synchronized (screens)
                            {
                                img = screens.removeFirst();
                            }
                            canvas.setScreen(img);
                        }
                        Thread.sleep(20);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception
    {
        new Capture();
    }
}
