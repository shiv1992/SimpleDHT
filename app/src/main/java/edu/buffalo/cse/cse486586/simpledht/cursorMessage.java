package edu.buffalo.cse.cse486586.simpledht;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by shivang on 4/2/16.
 */
public class cursorMessage {

    public String port;
    public HashMap<String,String> hm;

    public String toJString(Cursor cur,HashMap<String,String> nm) throws JSONException {


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("PORT", this.port);

        hm=new HashMap<String, String>();
        cur.moveToFirst();
        if(nm==null) {
            while (cur.moveToNext()) {
                hm.put(cur.getString(cur.getColumnIndex("key")), cur.getString(cur.getColumnIndex("value")));
            }

            jsonObj.put("CUR", hm);
        }
        else
        {
            while (cur.moveToNext()) {
                nm.put(cur.getString(cur.getColumnIndex("key")), cur.getString(cur.getColumnIndex("value")));
            }

            jsonObj.put("CUR", nm);

        }

        return jsonObj.toString();
    }

    public void toJMsg(String in) throws JSONException {


        JSONObject obj = new JSONObject(in);

        String port= obj.getString("KEY");
        String str= obj.getString("CUR");

        this.port=port;
        JSONObject jObject = new JSONObject(str);

        Iterator<?> keys = jObject.keys();

        while( keys.hasNext() ){

            String key = (String)keys.next();
            String value = jObject.getString(key);
            this.hm.put(key, value);

        }


    }
}
