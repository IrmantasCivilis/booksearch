package com.example.android.booksearch;

/**
 * Created by Irmantas Čivilis on 2017.06.22.
 * <p>
 * Book object holds all the relevant information for a single book.
 */

public class Book {

    private String mTitle;
    private String mAuthor;
    private String mInfoUrl;

    public Book(String title, String author, String infoUrl) {
        mTitle = title;
        mAuthor = author;
        mInfoUrl = infoUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getInfoUrl() {
        return mInfoUrl;
    }
}
