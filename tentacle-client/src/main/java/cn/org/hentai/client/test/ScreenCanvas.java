package cn.org.hentai.client.test;

import java.awt.*;

/**
 * Created by matrixy on 2018-04-09.
 */
public class ScreenCanvas extends Canvas
{
    @Override
    public void paint(Graphics g)
    {
        g.setColor(Color.RED);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
}
