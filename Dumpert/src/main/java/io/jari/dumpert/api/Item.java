package io.jari.dumpert.api;

import java.io.Serializable;

/**
 * JARI.IO
 * Date: 11-12-14
 * Time: 22:17
 */
public class Item implements Serializable {
    public String title;
    public boolean photo;
    public boolean video;
    public boolean audio;
    public int score;
    public String thumbUrl;
    public String[] imageUrls;
    public String url;
    public String description;
    public String date;
    public String stats;
}
