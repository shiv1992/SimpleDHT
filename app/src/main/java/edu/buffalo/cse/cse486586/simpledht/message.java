package edu.buffalo.cse.cse486586.simpledht;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by shivang on 3/27/16.
 */
public class message {

    public String key;
    public String mess;
    public String type;
    public String port;
    public HashMap<String,String> hm;


    public String toJString() throws JSONException {


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("KEY",this.key);
        jsonObj.put("VALUE", this.mess);
        jsonObj.put("TYPE", this.type);
        jsonObj.put("PORT", this.port);

        hm=new HashMap<String, String>();

        JSONObject innerJsonObj = new JSONObject();
        for(String strKey:hm.keySet()){
            innerJsonObj.put(strKey,hm.get(strKey));
        }

        jsonObj.put("CUR",innerJsonObj);
        return jsonObj.toString();
    }

    //Cursor String
    public String toJString(Cursor cur,HashMap<String,String> nm) throws JSONException {


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("PORT", this.port);
        jsonObj.put("TYPE", this.type);
        jsonObj.put("KEY", this.key);
        jsonObj.put("VALUE", this.mess);

        hm=new HashMap<String, String>();
        //cur.moveToFirst();
        if(nm==null) {
            Log.v("CUR CUNVERT","Enter");
            while (cur.moveToNext()) {
                Log.v("CUR CUNVERT","1");
                hm.put(cur.getString(cur.getColumnIndex("key")), cur.getString(cur.getColumnIndex("value")));
            }
            JSONObject innerJsonObj = new JSONObject();
            for(String strKey:hm.keySet()){
                Log.v("CUR CUNVERT","2");
                innerJsonObj.put(strKey,hm.get(strKey));
                Log.v("KEY", strKey);
                Log.v("VAL",hm.get(strKey));
            }
            jsonObj.put("CUR",innerJsonObj);

        }
        else
        {
            while (cur.moveToNext()) {
                Log.v("CUR CUNVERT","1N");
                nm.put(cur.getString(cur.getColumnIndex("key")), cur.getString(cur.getColumnIndex("value")));
            }
            JSONObject innerJsonObj = new JSONObject();
            for(String strKey:nm.keySet()){
                Log.v("CUR CUNVERT","2N");
                innerJsonObj.put(strKey, nm.get(strKey));
                Log.v("KEY", strKey);
                Log.v("VAL", nm.get(strKey));
            }
            jsonObj.put("CUR",innerJsonObj);


        }



        return jsonObj.toString();
    }

    public void toJMsg(String in) throws JSONException {


        JSONObject obj = new JSONObject(in);

        String key= obj.getString("KEY");
        String value= obj.getString("VALUE");
        String type= obj.getString("TYPE");
        String port= obj.getString("PORT");


        JSONObject jObject = obj.getJSONObject("CUR");
        Log.v("OUT",jObject.toString());

        Iterator<String> keys = jObject.keys();
         hm=new HashMap<String, String>();

        while(keys.hasNext()) {
            String K = keys.next();
            Log.v("OUT K",K);
            String V = jObject.getString(K);
            Log.v("OUT V",V);
            hm.put(K, V);
        }

        this.hm=hm;
        this.key=key;
        this.mess=value;
        this.type=type;
        this.port=port;


    }


}
