package io.jari.dumpert.fragments;

/**
 * JARI.IO
 * Date: 15-1-15
 * Time: 18:21
 */
public class SearchFragment extends ListingFragment {
    public String query = "";

    @Override
    public String getCurrentPath() {
        return "/search/ALL/"+query+"/";
    }

}
