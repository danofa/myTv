package org.damcode.www.mytv;

import java.util.Date;

/**
 *
 * @author dm
 */
public class Show {

    private String _id; // name of show
    private int numSeasons;
    private int[] episodeDistr;
    private String[] watched;
    private String[] unwatched;
    private Date nextEpisodeDate;

    public int[] getEpisodeDistr() {
        return episodeDistr;
    }

    public void setEpisodeDistr(int[] episodeDistr) {
        this.episodeDistr = episodeDistr;
    }

    public Date getNextEpisodeDate() {
        return nextEpisodeDate;
    }

    public void setNextEpisodeDate(Date nextEpisodeDate) {
        this.nextEpisodeDate = nextEpisodeDate;
    }

    public String getNextEpisodeId() {
        return nextEpisodeId;
    }

    public void setNextEpisodeId(String nextEpisodeId) {
        this.nextEpisodeId = nextEpisodeId;
    }
    private String nextEpisodeId;

    public Show(String _id) {
        this._id = _id;
    }

    public String getId() {
        return _id;
    }

    public int getNumEpisodes() {
        int sum = 0;
        for(int i : getEpisodeDistr()){
            sum += i;
        }
        return sum;
    }

    public int getNumSeasons() {
        return numSeasons;
    }

    public void setNumSeasons(int numSeasons) {
        this.numSeasons = numSeasons;
    }

    public String[] getWatched() {
        return watched;
    }

    public void setWatched(String[] watched) {
        this.watched = watched;
    }

    public String[] getUnwatched() {
        return unwatched;
    }

    public void setUnwatched(String[] unwatched) {
        this.unwatched = unwatched;
    }

}
