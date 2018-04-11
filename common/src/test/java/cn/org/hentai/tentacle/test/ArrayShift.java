package cn.org.hentai.tentacle.test;

import cn.org.hentai.tentacle.util.ByteUtils;

/**
 * Created by matrixy on 2018/4/11.
 */
public class ArrayShift
{
    public static void main(String[] args) throws Exception
    {
        byte[] arr = ByteUtils.parse("00 11 22 33 44 55 66 77 88 99 aa bb cc dd ee ff");
        System.arraycopy(arr, 0, arr, 1, arr.length - 1);
        System.out.println(ByteUtils.toString(arr));
    }
}
