package cn.org.hentai.tentacle.system;

/**
 * Created by matrixy on 2018/5/8.
 * 操作系统调用声明
 */
public interface SystemCall
{
    /**
     * 用户登陆/解锁
     * @param username
     * @param password
     * @return
     */
    public boolean login(String username, String password) throws Exception;

    /**
     * 锁屏/锁定
     * @return
     */
    public boolean lock() throws Exception;
}
