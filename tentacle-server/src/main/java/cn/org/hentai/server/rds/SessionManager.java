package cn.org.hentai.server.rds;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/1/3.
 */
public final class SessionManager
{
    private static final ConcurrentHashMap<Long, TentacleDesktopSession> sessions = new ConcurrentHashMap<Long, TentacleDesktopSession>();
    private static AtomicLong sequence = new AtomicLong(1);

    public static void register(TentacleDesktopSession session)
    {
        long nid = sequence.getAndAdd(1);
        session.getClient().setId(nid);
        sessions.put(nid, session);
    }

    public static TentacleDesktopSession getSession(long sessionId)
    {
        return sessions.get(sessionId);
    }

    public static TentacleDesktopSession[] activeSessions()
    {
        return sessions.values().toArray(new TentacleDesktopSession[0]);
    }

    public static void removeSession(TentacleDesktopSession session)
    {
        sessions.remove(session);
    }
}
