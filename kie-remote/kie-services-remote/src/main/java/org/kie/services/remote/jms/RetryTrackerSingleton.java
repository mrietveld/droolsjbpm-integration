package org.kie.services.remote.jms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps make sure that messages which fail are not
 * endlessly retried.
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class RetryTrackerSingleton {

    private static final Logger logger = LoggerFactory.getLogger(RetryTrackerSingleton.class);

    private final static int MSG_RETRY_CACHE_LIMIT = 1000;
    private HashMap<String, Integer> msgIdRetryMap = new LinkedHashMap<String, Integer>() {
        /* generated serial version UID */
        private static final long serialVersionUID = 6031151588684920520L;
        @Override protected boolean removeEldestEntry (Map.Entry<String,Integer> eldest) {
           return size() > MSG_RETRY_CACHE_LIMIT;
        }
    };

    private Integer RETRY_LIMIT = null;
    private int RETRY_LIMIT_DEFAULT = 2;
    private static String RETRY_LIMIT_NAME_PROPERTY = "kie.services.jms.retries.limit";

    @PostConstruct
    public void init() {
        String retryLimitStr = System.getProperty(RETRY_LIMIT_NAME_PROPERTY, Integer.toString(RETRY_LIMIT_DEFAULT));
        try {
            RETRY_LIMIT = Integer.parseInt(retryLimitStr);
        } catch (Exception e) {
            logger.warn("Unable to parse '" + retryLimitStr + "' as number for " + RETRY_LIMIT_NAME_PROPERTY + " property.");
            RETRY_LIMIT = RETRY_LIMIT_DEFAULT;
        }
        logger.info("JMS message retry limit set to " + RETRY_LIMIT);
    }

    @Lock(LockType.READ)
    public int getMaximumLimitRetries() { 
        return RETRY_LIMIT;
    }
    
    @Lock(LockType.READ)
    public boolean maxRetriesReached(String msgId) {
        Integer retries = msgIdRetryMap.get(msgId);
        if (retries == null) {
            return false;
        }
        if (retries < RETRY_LIMIT) {
            return false;
        }
        return true;
    }

    @Lock(LockType.WRITE)
    public int incrementRetries(String msgId) {
        Integer retries = msgIdRetryMap.get(msgId);
        if (retries == null) {
            retries = 0;
        }
        ++retries;
        msgIdRetryMap.put(msgId, retries);
        return retries;
    }
    
    @Lock(LockType.WRITE)
    public void clearRetries(String msgId) {
        msgIdRetryMap.remove(msgId);
    }

}
