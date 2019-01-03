package cn.org.hentai.server.rds;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/1/3.
 */
public final class SessionManager
{
    private static RemoteDesktopServer instance = null;
    private static final ConcurrentHashMap<Long, TentacleDesktopHandler> sessions = new ConcurrentHashMap<Long, TentacleDesktopHandler>();
    private static AtomicLong sequence = new AtomicLong(1);

    public static void register(TentacleDesktopHandler handler)
    {
        long nid = sequence.getAndAdd(1);
        handler.getClient().setId(nid);
        sessions.put(nid, handler);
    }

    public static TentacleDesktopHandler getSession(long sessionId)
    {
        return sessions.get(sessionId);
    }

    public static TentacleDesktopHandler[] activeSessions()
    {
        return sessions.values().toArray(new TentacleDesktopHandler[0]);
    }

    public static void removeSession(TentacleDesktopHandler handler)
    {
        sessions.remove(handler);
    }
}
