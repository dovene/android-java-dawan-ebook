package com.dov.ebookjava.books;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.dov.ebookjava.database.AppRoomDatabase;
import com.dov.ebookjava.database.BookEntity;
import com.dov.ebookjava.database.BookMapper;
import com.dov.ebookjava.model.BooksResponse;
import com.dov.ebookjava.webservice.BooksApi;
import com.dov.ebookjava.webservice.BooksService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BooksViewModel extends AndroidViewModel {
    private final MutableLiveData<List<BooksResponse.Book>> _books = new MutableLiveData<>();
    public LiveData<List<BooksResponse.Book>> books = _books;

    private MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> loading = _loading;


    private MutableLiveData<Boolean> _isAddedToFavorite = new MutableLiveData<>();
    public LiveData<Boolean> isAddedToFavorite = _isAddedToFavorite;

    public BooksViewModel(@NonNull Application application) {
        super(application);
    }

    public void getBooks(String search) {
        if (search.isEmpty()) {
            _error.postValue("Il faut remplir le champ de recherche");
            return;
        }
        _loading.postValue(true);
        BooksService booksService = BooksApi.getInstance().getClient().create(BooksService.class);
        Call<BooksResponse> call = booksService.getBooks(search);

        call.enqueue(new retrofit2.Callback<BooksResponse>() {

            @Override
            public void onResponse(Call<BooksResponse> call, Response<BooksResponse> response) {
                if (response.body() != null && response.body().getBooks()!= null) {
                    _books.postValue(response.body().getBooks());
                } else {
                    _error.postValue("Une erreur s'est produite");
                }
                _loading.postValue(false);
            }

            @Override
            public void onFailure(Call<BooksResponse> call, Throwable throwable) {
                _error.postValue(throwable.getMessage());
                _loading.postValue(false);
            }
        });
    }

    public void toggleFavorite(BooksResponse.Book book) {
        new Thread(() -> {
            AppRoomDatabase db = AppRoomDatabase.getInstance(getApplication());
            if (isFavorite(book.getId())) {
                db.userDao().deleteByIdString(book.getId());
                _isAddedToFavorite.postValue(false);
            } else {
                db.userDao().insert(BookMapper.toEntity(book));
                _isAddedToFavorite.postValue(true);
            }
        }).start();
    }

    public boolean isFavorite(String id) {
        AppRoomDatabase db = AppRoomDatabase.getInstance(getApplication());
        BookEntity book = db.userDao().getBookById(id);
        return book != null;
    }


}