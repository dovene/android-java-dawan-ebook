package com.dov.ebookjava.books;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dov.ebookjava.R;
import com.dov.ebookjava.database.AppRoomDatabase;
import com.dov.ebookjava.database.BookEntity;
import com.dov.ebookjava.model.BooksResponse;
import com.dov.ebookjava.model.Response;

public class BooksViewHolder extends RecyclerView.ViewHolder {
    AppCompatTextView titleTV;
    AppCompatTextView authorTV;
    AppCompatTextView descriptionTV;
    AppCompatImageView imageIV;
    AppCompatImageView bookmarkIV;

    public BooksViewHolder(@NonNull View itemView) {
        super(itemView);
        titleTV = itemView.findViewById(R.id.title_tv);
        authorTV = itemView.findViewById(R.id.author_tv);
        descriptionTV = itemView.findViewById(R.id.description_tv);
        imageIV = itemView.findViewById(R.id.image_iv);
        bookmarkIV = itemView.findViewById(R.id.bookmark_iv);
    }

    public void bind(Response.Item bookInfo, BooksRecyclerViewAdapter.OnItemAction onItemAction) {
        titleTV.setText(bookInfo.volumeInfo.title);
        if (bookInfo.volumeInfo.authors != null && !bookInfo.volumeInfo.authors.isEmpty()) {
            authorTV.setText(bookInfo.volumeInfo.authors.get(0));
        }
        descriptionTV.setText(bookInfo.volumeInfo.description);
        bookmarkIV.setImageResource(R.drawable.baseline_favorite_24);
        new Thread(() -> {
            AppRoomDatabase db = AppRoomDatabase.getInstance(itemView.getContext());
            BookEntity book = db.userDao().getBookById(bookInfo.id);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (book == null) {
                    bookmarkIV.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.white));
                } else {
                    bookmarkIV.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.orange));
                }
            });
        }
        ).start();

        if (bookInfo.volumeInfo.imageLinks != null) {
            Glide.with(imageIV.getContext()).load(bookInfo.volumeInfo.imageLinks.smallThumbnail).into(imageIV);
        }
        bookmarkIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemAction.onFavoriteClick(bookInfo);
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemAction.onShowDetails(bookInfo);
            }
        });
    }
}