package cn.org.hentai.client.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Created by matrixy on 2019/5/20.
 */
public class LockTest
{
    public static void main(String[] args) throws Exception
    {
        final LockTest test = new LockTest();
        new Thread()
        {
            public void run()
            {
                while (true)
                {
                    Integer val = test.get();
                    if (null == val)
                    {
                        System.out.println("<- blank");
                        break;
                    }
                    else System.out.println("<- " + val);
                }
            }
        }.start();

        Thread.sleep(1000);
        java.io.BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            System.out.print("> ");
            String text = reader.readLine();
            if (text.matches("^\\d+$")) test.add(Integer.parseInt(text));
            else break;
        }
        test.free();
        System.out.println("\n free !!!");
        System.in.read();
    }

    boolean free = false;
    Object lock = new Object();
    LinkedList<Integer> array = new LinkedList<Integer>();

    public Integer get()
    {
        Integer val = null;
        free = false;
        synchronized (lock)
        {
            while (free == false && array.size() == 0) try { lock.wait(); } catch(Exception e) { }
            if (array.size() > 0) val = array.removeLast();
            array.clear();
        }
        return val;
    }

    public void add(Integer val)
    {
        synchronized (lock)
        {
            array.add(val);
            lock.notifyAll();
        }
    }

    public void free()
    {
        synchronized (lock)
        {
            lock.notifyAll();
        }
    }
}
