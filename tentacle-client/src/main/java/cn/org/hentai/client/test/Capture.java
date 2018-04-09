package cn.org.hentai.client.test;

import javax.swing.*;
import java.awt.*;

import static sun.misc.Version.init;

/**
 * Created by matrixy on 2018-04-09.
 */
public class Capture
{
    JFrame frame;
    ScreenCanvas canvas;

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
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws Exception
    {
        new Capture();
    }
}
