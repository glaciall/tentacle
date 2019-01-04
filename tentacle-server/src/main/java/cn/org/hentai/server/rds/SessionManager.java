package cn.org.hentai.server.rds;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/1/3.
 */
public final class SessionManager
{
    private static final ConcurrentHashMap<Long, TentacleDesktopSessionHandler> sessions = new ConcurrentHashMap<Long, TentacleDesktopSessionHandler>();
    private static AtomicLong sequence = new AtomicLong(1);

    public static void register(TentacleDesktopSessionHandler handler)
    {
        long nid = sequence.getAndAdd(1);
        handler.getClient().setId(nid);
        sessions.put(nid, handler);
    }

    public static TentacleDesktopSessionHandler getSession(long sessionId)
    {
        return sessions.get(sessionId);
    }

    public static TentacleDesktopSessionHandler[] activeSessions()
    {
        return sessions.values().toArray(new TentacleDesktopSessionHandler[0]);
    }

    public static void removeSession(TentacleDesktopHandler handler)
    {
        sessions.remove(handler);
    }
}
