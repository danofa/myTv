package org.damcode.www.mytv.db;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dm
 */
public enum MongoSingleton {
    
    INSTANCE;

    private MongoClient dbClient;

    /**
     *
     * @param dbName
     * @return instance of MongoClient("server").getDB("database")
     */
    public DB getDb(String dbName) {
        DB db = null;
        if (dbClient == null) {
            try {
                dbClient = new MongoClient();
                db = dbClient.getDB(dbName);
            } catch (UnknownHostException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            db = dbClient.getDB(dbName);
        }

        return db;
    }

    /*
     @param shutdown connection
     */
    public void shutdown() {
        try {
            System.out.println(Thread.currentThread().getStackTrace()[1] + " @ " + new Date());
            dbClient.close();
            dbClient = null;

        } catch (Exception e) {
            System.out.println("Exception in DB Singleton shutdown: " + e);
        }
    }
}
