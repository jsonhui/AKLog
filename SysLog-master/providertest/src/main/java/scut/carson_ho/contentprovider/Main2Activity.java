package scut.carson_ho.contentprovider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ContentResolver resolver = getContentResolver();
        Uri uri = Uri.parse("content://com.keyuanc.provider/student");
        // 通过ContentResolver 向ContentProvider中查询数据
        Cursor cursor2 = resolver.query(uri, new String[]{"id", "name"}, null, null, null);
        while (cursor2.moveToNext()) {
            Log.d("Jason", "query student:" + cursor2.getInt(0) + " " + cursor2.getString(1));
            // 将表中数据全部输出
        }
        cursor2.close();
    }
}
