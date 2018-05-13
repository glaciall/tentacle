package cn.org.hentai.tentacle.system.os;

import cn.org.hentai.tentacle.system.SystemCall;

import java.io.File;

/**
 * Created by matrixy on 2018/5/8.
 */
public class Windows implements SystemCall
{
    public boolean login(String username, String password) throws Exception
    {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("rundll32.exe advapi32.dll,LogonUser  " + username + " " + password);
        process.waitFor();
        return process.exitValue() == 0;
    }

    public boolean lock() throws Exception
    {
        Runtime.getRuntime().exec("RunDll32.exe user32.dll,LockWorkStation");
        return true;
    }
}
