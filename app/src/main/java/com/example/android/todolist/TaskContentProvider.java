package com.example.android.todolist;

/**
 * Created by Swapnil on 24-12-2016.
 */
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;


public class TaskContentProvider extends ContentProvider {

    private TaskDbHelper mTaskDbHelper;
    public static final int TASKS = 100;
    public static final int TASK_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //Add Directory
        uriMatcher.addURI(TaskContract.AUTHORITY,TaskContract.PATH_TASKS,TASKS);
        //single item
        uriMatcher.addURI(TaskContract.AUTHORITY,TaskContract.PATH_TASKS+"/#",TASK_WITH_ID);
        return uriMatcher;
    }

    /* onCreate() is where you should initialize anything you’ll need to setup
    your underlying data source.
    In this case, you’re working with a SQLite database, so you’ll need to
    initialize a DbHelper to gain access to it.
     */
    @Override
    public boolean onCreate() {
        // [Hint] Declare the DbHelper as a global variable
        Context context = getContext();
        mTaskDbHelper = new TaskDbHelper(context);
        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch(match){
            case TASKS:
                long id = db.insert(TaskContract.TaskEntry.TABLE_NAME,null,values);
                if(id > 0){
                     returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI,id);
                }
                else
                {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                 break;
            default:
                    throw new UnsupportedOperationException("Not yet implemented");
        }
        //Notify resolver that uri has changed
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;

    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch(match){
            case TASKS:
                retCursor = db.query(TaskContract.TaskEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);

                break;
            case TASK_WITH_ID:
                //Uri =  <scheme>://<authority>/taska(index 0 )/#(index 1)
                String id = uri.getPathSegments().get(1);
                //Selection is the _ID Column = ? and selection args is the row id from the URI
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[] {id}; // ? is a placeholder and it asks for this from selection Args
                retCursor = db.query(TaskContract.TaskEntry.TABLE_NAME,projection,mSelection,mSelectionArgs,null,null,sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);

        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int retRows;
        switch(match){
            case TASKS:
                retRows = db.delete(TaskContract.TaskEntry.TABLE_NAME,selection,selectionArgs);

                break;
            case TASK_WITH_ID:
                //Uri =  <scheme>://<authority>/taska(index 0 )/#(index 1)
                String id = uri.getPathSegments().get(1);
                //Selection is the _ID Column = ? and selection args is the row id from the URI
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[] {id}; // ? is a placeholder and it asks for this from selection Args
                retRows = db.delete(TaskContract.TaskEntry.TABLE_NAME,mSelection,mSelectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        if (retRows != 0) {
                        // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
              }

        return retRows;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        //Keep track of if an update occurs
        int tasksUpdated;

        // match code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case TASK_WITH_ID:
                //update a single task by getting the id
                String id = uri.getPathSegments().get(1);
                //using selections
                tasksUpdated = mTaskDbHelper.getWritableDatabase().update(TaskContract.TaskEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (tasksUpdated != 0) {
            //set notifications if a task was updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of tasks updated
        return tasksUpdated;
    }


    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                // directory
                return "vnd.android.cursor.dir" + "/" + TaskContract.AUTHORITY + "/" + TaskContract.PATH_TASKS;
            case TASK_WITH_ID:
                // single item type
                return "vnd.android.cursor.item" + "/" + TaskContract.AUTHORITY + "/" + TaskContract.PATH_TASKS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

}