package cn.org.hentai.client.desktop;

import cn.org.hentai.tentacle.hid.HIDCommand;

import java.util.LinkedList;

/**
 * Created by matrixy on 2019/5/18.
 */
public final class HIDCommands
{
    LinkedList<HIDCommand> commands = null;

    private HIDCommands()
    {
        commands = new LinkedList<HIDCommand>();
    }

    public void add(HIDCommand cmd)
    {
        synchronized (commands)
        {
            commands.addLast(cmd);
        }
    }

    public HIDCommand get()
    {
        synchronized (commands)
        {
            if (commands.size() == 0) return null;
            return commands.removeFirst();
        }
    }

    static HIDCommands instance = null;
    public static synchronized HIDCommands getInstance()
    {
        if (instance == null) instance = new HIDCommands();
        return instance;
    }
}
