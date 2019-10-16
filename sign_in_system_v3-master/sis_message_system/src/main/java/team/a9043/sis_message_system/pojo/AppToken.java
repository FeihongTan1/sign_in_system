package team.a9043.sign_in_system.service_pojo;

import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AppToken {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private String accessToken;
    private Date expire;

    public AppToken(String accessToken, Date expire) {
        this.accessToken = accessToken;
        this.expire = expire;
    }

    public void modifyAppToken(String accessToken, Date expire) {
        readWriteLock.writeLock().lock();
        try {
            this.accessToken = accessToken;
            this.expire = expire;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public String getAccessToken() {
        readWriteLock.readLock().lock();
        try {
            return accessToken;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Date getExpire() {
        readWriteLock.readLock().lock();
        try {
            return new Date(expire.getTime());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
