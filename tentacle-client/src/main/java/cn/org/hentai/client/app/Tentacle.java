package cn.org.hentai.client.app;

import cn.org.hentai.client.test.ScreenCanvas;
import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.system.LocalComputer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by matrixy on 2018/4/9.
 */
public class Tentacle
{
    JFrame frame;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    public Tentacle()
    {
        initCore();
        initWindow();
    }

    // 核心模块初始化，提高运行中的性能
    private void initCore()
    {
        // 静态成员初始化
        RLEncoding.init();
    }

    // 窗口UI初始化
    private void initWindow()
    {
        Rectangle screenSize = LocalComputer.getScreenSize();

        frame = new JFrame();
        frame.setTitle("Tentacle Desktop");

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocation((int)screenSize.getWidth() - WINDOW_WIDTH, (int)screenSize.getHeight() - WINDOW_HEIGHT - 50);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }
}
