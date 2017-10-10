package com.example.android.booksearch;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Irmantas Čivilis on 2017.06.22.
 * <p>
 * A custom ArrayAdapter displays a list of Book objects.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(@NonNull Activity context, @NonNull List<Book> books) {
        super(context, 0, books);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //Using View Holder for better performance
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Book book = getItem(position);

        String bookTitle = book.getTitle();
        String bookAuthor = book.getAuthor();

        holder.titleTextView.setText(bookTitle);
        holder.authorTextView.setText(bookAuthor);

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.title_text_view) TextView titleTextView;
        @BindView(R.id.author_text_view) TextView authorTextView;

        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
