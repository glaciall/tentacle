package cn.org.hentai.client.worker;

import cn.org.hentai.client.client.Client;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Log;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by matrixy on 2018/5/15.
 */
public class FileTransferWorker extends BaseWorker
{
    File file = null;
    Client clientSession = null;
    public FileTransferWorker(Client clientSession, File file)
    {
        this.file = file;
        this.clientSession = clientSession;

        this.setName("file-transfer-worker");
    }

    private void transfer() throws Exception
    {
        int len = -1;
        byte[] block = new byte[40960];
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(this.file);
            while ((len = fis.read(block)) > -1)
            {
                byte[] data = null;
                if (len == 40960) data = block;
                else
                {
                    data = new byte[len];
                    System.arraycopy(block, 0, data, 0, len);
                }
                clientSession.send(Packet.create(Command.DOWNLOAD_FILE_RESPONSE, len + 4).addInt(len).addBytes(data));
            }
            clientSession.send(Packet.create(Command.DOWNLOAD_FILE_RESPONSE, 4).addInt(0));
        }
        finally
        {
            try { fis.close(); } catch(Exception e) { }
        }
    }

    public void run()
    {
        try
        {
            transfer();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
