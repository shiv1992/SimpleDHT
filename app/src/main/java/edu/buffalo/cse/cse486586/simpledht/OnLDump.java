package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import org.json.JSONException;


public class OnLDump implements OnClickListener {

    private static final String TAG = OnLDump.class.getName();
    private static final int TEST_CNT = 50;
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    private final TextView mTextView;
    private final ContentResolver mContentResolver;
    private final Uri mUri;
    private final ContentValues[] mContentValues;



    public OnLDump(TextView _tv, ContentResolver _cr) {
        mTextView = _tv;
        mContentResolver = _cr;
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        mContentValues = initTestValues();
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    private ContentValues[] initTestValues() {
        ContentValues[] cv = new ContentValues[TEST_CNT];
        for (int i = 0; i < TEST_CNT; i++) {
            cv[i] = new ContentValues();
            cv[i].put(KEY_FIELD, "key" + Integer.toString(i));
            cv[i].put(VALUE_FIELD, "val" + Integer.toString(i));
        }

        return cv;
    }

    @Override
    public void onClick(View v) {
        new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class Task extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (testInsert()) {
                publishProgress("Insert success\n");
            } else {
                publishProgress("Insert fail\n");
                return null;
            }

            if (testQuery()) {
                publishProgress("Query success\n");
            } else {
                publishProgress("Query fail\n");
            }

            /*
            if (testDelete()) {
                publishProgress("Delete success\n");
            } else {
                publishProgress("Delete fail\n");
            }
*/

            message msg=new message();
            msg.key="Hey";
            msg.mess="You";

            String val="";
            try {
                val=msg.toJString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        /*
            message mm=new message();

            try {
                mm=mm.toJMsg(val);

                Log.v("MESSAGE TEST :","MMM");
                Log.v("KEY M :",mm.key);
                Log.v("VALUE M :",mm.mess);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        */

            return null;
        }

        protected void onProgressUpdate(String...strings) {
            mTextView.append(strings[0]);

            return;
        }

        private boolean testInsert() {
            try {
                for (int i = 0; i < TEST_CNT; i++) {
                    mContentResolver.insert(mUri, mContentValues[i]);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }

            return true;
        }

        private boolean testQuery() {
            try {

                //Test Ldump
                Cursor ress = mContentResolver.query(mUri, null,"@", null, null);


                Log.v("DATABASE", "CURSOR");
                String str;
                str = DatabaseUtils.dumpCursorToString(ress);
                //Log.v("DATABASE :",str);
                return true;


            } catch (Exception e) {
                return false;
            }

            //return true;
        }


        private boolean testDelete() {
            try {

                //Test Ldump Delete
                int ress = mContentResolver.delete(mUri,"@",null);

                Log.v("DATABASE DEL", String .valueOf(ress));
                return true;


            } catch (Exception e) {
                return false;
            }

            //return true;
        }
    }
}
