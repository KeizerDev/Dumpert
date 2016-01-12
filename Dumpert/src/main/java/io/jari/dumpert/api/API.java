package io.jari.dumpert.api;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jari.dumpert.R;
import io.jari.dumpert.Utils;
import io.jari.dumpert.thirdparty.SerializeObject;
import io.jari.dumpert.thirdparty.TimeAgo;

/**
 * JARI.IO
 * Date: 11-12-14
 * Time: 21:58
 */
public class API {
    static String TAG = "DAPI";

    static void setNSFWCookie(Context context, Connection connection) {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("nsfw", false)) {
            Log.d(TAG, "asking to see some leg");
            connection.cookie("nsfw", "1");
        }
    }

    static Object getFromCache(Context context, String key) {
        String raw = context.getSharedPreferences("dumpert", 0).getString(key, null);
        if(raw == null) return null;
        return SerializeObject.stringToObject(raw);
    }

    static void saveToCache(Context context, String key, Serializable object) {
        context.getSharedPreferences("dumpert", 0).edit().putString(key, SerializeObject
                .objectToString(object)).apply();
    }

    /**
     * getListing fetches a listing of items and parses them into a Item array.
     * Returns cache if in offline mode.
     */
    public static Item[] getListing(Integer page, Context context, String path) throws IOException,
            ParseException {
        String cacheKey = "frontpage_"+page+"_"+path.replace("/", "");
        if(Utils.isOffline(context)) {
            Object cacheObj = API.getFromCache(context, cacheKey);
            //if no cached data present, return empty array
            if(cacheObj == null) return new Item[0];
            else {
                return (Item[])cacheObj;
            }
        }

        Connection connection = Jsoup.connect("https://www.dumpert.nl" + path + ((page != 0) ? page
                + "/" : ""));
        // set timeout to 12 seconds.
        // Default is 3, and internet speeds on weak connections take about 12 seconds.
        connection.timeout(12000);
        setNSFWCookie(context, connection);
        Document document = connection.get();
        Elements elements = document.select(".dump-cnt .dumpthumb");
        ArrayList<Item> itemArrayList = new ArrayList<>();

        for(Element element : elements) {
            Item item = new Item();

            item.url = element.attr("href");
            Log.d(TAG, "Parsing '"+item.url+"'");

            item.title = element.select("h1").first().text();
            item.description = element.select("p.description").first().html();
            item.thumbUrl = element.select("img").first().attr("src");
            String rawDate = element.select("date").first().text();
            Date date = new SimpleDateFormat("dd MMMM yyyy kk:ss", new Locale("nl", "NL"))
                    .parse(rawDate);
            item.date = new TimeAgo(context).timeAgo(date);
            item.stats = element.select("p.stats").first().text();
            item.photo = element.select(".foto").size() > 0;
            item.video = element.select(".video").size() > 0;
            item.audio = element.select(".audio").size() > 0;

            Pattern pattern = Pattern.compile(".*kudos:\\s(.*)");
            Matcher matcher = pattern.matcher(item.stats);

            if(matcher.matches()) {
                item.score = Integer.valueOf(matcher.group(1));
            }

            if(item.video) {
                item.imageUrls = new String[]{item.thumbUrl.replace("sq_thumbs", "stills")};
            } else if(item.photo) {
                //get the image itself from it's url.
                //sadly no other way to get full hq image :'(
                Log.d(TAG, "Got image, requesting "+item.url);
                Connection imageConn = Jsoup.connect(item.url);
                // set timeout to 12 seconds.
                // Default is 3, and internet speeds on weak connections take about 12 seconds.
                imageConn.timeout(12000);
                setNSFWCookie(context, imageConn);
                Document imageDocument = imageConn.get();

                ArrayList<String> imgs = new ArrayList<>();
                for(Element img : imageDocument.select("img.player")) {
                    imgs.add(img.attr("src"));
                }
                item.imageUrls = new String[imgs.size()];
                imgs.toArray(item.imageUrls);
            }

            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("upkudo", false)) {
                if(item.score > -1) {
                    itemArrayList.add(item);
                }
            } else {
                itemArrayList.add(item);
            }
        }

        Item[] returnList = new Item[itemArrayList.size()];
        itemArrayList.toArray(returnList);

        saveToCache(context, cacheKey, returnList);

        return returnList;
    }

    public static Item[] getListing(Context context, String path) throws IOException,
            ParseException {
        return API.getListing(0, context, path);
    }

    public static ItemInfo getItemInfo(Item item, Activity context) throws IOException,
            JSONException {
        Connection infoConnection = Jsoup.connect(item.url);
        // set timeout to 12 seconds.
        // Default is 3, and internet speeds on weak connections take about 12 seconds.
        infoConnection.timeout(12000);
        setNSFWCookie(context, infoConnection);
        Document document = infoConnection.get();

        ItemInfo itemInfo = new ItemInfo();
        itemInfo.itemId = document.select("body").first().attr("data-itemid");
        if(item.video) {
            boolean requestHD = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("video_quality", "hd").equals("hd");
            String rawFiles = document.select(".videoplayer").first().attr("data-files");
            rawFiles = new String(Base64.decode(rawFiles, Base64.DEFAULT), "UTF-8");
            JSONObject files = new JSONObject(rawFiles);

            if(files.has("embed")) {
                String embedCode = files.getString("embed");
                String embed[]   = embedCode.split(":");
                String domain    = embed[0];
                String video     = embed[1];
                String file;
                URL embedUrl;

                Log.d(TAG, domain+" video found: "+video);

                if(domain.equals("youtube")) {
                    // get the video info file
                    embedUrl = new URL("http://www.youtube.com/get_video_info?video_id=" + video);
                    HttpURLConnection connection = (HttpURLConnection) embedUrl.openConnection();
                    String video_info = null;

                    try {
                        InputStream in = new BufferedInputStream(connection.getInputStream());
                        video_info = IOUtils.toString(in, Charset.forName("UTF-8")); // assume UTF-8
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        connection.disconnect();
                    }

                    if(video_info == null) {
                        // still nicer than crashing because file is null
                        throw new IOException();
                    }

                    // find the one thing we're looking for
                    Pattern pattern = Pattern.compile("url_encoded_fmt_stream_map=([^&]+)");
                    Matcher matcher = pattern.matcher(video_info);
                    StringBuilder raw_video_urls = new StringBuilder();

                    while(matcher.find()) {
                        raw_video_urls.append(URLDecoder.decode(matcher.group(1), "UTF-8"));
                    }

                    String video_urls = raw_video_urls.toString();

                    // split the values of quality
                    String[] video_url_array = video_urls.split(",");
                    Map<String, String> qualityMap = new HashMap<>();

                    // this has to be the most ugly url splitter I ever wrote. Sorry guys.
                    for(String v_item : video_url_array) {
                        String item_params[] = v_item.split("&");
                        String quality = null;
                        String url = null;

                        for(String param : item_params) {
                            if(param.startsWith("quality")) {
                                quality = param.split("=")[1];
                            } else if(param.startsWith("url")) {
                                url = URLDecoder.decode(param.split("=")[1], "UTF-8");
                            }
                        }

                        if(quality != null && url != null) {
                            qualityMap.put(quality, url);
                        }
                    }

                    if(requestHD) {
                        file = "";
                        if(qualityMap.containsKey("hd720")) {
                            file = qualityMap.get("hd720");
                        } else if(qualityMap.containsKey("medium")) {
                            file = qualityMap.get("medium");
                        } else if(qualityMap.containsKey("small")) {
                            file = qualityMap.get("small");
                        }
                    } else {
                        file = "";
                        if(qualityMap.containsKey("medium")) {
                            file = qualityMap.get("medium");
                        } else if(qualityMap.containsKey("small")) {
                            file = qualityMap.get("small");
                        }
                    }

                } else {
                    // it's not a YouTube video, use else if's to catch other websites.
                    Log.w(TAG, "Unable to parse enmbed code: "+embedCode);
                    // exit with a nice error message in userspace
                    throw new IOException();
                }

                itemInfo.media = file;
            } else {
                // assume Dumpert video
                Log.d(TAG, rawFiles);
                if(requestHD) {
                    itemInfo.media = files.getString("tablet");
                } else {
                    itemInfo.media = files.getString("mobile");
                }
            }
        } else if(item.audio) {
            itemInfo.media = document.select(".dump-player").first().select(".audio").first()
                    .attr("data-audurl");
        }
        return itemInfo;
    }

    public static Comment[] getComments(String itemId, Context context) throws IOException {
        URL url = new URL("https://dumpcomments.geenstijl.nl/"+itemId+".js");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String file = null;
        ArrayList<Comment>newComments = new ArrayList<>();

        try {
            InputStream in = new BufferedInputStream(connection.getInputStream());
            file = IOUtils.toString(in, Charset.forName("iso-8859-1")); // Dumpert uses iso-8859-1
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            connection.disconnect();
        }

        if(file != null) {
            Pattern pattern = Pattern.compile("comments\\.push\\('(<[p|footer|article].*)'\\);");
            Matcher matcher = pattern.matcher(file);
            StringBuilder rawDoc = new StringBuilder();

            while(matcher.find()) {
                rawDoc.append(matcher.group(1));
            }

            Document document = Jsoup.parse(rawDoc.toString());
            ArrayList<Comment> comments = new ArrayList<>();
            Elements elements = document.select("article");

            for(Element element : elements) {
                Comment comment = new Comment();
                comment.id = element.attr("id").substring(1);
                Element p = element.select("p").first();
                comment.content = p != null ? p.html().replace("\\\"", "\"").replace("\\'", "'")
                        .replace("\\&quot;", "") : "";
                String footer = element.select("footer").first().text();

                if(element.select("footer").first().html().contains("title=\"newbie\""))
                    comment.newbie = true;

                StringTokenizer tokenizer = new StringTokenizer(footer, "|");
                comment.author = tokenizer.nextToken().trim();
                comment.time = tokenizer.nextToken().trim();
                comments.add(comment);
            }

            //modlinks
            Pattern modlinksPattern = Pattern.compile("modscr\\.setAttribute\\('src','(.*)'\\)");
            Matcher modlinksMatcher = modlinksPattern.matcher(file);
            if(modlinksMatcher.find()) {
                URL modlinksUrl = new URL("https://"+modlinksMatcher.group(1)
                        .replaceAll("^/+", ""));
                connection = (HttpURLConnection) modlinksUrl.openConnection();
                String modEntry = modlinksUrl.toString().split("\\&entry=")[1];
                String modlinksFile = null;

                try {
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    modlinksFile = IOUtils.toString(in, Charset.forName("iso-8859-1"));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    connection.disconnect();
                }

                if(modlinksFile != null) {
                    //best comments
                    Pattern bestPattern = Pattern
                            .compile("bestcomments = \\[(([0-9]+)(,\\s)?)+\\];");
                    Matcher bestMatcher = bestPattern.matcher(modlinksFile);
                    ArrayList<String> bestComments = new ArrayList<>();
                    while (bestMatcher.find()) {
                        bestComments.add(bestMatcher.group(1));
                    }

                    //first loop adds best comments @ top
                    for (Comment comment : comments) {
                        if (bestComments.contains(comment.id)) {
                            comment.best = true;
                            comment.entry = modEntry;
                            newComments.add(comment);
                        }
                    }

                    //second loop adds the comments that aren't best
                    for (Comment comment : comments) {
                        if (!bestComments.contains(comment.id)) {
                            comment.entry = modEntry;
                            newComments.add(comment);
                        }
                    }

                    //scores
                    Pattern scoresPattern = Pattern
                            .compile("moderation\\['([0-9]*)'] = '(-?[0-9]*)';");
                    Matcher scoresMatcher = scoresPattern.matcher(modlinksFile);
                    while (scoresMatcher.find()) {
                        String id = scoresMatcher.group(1);
                        String score = scoresMatcher.group(2);
                        for (Comment comment : newComments) {
                            if (comment.id.equals(id)) {
                                Integer index = newComments.indexOf(comment);
                                comment.score = Integer.parseInt(score);
                                newComments.set(index, comment);
                            }
                        }
                    }
                }
            }
        }

        // if the page could not be downloaded no comments will show up.
        if(newComments.size() == 0) {
            Log.d(TAG, "no comments found, injecting placeholder");

            Comment placeholder = new Comment();
            placeholder.content = context.getString(R.string.no_comments);
            placeholder.author = "";
            placeholder.id = "";
            placeholder.time = "";
            placeholder.entry = "";
            placeholder.best = false;
            placeholder.score = null;

            newComments.add(placeholder);
        }

        Comment[] returnArr = new Comment[newComments.size()];
        newComments.toArray(returnArr);
        return returnArr;
    }

    // yes, this actually works... It does not update the screen, but it votes.
    public static void vote(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final URL vote = new URL(url);
                    final HttpURLConnection connection = (HttpURLConnection) vote.openConnection();

                    connection.setRequestProperty(
                                    "Accept", "application/json, text/javascript, */*; q=0.01");
                    connection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // very ugly, and completely duct-taped from Login.java
    public static boolean reply(Context context, String itemID, String entryID, String message) throws IOException {
        URL url = new URL("ttp://app.steylloos.nl/mt-comments.fcgi");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String session = context.getSharedPreferences("dumpert", 0).getString("session", "");
        HashMap<String, String> formData = new HashMap<>();

        if(session.equals("")) {
            Log.wtf(TAG, "Session is not set! Method could not have been called!");
            return false;
        }

        formData.put("static", "http://www.dumpert.nl/return.php?target="+itemID); // 87327_984327
        formData.put("entry_id", entryID); // item entry id
        formData.put("text", message);
        formData.put("post", "+Post+");

        connection.setRequestMethod("POST");
        connection.setInstanceFollowRedirects(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Cookie", session);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream())));
        Uri.Builder builder = new Uri.Builder();
        Iterator<Map.Entry<String, String>> data = formData.entrySet().iterator();

        while(data.hasNext()) {
            Map.Entry<String, String> param = data.next();
            builder.appendQueryParameter(param.getKey(), param.getValue());
            data.remove();
        }

        bw.write(builder.build().getEncodedQuery());
        bw.flush();
        bw.close();

        connection.connect();

        // @todo: return actual result
        return false;
    }

}
