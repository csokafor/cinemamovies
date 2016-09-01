package com.chinedusokafor.silverbirdmoviis.rss;

import java.io.Serializable;

/**
 * Created by cokafor on 1/22/2015.
 */
public class MovieItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
