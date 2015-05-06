package org.damcode.www.mytv;

import com.mongodb.BasicDBList;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.damcode.www.mytv.db.myTvDBAO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author dm
 */
public class FtvHandler {

    public static final String baseUrl = PrivateSettings.baseUrl;
    public static final String userAgent = PrivateSettings.userAgent;
    public static final String showIndexElement = PrivateSettings.showIndexElement;
    public static final String badShowIndexTitle = PrivateSettings.badShowIndexTitle;

    // regex pattern to match next episode, splits into 4 groups
    //  25 Jan 2015 S02E14
    public static final String nextEpisodePattern = ".*Next Episode Air Date:.(\\w+.)(\\w+.)(\\w+.)(\\w+.).*";
    public static final String nextEpisodeDateFormat = "dd MMM yyyy";

    myTvDBAO dbao;

    public static void main(String[] args) throws IOException {
        System.out.println("Show Exists: " + new FtvHandler().showExists("elementary"));
        System.out.println("episode count: " + new FtvHandler().fetchNumEpiAndSeas("the_walking_dead").episodes.length);
        System.out.println("season count: " + new FtvHandler().fetchNumEpiAndSeas("the_walking_dead").seasons);
    }

    public FtvHandler() {
        dbao = new myTvDBAO();
    }

    public ArrayList<String> getShows() {
        return dbao.getShows();
    }

