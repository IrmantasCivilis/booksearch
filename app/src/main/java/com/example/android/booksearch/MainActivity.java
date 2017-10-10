package com.example.android.booksearch;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    public static final String LOG_TAG = MainActivity.class.getName();
    private static final int BOOK_LOADER_ID = 1;
    private static final String GOOGLE_BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    String userQuery = "";
    String bookQueryUrl = "";
    EditText mEditText;
    boolean isTheFirstTime = true;
    boolean isConnected;
    private BookAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private View loadingIndicator;

    public MainActivity() {
        super();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("Is the first time?", isTheFirstTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the app is open for a first time
        if (savedInstanceState != null) {
            isTheFirstTime = savedInstanceState.getBoolean("Is the first time?");
        }

        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.edit_text);

        // Set OnEditorAction Listener to perform a search via search button in a keyboard.
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    mAdapter.clear();

                    // Close the keyboard after search button is clicked.
                    mEditText.clearFocus();
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                    // Get user's input and convert it to the string
                    userQuery = mEditText.getText().toString();

                    // Create a final query of a book and encode it
                    try {
                        bookQueryUrl = GOOGLE_BOOKS_REQUEST_URL + URLEncoder.encode(userQuery, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("utf8", "conversion", e);
                    }

                    checkConnectivity();
                    if (isConnected) {
                        LoaderManager loaderManager = getLoaderManager();
                        loaderManager.restartLoader(BOOK_LOADER_ID, null, MainActivity.this);
                        showLoadingIndicator();
                    } else {
                        showNoConnection();
                    }
                    return true;
                }
                return false;
            }
        });

        ListView bookListView = (ListView) findViewById(R.id.books_list);

        mEmptyStateTextView = (TextView) findViewById((R.id.empty_view));

        bookListView.setEmptyView(mEmptyStateTextView);

        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        bookListView.setAdapter(mAdapter);

        // Set onClickListener on a list item. Create an intent which leads to a book website for
        // more info
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Book currentBook = mAdapter.getItem(position);
                Uri bookUri = Uri.parse(currentBook.getInfoUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);
                startActivity(websiteIntent);
            }
        });

        loadingIndicator = findViewById(R.id.loading_indicator);

        if (isTheFirstTime) {
            // if the app is open for a first time
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.first_time);
            isTheFirstTime = false;
        } else {
            mEmptyStateTextView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            checkConnectivity();
            if(isConnected){
                LoaderManager loaderManager = getLoaderManager();
                loaderManager.initLoader(BOOK_LOADER_ID, null, this);
                showLoadingIndicator();
        } else {
                showNoConnection();
            }
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        // Create a new loader for the given URL
        return new BookLoader(this, bookQueryUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        loadingIndicator.setVisibility(View.GONE);

        // Clear the adapter of previous book data
        mAdapter.clear();

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
        } else {
            mEmptyStateTextView.setText(R.string.no_books_found);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    // Check network connectivity
    public boolean checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public void showLoadingIndicator() {
        mEmptyStateTextView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    public void showNoConnection() {
        loadingIndicator.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.no_internet_connection);
    }

}