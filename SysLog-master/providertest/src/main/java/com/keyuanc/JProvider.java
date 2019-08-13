package com.keyuanc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class JProvider extends ContentProvider {
    private static final UriMatcher matcher;
    private static final int code = 0;
    private static ArrayList<Student> students = new ArrayList<>();
    private static MyCursor cursor;


    static {
        cursor = new MyCursor();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI("com.keyuanc.provider", "student", code);
    }

    @Override
    public boolean onCreate() {
        //初始化数据
        for (int pos = 0; pos < 5; pos++) {
            students.add(new Student(pos, "name" + pos));
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        int code = matcher.match(uri);

        if (code == 0) {
            cursor.updateUserData(students);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
