package cn.org.hentai.client.test;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by matrixy on 2018-04-09.
 */
public class ScreenCanvas extends Canvas
{
    private BufferedImage screen = null;

    public void setScreen(BufferedImage img)
    {
        this.screen = img;
        this.repaint();
    }

    @Override
    public void paint(Graphics g)
    {
        if (this.screen != null) g.drawImage(this.screen, 0, 0, null);
    }
}
