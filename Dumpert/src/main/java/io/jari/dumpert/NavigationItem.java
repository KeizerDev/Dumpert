package io.jari.dumpert;

import android.graphics.drawable.Drawable;

/**
 * JARI.IO
 * Date: 22-12-14
 * Time: 2:07
 */
public class NavigationItem {
    public String title;
    public Drawable drawable;
    public Main.NavigationItemCallback callback;
    public boolean selected = false;
}
