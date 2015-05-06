package org.damcode.www.mytv.db;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.damcode.www.mytv.Show;

/**
 *
 * @author dm
 */
public class myTvDBAO {

    private final DB db;
    private final DBCollection dbCollection;

    public myTvDBAO() {
        db = MongoSingleton.INSTANCE.getDb("damtvtracker");
        dbCollection = db.getCollection("shows");
    }

    public ArrayList<String> getShows() {
        ArrayList<String> shows = new ArrayList<>();

        DBCursor curs = dbCollection.find();
        while (curs.hasNext()) {
            shows.add((String) curs.next().get("_id"));
        }
        return shows;
    }

    public void deleteShow(String show) {
        try {
            dbCollection.remove(new BasicDBObject("_id", show));
        } catch (MongoException ex) {
            System.out.println("Error in deleting " + show + " : " + ex);
        }
    }

    public void setWatched(String show, String episodeId) {
        BasicDBObject pullAndSet = new BasicDBObject("$pull", new BasicDBObject("unwatched_ids", episodeId))
                .append("$addToSet", new BasicDBObject("watched_ids", episodeId));
        dbCollection.update(new BasicDBObject("_id", show), pullAndSet);
    }

    public void addShow(Show show) {
        BasicDBObject seasons = new BasicDBObject("count", show.getNumSeasons())
                .append("episode_distr", show.getEpisodeDistr());

        BasicDBObject ins = new BasicDBObject("_id", show.getId())
                .append("seasons", seasons)
                .append("unwatched_ids", show.getUnwatched())
                .append("nextepidate", show.getNextEpisodeDate())
                .append("nexteid", show.getNextEpisodeId());

        try {
            dbCollection.insert(ins);
        } catch (MongoException ex) {
            System.out.println("Exception in addShow(" + show + "): " + ex);
        }
    }

    public ArrayList<Map> getAllNextEpisodes() {
        ArrayList<Map> nextData = new ArrayList<Map>();
        BasicDBObject qry = new BasicDBObject("nextepidate", 1)
                .append("nexteid", 1);

        DBCursor curs = dbCollection.find(new BasicDBObject(), qry);

        while (curs.hasNext()) {
            Map m = curs.next().toMap();
            if (m.get("nexteid") != null) {
                nextData.add(m);
            }
        }
        return nextData;
    }

    public long getEpisodeCount(String show) {
        //db.shows.aggregate({$match:{_id:"suits"}},{$unwind:"$seasons.episode_distr"},{$group:{_id:"$_id","totalep":{$sum:"$seasons.episode_distr"}}})

        ArrayList pipeline = new ArrayList();
        pipeline.add(new BasicDBObject("$match", new BasicDBObject("_id", show)));
        pipeline.add(new BasicDBObject("$unwind", "$seasons.episode_distr"));
        pipeline.add(new BasicDBObject("$group", new BasicDBObject("_id", "$_id")
                .append("totalep", new BasicDBObject("$sum", "$seasons.episode_distr"))));

        AggregationOutput result = dbCollection.aggregate(pipeline);
        int totalcount = 0;
        try {
            Object o = result.results().iterator().next().get("totalep");
            if (o instanceof Double) {
                totalcount = ((Double) o).intValue();
            } else {
                totalcount = (int) o;
            }
        } catch (Exception e) {
            System.out.println("error in aggregation getEpisodeCount: " + e);
        }
        return totalcount;
    }

    public void addUnwatchedEids(String show, String[] eids) {
        BasicDBObject qry = new BasicDBObject("_id", show);
        BasicDBObject upd = new BasicDBObject("$addToSet", new BasicDBObject("unwatched_ids", new BasicDBObject("$each", eids)));
        dbCollection.update(qry, upd);
    }

    public void updateSeasonsData(String show, int season, int numTotalEpisodes) {
        BasicDBObject qry = new BasicDBObject("_id", show);
        BasicDBObject upd = new BasicDBObject("$set", new BasicDBObject("seasons.episode_distr." + (season - 1), numTotalEpisodes));
        dbCollection.update(qry, upd);
    }

    public DBObject getShowData(String show) {
        BasicDBObject qry = new BasicDBObject("_id", show);
        Show s = new Show(show);

        // TODO : convert return value to Show class;
        return dbCollection.findOne(qry);
    }

    public void updateNextEpisodeData(String show, Date neDate, String neId) {
        BasicDBObject qry = new BasicDBObject("_id", show);
        BasicDBObject upd = new BasicDBObject("$set", new BasicDBObject("nexteid", neId).append("nextepidate", neDate));

        WriteResult update = dbCollection.update(qry, upd);
    }
}
