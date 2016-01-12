package io.jari.dumpert.api;

import java.io.Serializable;

/**
 * JARI.IO
 * Date: 15-12-14
 * Time: 13:00
 */
public class Comment implements Serializable {
    public String content;
    public String author;
    public String id;
    public String time;
    public String entry;
    public boolean best = false;
    public boolean newbie = false;
    public Integer score = 0;
}
