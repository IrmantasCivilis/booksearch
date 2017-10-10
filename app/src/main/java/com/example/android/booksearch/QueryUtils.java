package com.example.android.booksearch;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.booksearch.MainActivity.LOG_TAG;

/**
 * Created by Irmantas Čivilis on 2017.06.22.
 * <p>
 * Helper methods related to requesting and receiving book data from Google Books.
 */

public final class QueryUtils {

    private static final String KEY_ITEMS = "items";
    private static final String KEY_VOLUME_INFO = "volumeInfo";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "authors";
    private static final String KEY_INFO_LINK = "infoLink";

    private QueryUtils() {
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */

    private static List<Book> extractFeaturesFromJson(String bookJSON) {

        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Extract features from JSON object
        try {

            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            //if (baseJsonResponse.has(KEY_ITEMS)) {
                JSONArray booksArray = baseJsonResponse.optJSONArray(KEY_ITEMS);
                for (int i = 0; i < booksArray.length(); i++) {
                    JSONObject currentBook = booksArray.getJSONObject(i);
                    JSONObject volumeInfo = currentBook.getJSONObject(KEY_VOLUME_INFO);
                    String bookTitle = volumeInfo.getString(KEY_TITLE);
                    String authors = "";
                    //if (volumeInfo.has(KEY_AUTHOR)) {
                        JSONArray bookAuthors = volumeInfo.optJSONArray(KEY_AUTHOR);
                        if (bookAuthors != null) {
                            for (int j = 0; j < bookAuthors.length(); j++) {
                                authors += bookAuthors.getString(j) + ", ";
                            }
                            authors = authors.substring(0, authors.length() - 2);
                        }//}
                    else {
                        authors = "Unknown author";
                    }
                    String url = volumeInfo.getString(KEY_INFO_LINK);
                    Book book = new Book(bookTitle, authors, url);
                    books.add(book);
                }
            //} else {
            //    books = null;
            //}

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }
        return books;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Query the Google books dataset and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<Book> books = extractFeaturesFromJson(jsonResponse);

        return books;

    }


}
