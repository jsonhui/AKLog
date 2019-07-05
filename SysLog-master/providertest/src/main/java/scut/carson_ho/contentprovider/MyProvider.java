package scut.carson_ho.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class MyProvider extends ContentProvider {
    private static String TAG = "MyProvider";

    private Context mContext;
    DBHelper mDbHelper = null;
    SQLiteDatabase db = null;
    public static final String AUTOHORITY = "scut.carson_ho.contentprovider";
    // 设置ContentProvider的唯一标识

    public static final int User_Code = 1;
    public static final int Job_Code = 2;

    //设置MIME
    public static final String CONTENT_FIRST_TYPE = "vnd.android.cursor.dir/first";
    public static final String CONTENT_SECOND_TYPE = "vnd.android.cursor.item/second";

    // UriMatcher类使用:在ContentProvider 中注册URI
    private static final UriMatcher mMatcher;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 初始化
        mMatcher.addURI(AUTOHORITY, "user", User_Code);
        mMatcher.addURI(AUTOHORITY, "job", Job_Code);
        // 若URI资源路径 = content://scut.carson_ho.contentprovider/user ，则返回注册码User_Code
        // 若URI资源路径 = content://scut.carson_ho.contentprovider/job ，则返回注册码Job_Code
    }

    // 以下是ContentProvider的6个方法

    /**
     * 初始化ContentProvider
     */
    @Override
    public boolean onCreate() {

        mContext = getContext();
        // 在ContentProvider创建时对数据库进行初始化
        // 运行在主线程，故不能做耗时操作,此处仅作展示
        mDbHelper = new DBHelper(getContext());
        db = mDbHelper.getWritableDatabase();
        UriMatcher mnMatcher = mMatcher;
        // 初始化两个表的数据(先清空两个表,再各加入一个记录)
        db.execSQL("delete from user");
        db.execSQL("insert into user values(1,'Carson');");
        db.execSQL("insert into user values(2,'Kobe');");

        db.execSQL("delete from job");
        db.execSQL("insert into job values(1,'Android');");
        db.execSQL("insert into job values(2,'iOS');");

        return true;
    }

    /**
     * 添加数据
     */

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
        // 该方法在最下面
        String table = getTableName(uri);

        // 向该表添加数据
        db.insert(table, null, values);

        // 当该URI的ContentProvider数据发生变化时，通知外界（即访问该ContentProvider数据的访问者）
        mContext.getContentResolver().notifyChange(uri, null);

//        // 通过ContentUris类从URL中获取ID
//        long personid = ContentUris.parseId(uri);
//        System.out.println(personid);

        return uri;
    }

    /**
     * 查询数据
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
        // 该方法在最下面
        String table = getTableName(uri);

//        // 通过ContentUris类从URL中获取ID
//        long personid = ContentUris.parseId(uri);
//        System.out.println(personid);

        // 查询数据
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    /**
     * 更新数据
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // 由于不展示,此处不作展开
        return 0;
    }

    /**
     * 删除数据
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // 由于不展示,此处不作展开
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (mMatcher.match(uri)) {
            case User_Code: {
                return CONTENT_FIRST_TYPE;
            }
            case Job_Code: {
                return CONTENT_SECOND_TYPE;
            }
        }
        return null;
    }

    /**
     * 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
     */
    private String getTableName(Uri uri) {
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case User_Code:
                tableName = DBHelper.USER_TABLE_NAME;
                break;
            case Job_Code:
                tableName = DBHelper.JOB_TABLE_NAME;
                break;
        }
        return tableName;
    }
}

