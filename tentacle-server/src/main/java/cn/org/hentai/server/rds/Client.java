package cn.org.hentai.server.rds;

import java.net.InetAddress;
import java.net.SocketAddress;

/**
 * Created by matrixy on 2019/1/3.
 */
public class Client
{
    private long id;
    private String name;
    private SocketAddress address;
    private boolean controlling;
    private String secret;

    public String getSecret() {
        return secret;
    }

    public Client setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public long getId() {
        return id;
    }

    public Client setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Client setName(String name) {
        this.name = name;
        return this;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public boolean isControlling() {
        return controlling;
    }

    public void setControlling(boolean controlling)
    {
        this.controlling = controlling;
    }
}
