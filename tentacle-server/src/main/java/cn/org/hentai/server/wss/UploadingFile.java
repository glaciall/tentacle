package cn.org.hentai.server.wss;

/**
 * Created by matrixy on 2019/1/12.
 */
public class UploadingFile
{
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String filePath;
    private Long receivedBytes;

    public boolean uploadCompleted()
    {
        return receivedBytes >= fileSize;
    }

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId(String fileId)
    {
        this.fileId = fileId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Long getReceivedBytes()
    {
        return receivedBytes;
    }

    public void setReceivedBytes(Long receivedBytes)
    {
        this.receivedBytes = receivedBytes;
    }
}
