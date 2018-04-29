package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.hid.HIDCommand;
import cn.org.hentai.tentacle.hid.KeyMapping;
import cn.org.hentai.tentacle.hid.KeyboardCommand;
import cn.org.hentai.tentacle.hid.MouseCommand;
import cn.org.hentai.tentacle.util.Log;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

/**
 * Created by Expect on 2018/4/21.
 */
public class HIDCommandExecutor extends BaseWorker
{
    LinkedList<HIDCommand> commands = null;
    HIDCommand nextCommand = null;
    int timespan = 0;
    long nextExecuteTime = 0L;
    Robot robot = null;
    public HIDCommandExecutor() throws AWTException
    {
        robot = new Robot();
        robot.setAutoDelay(0);
        robot.setAutoWaitForIdle(false);
        commands = new LinkedList<HIDCommand>();
    }

    public void add(HIDCommand cmd)
    {
        synchronized (commands)
        {
            commands.addLast(cmd);
        }
    }

    private HIDCommand get()
    {
        synchronized (commands)
        {
            if (commands.size() == 0) return null;
            return commands.removeFirst();
        }
    }

    private void execute() throws Exception
    {
        if (System.currentTimeMillis() < nextExecuteTime) return;
        if (null == nextCommand) nextCommand = get();
        if (nextCommand == null) return;

        int timespan = nextCommand.timestamp;
        doCommand(nextCommand);
        nextCommand = get();
        if (nextCommand != null)
        {
            timespan = nextCommand.timestamp - timespan;
            nextExecuteTime = System.currentTimeMillis() + timespan;
        }
    }

    private void doCommand(HIDCommand cmd)
    {
        if (cmd.type == HIDCommand.TYPE_MOUSE)
        {
            MouseCommand mouse = (MouseCommand) cmd;
            if (mouse.eventType == MouseCommand.MOUSE_DOWN)
            {
                int key = InputEvent.BUTTON1_MASK;
                if (mouse.key == 2) key = InputEvent.BUTTON2_MASK;
                else if (mouse.key == 3) key = InputEvent.BUTTON3_MASK;
                robot.mouseMove(mouse.x, mouse.y);
                robot.mousePress(key);
            }
            else if (mouse.eventType == MouseCommand.MOUSE_UP)
            {
                int key = InputEvent.BUTTON1_MASK;
                if (mouse.key == 2) key = InputEvent.BUTTON2_MASK;
                else if (mouse.key == 3) key = InputEvent.BUTTON3_MASK;
                robot.mouseMove(mouse.x, mouse.y);
                robot.mouseRelease(key);
            }
            else if (mouse.eventType == MouseCommand.MOUSE_MOVE)
            {
                robot.mouseMove(mouse.x, mouse.y);
            }
            else if (mouse.eventType == MouseCommand.MOUSE_WHEEL)
            {
                robot.mouseMove(mouse.x, mouse.y);
                robot.mouseWheel(mouse.key == 1 ? -1 : 1);
            }
        }
        else if (cmd.type == HIDCommand.TYPE_KEYBOARD)
        {
            KeyboardCommand keyboard = (KeyboardCommand) cmd;
            if (keyboard.eventType == KeyboardCommand.KEY_PRESS)
            {
                try
                {
                    robot.keyPress(KeyMapping.convert(keyboard.keycode));
                }
                catch(Exception e) { Log.error(e); }
            }
            else if (keyboard.eventType == KeyboardCommand.KEY_RELEASE)
            {
                try
                {
                    robot.keyRelease(KeyMapping.convert(keyboard.keycode));
                }
                catch(Exception e) { Log.error(e); }
            }
        }
    }

    public void run()
    {
        while (!this.isTerminated())
        {
            try
            {
                execute();
                sleep(5);
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }
    }
}
