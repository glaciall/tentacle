package cn.org.hentai.tentacle.protocol;

/**
 * Created by matrixy on 2019/1/3.
 */
public class Message
{
    byte command;
    Packet body;

    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command)
    {
        this.command = command;
    }

    public Message withCommand(byte command)
    {
        setCommand(command);
        return this;
    }

    public Packet getBody()
    {
        return body;
    }

    public void setBody(Packet body)
    {
        this.body = body;
    }

    public Message withBody(Packet body)
    {
        setBody(body);
        return this;
    }
}