    public void threadUpdateShows() {
        ExecutorService exec = Executors.newCachedThreadPool();

        for (String s : getShows()) {
            exec.submit(new UpdateShowThreadClass(s));
        }

        exec.shutdown();
        System.out.println("All thread tasks submitted");
        try {
            exec.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(FtvHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("finished all thread tasks.");
    }

    private void addNewSeason(String show, int season, int numEpisodes) {
        dbao.addUnwatchedEids(show, fillUnwatched(numEpisodes, season, 1));
    }

    private long getEpisodeCount(String show) {
        return dbao.getEpisodeCount(show);
    }

    public ArrayList<HashMap> getAllUnwatched() {
        ArrayList<HashMap> result = new ArrayList<HashMap>();
        for (String s : getShows()) {
            HashMap unwatchedEps = getUnwatchedEps(s);
            if (unwatchedEps != null) {
                result.add(unwatchedEps);
            }
        }
        return result;
    }

    public ArrayList<Map> getAllNextEpisodes() {
        ArrayList<Map> result = dbao.getAllNextEpisodes();
        for (Map m : result) {
            m.put("nexteid", decodeEpisodeId((String) m.get("nexteid")));
        }
        return result;
    }

    public HashMap getUnwatchedEps(String show) {
        HashMap result = new HashMap();
        result.putAll((Map) dbao.getShowData(show));

        ArrayList<String> uw = new ArrayList<String>();

        if (result.containsKey("unwatched_ids")) {
            for (Object o : (BasicDBList) result.get("unwatched_ids")) {
                String eid = (String) o.toString();
                if (!eid.isEmpty()) {
                    uw.add(decodeEpisodeId(eid));
                }
            }

            if (uw.isEmpty())
                return null;
            else
                result.put("unwatched_ids", uw);
        }

        return result;
    }

    public boolean addShow(String show) throws IOException {
        if (show.trim().isEmpty())
            return false;

        show = show.replace(" ", "_").toLowerCase();

        if (showExists(show)) {
            Show s = new Show(show);
            NumEandS nes = fetchNumEpiAndSeas(show);

            s.setNumSeasons(nes.seasons);
            s.setEpisodeDistr(nes.episodes);
            s.setUnwatched(fillUnwatched(s, nes.count));

            if (nes.nextEpisodeData != null) {
                s.setNextEpisodeDate((Date) nes.nextEpisodeData.nextepidate);
                s.setNextEpisodeId((String) nes.nextEpisodeData.nexteid);
            }
            dbao.addShow(s);
            return true;
        }

        return false;
    }

    private String[] fillUnwatched(int count, int forSeason, int startAtEpisode) {
        String[] unwatched = new String[count];
        for (int ep = startAtEpisode, i = 0; i < count; ep++, i++) {
            unwatched[i] = "S" + String.format("%02d", forSeason) + "E" + String.format("%02d", ep);
        }
        return unwatched;
    }

    private String[] fillUnwatched(Show s, int count) {
        String[] unwatched = new String[count];
        int i = 0;
        for (int se = 0; se < s.getNumSeasons(); se++) {
            for (int ep = 0; ep < s.getEpisodeDistr()[se]; ep++) {
                unwatched[i] = "S" + String.format("%02d", (se + 1)) + "E" + String.format("%02d", (ep + 1));
                i++;
            }
        }
        return unwatched;
    }

    private boolean showExists(String show) throws IOException {
        Document doc = getDoc(baseUrl + show);
        if (doc.title().toLowerCase().contains(badShowIndexTitle.toLowerCase())) {
            return false;
        } else {
            return doc.title().toLowerCase().contains(show.replace("_", " "));
        }
    }

    public String decodeEpisodeId(String id) {
        String result = "";
        Pattern p = Pattern.compile("^S(\\d+)E(\\d+)");
        Matcher m = p.matcher(id.trim());

        if (m.matches()) {
            result = "season_" + Integer.parseInt(m.group(1))
                    + ",e" + Integer.parseInt(m.group(2))
                    + "," + id;
        }

        return result;
    }

    // extracts next episode data from raw website input
    private NextEpisodeData extractNextEpisode(String raw) {
        NextEpisodeData data = new NextEpisodeData();

        Matcher m = Pattern.compile(nextEpisodePattern).matcher(raw);
        if (m.matches()) {
            Date neDate = extractDate(m.group(1) + m.group(2) + m.group(3));
            if (neDate != null) {
                data.nextepidate = neDate;
                data.nexteid = m.group(4);
                return data;
            }
        }
        return null;
    }

    private Date extractDate(String rawDate) {
        SimpleDateFormat df = new SimpleDateFormat(nextEpisodeDateFormat);

        try {
            Date nextEpiDate = df.parse(rawDate);
            return nextEpiDate;
        } catch (ParseException ex) {
            System.out.println("Error in date extraction: " + ex);
            return null;
        }
    }

    private NumEandS fetchNumEpiAndSeas(String show) throws IOException {
        NumEandS result = new NumEandS();
        ArrayList<Integer> epiDistr = new ArrayList<Integer>();

        Document doc = getDoc(baseUrl + show);
        Elements es = doc.getElementsByClass(showIndexElement);
        Pattern p = Pattern.compile("^Season\\s+(\\d+).\\((\\d+).*");

        for (Element e : es) {
            String s = e.text();
            try {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    epiDistr.add(Integer.parseInt(m.group(2)));
                    result.count += Integer.parseInt(m.group(2));
                    result.seasons++;
                }
            } catch (IllegalStateException ex) {
                System.out.println("Error in regex match: " + ex);
            }

            // check and fill next episode data;
            NextEpisodeData neData = extractNextEpisode(s);
            if (neData != null) {
                result.nextEpisodeData = neData;
            }
        }

        result.episodes = new int[result.seasons];
        for (int i = 0; i < epiDistr.size(); i++) {
            result.episodes[i] = epiDistr.get(i);
        }

        return result;
    }

    public void setWatched(String show, String episodeId) {
        dbao.setWatched(show, episodeId);

    }

    public void deleteShow(String show) {
        dbao.deleteShow(show);
    }

    private Document getDoc(String url) {
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .get();

        } catch (IOException ex) {
            Logger.getLogger(FtvHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    class UpdateShowThreadClass implements Runnable {

        private String show;

        public UpdateShowThreadClass(String show) {
            this.show = show;
        }

        @Override
        public void run() {
            System.out.println("Thread started : " + show);

            NumEandS nes = null;
            try {
                nes = fetchNumEpiAndSeas(show);
            } catch (IOException ex) {
                Logger.getLogger(FtvHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (nes != null && nes.count > getEpisodeCount(show)) {
                if (nes.nextEpisodeData != null) {
                    dbao.updateNextEpisodeData(show, nes.nextEpisodeData.nextepidate, nes.nextEpisodeData.nexteid);
                }

                Map showMap = (Map) dbao.getShowData(show);
                Map seasons = (Map) showMap.get("seasons");

                if ((int) seasons.get("count") < nes.seasons) {
                    for (int i = (int) seasons.get("count") + 1; i <= nes.seasons; i++) {
                        addNewSeason(show, i, nes.episodes[i - 1]);
                    }
                }

                if ((int) seasons.get("count") == nes.seasons) {
                    for (int i = 0; i < nes.seasons; i++) {
                        int remoteEpiCount = nes.episodes[i];
                        Object o = ((BasicDBList) seasons.get("episode_distr")).get(i);
                        int localEpiCount = 0;
                        if (o instanceof Double) {
                            localEpiCount = ((Double) o).intValue();
                        } else {
                            localEpiCount = (int) o;
                        }

                        if (remoteEpiCount > localEpiCount) {
                            String[] newEids = fillUnwatched(remoteEpiCount - localEpiCount, i + 1, localEpiCount + 1);
                            dbao.addUnwatchedEids(show, newEids);
                            dbao.updateSeasonsData(show, i + 1, remoteEpiCount);
                        }
                    }
                }
            }
            System.out.println("Thread exiting: " + show);
        }
    }
}
