package cn.org.hentai.tentacle.system;

import java.util.Date;

/**
 * Created by matrixy on 2018/5/8.
 */
public final class OperatingSystem
{
    static OperatingSystem os;
    SystemCall systemCall;

    public boolean login(String username, String password) throws Exception
    {
        return systemCall.login(username, password);
    }

    public boolean lock() throws Exception
    {
        return systemCall.lock();
    }

    private OperatingSystem(SystemCall systemCall)
    {
        this.systemCall = systemCall;
    }

    public static synchronized OperatingSystem getInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        if (null == os)
        {
            String osname = System.getProperty("os.name").replaceAll("^(\\w+)( .+)?$", "$1");
            os = new OperatingSystem((SystemCall) Class.forName("cn.org.hentai.tentacle.system.os." + osname).newInstance());
        }
        return os;
    }

    public static void main(String[] args) throws Exception
    {
        OperatingSystem os = OperatingSystem.getInstance();
        boolean rst = os.lock();
        Thread.sleep(1000);
        rst = os.login("matrixy", "123456");
        System.out.println(rst);
        System.out.println(new Date().toLocaleString());
    }
}
