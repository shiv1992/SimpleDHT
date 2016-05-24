package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;


import org.json.JSONException;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public class SimpleDhtProvider extends ContentProvider {


    public static final String KEY_FIELD = "key";
    public static final String VALUE_FIELD = "value";
    public static SQLiteDatabase db=null;
    public static DbHelper dbS;
    public static String TableName = "myTable";
    static final int SERVER_PORT = 10000;
    public static Uri uri = Uri.parse("content://edu.buffalo.cse.cse486586.simpledht.provider/myTable");
    public static int KEY=0;
    public static String TAG="AA";

    public static String[] Node={"5554","5556","5558","5560","5562"};
    public static String[] NodeH={"","","","",""};
    public static String[] Port={"11108","11112","11116","11120","11124"};
    public static boolean[] Stat={false,false,false,false,false};
    public static String Status="00000";
    public static int POS=0;
    private static String myPort="";
    public static String pred;
    public static String succ;
    public static int INIT=0;

    public static int STARFLAG=0; // Flag to set if the avd was the initiator for *
    public static message mesg;
    public static message TTT;


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        Log.e("1","Before");
        db = dbS.getReadableDatabase();
        Log.e("3", "Middle");

        queryBuilder.setTables(TableName);
        Log.e("4", "Again middle");

        Cursor cr;
        db.execSQL("DELETE FROM "+TableName);
        cr=db.rawQuery("SELECT * FROM "+TableName,null);
        Log.v("DELETE COUNT",String.valueOf(cr.getCount()));
        return cr.getCount();
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        Log.e("ANS", values.toString());
        db = dbS.getWritableDatabase();
        String key = (String)values.get("key");
        String value = (String)values.get("value");

        // Check for Hash Value
        try {
            String MHash = genHash(key);
            String NodeHash = "";
            String FNodeHash="";
            int Fval=0;
            int h;
            if(POS==0) {

                for (h=4;h>=0;h--) {
                    if (Port[h].equals(pred)) {
                        Log.v("FNODE","TRUE");
                        break;
                    }
                    else{
                        Log.v("FNODE","FALSE");
                    }
                }

                Log.v("STAT",Node[h]);
                Log.v("STATUS",Port[h]);
                Log.v("FNODE",String.valueOf(h));
                FNodeHash = genHash(Node[h]);

                Fval=MHash.compareTo(FNodeHash);
            }
            for (h=0;h<5;h++) {
                if (Port[h].equals(myPort)) {
                    Log.v("FNODE","TRUE");
                    break;
                }
                else{
                    Log.v("FNODE","FALSE");
                }
            }

            NodeHash=genHash(Node[h]);

            int val=MHash.compareTo(NodeHash);

            // Check Position for Insert Value
            if(INIT==0)
            {
                Log.v("INSERT INIT :", "NOW");
                Log.v("VAL :", String.valueOf(val));
                Log.v("FVAL :", String.valueOf(Fval));
                Log.v("INIT :", String.valueOf(INIT));
                Log.v("KEY : ", key);
                Log.v("Inserted in", myPort);
                db.insertWithOnConflict(TableName, null, values, CONFLICT_REPLACE);

            }

            else if(POS==0) {
                if ( val <= 0 || Fval > 0){

                    Log.v("INSERT 1 :", "NOW");
                    Log.v("VAL :", String.valueOf(val));
                    Log.v("FVAL :", String.valueOf(Fval));
                    Log.v("INIT :", String.valueOf(INIT));
                    Log.v("KEY : ", key);
                    Log.v("MHASH",MHash);
                    Log.v("NODEHASH",NodeHash);
                    Log.v("NODEHASH 1",genHash(Node[1]));
                    Log.v("NODEHASH 2",genHash(Node[2]));
                    Log.v("FNODEHASH",FNodeHash);
                    Log.v("Inserted in", myPort);
                    db.insertWithOnConflict(TableName, null, values, CONFLICT_REPLACE);

                }
                //Pass message to Successor
                else{
                    //Client task to send message to successor

                    message tmp=new message();
                    tmp.type="2";
                    tmp.key=key;
                    tmp.port=myPort;
                    tmp.mess=value;
                    String send;
                    send=tmp.toJString();
                    Log.v("Sent to",succ);
                    new ClientTaskSucc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send, myPort);

                }
            }
            else if(POS>0){

                int j=0;
                for(j=0;j<5;j++)
                {
                    if(Port[j].equals(pred))
                    {
                        break;
                    }
                }
                int valN=MHash.compareTo(genHash(Node[j]));

                if ( valN > 0 && val <= 0 ) {

                    Log.v("INSERT 2 :", "NOW");
                    Log.v("VAL :", String.valueOf(val));
                    Log.v("FVAL :", String.valueOf(Fval));
                    Log.v("INIT :", String.valueOf(INIT));
                    Log.v("KEY : ", key);
                    Log.v("Inserted in", myPort);
                    db.insertWithOnConflict(TableName, null, values, CONFLICT_REPLACE);

                }
                //Pass message to Succesessor
                else{
                    //Client task to send message to successor

                    message tmp=new message();
                    tmp.type="2";
                    tmp.key=key;
                    tmp.port=myPort;
                    tmp.mess=value;
                    String send;
                    send=tmp.toJString();
                    Log.v("Sent to",succ);
                    new ClientTaskSucc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send, myPort);

                }

            }


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        Log.e("Ins","Ins");
        dbS = new DbHelper(this.getContext());


        //Telephony
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        //Setting myPort as Pred and Succ at Start
        pred=myPort;
        succ=myPort;

        // SORT Acc Hash Valve
        int i,j;
        for(i=0;i<5;i++)
        {
            try {
                NodeH[i]=genHash(Node[i]);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        String str="";
        for(i=0;i<5;i++)
        {
            for(j=i+1;j<5;j++)
            {
                if(NodeH[i].compareTo(NodeH[j]) >0)
                {
                    str=NodeH[i];
                    NodeH[i]=NodeH[j];
                    NodeH[j]=str;

                    str=Node[i];
                    Node[i]=Node[j];
                    Node[j]=str;

                    str=Port[i];
                    Port[i]=Port[j];
                    Port[j]=str;
                }

            }
        }

        for(i=0;i<5;i++){
         Log.v("TT NODE "+i," :"+Node[i]);
            Log.v("TT NODEH "+i," :"+NodeH[i]);
            Log.v("TT PORT "+i," :"+Port[i]);


        }


        //Set Position of port
        /*for(int z=0;z<5;z++)
        {
            if(Port[z].equals(myPort)){
                POS=z;
                Log.v("POS : ",String.valueOf(POS));
                break;
            }
        }
*/
        String NodeHash="";

        try {
            NodeHash = genHash(Node[POS]);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Log.v("PORT HASH : ", NodeHash);
        Log.v("PORT PRED : ", pred);
        Log.v("PORT SUCC : ",succ);

        //Send Activation to port 5554 Client Task
        new ClientTaskActive().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "ACTIVE", myPort);

        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e("TAG", "Can't create a ServerSocket");
            return false;
        }
        return false;
    }




    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        Log.e("1","Before");
        db = dbS.getReadableDatabase();
        Log.e("3", "Middle");

        queryBuilder.setTables(TableName);
        Log.e("4", "Again middle");

        Cursor cursor;
        MatrixCursor mat = null;
        MergeCursor mgr=null;
        

        //For one Instance
        if(INIT==0)// && (selection.equals("@") || selection.equals("*")) )
        {
            Log.e("Single AVD", "Enter");
            cursor=db.rawQuery("SELECT * FROM " + TableName,null);
            return cursor;
        }
        //For @
        else if(selection.equals("@") )
        {
            Log.e("@", "Enter");
            cursor=db.rawQuery("SELECT * FROM " + TableName,null);
            return cursor;
        }
        //For * Initial
        else if(selection.equals("*") )
        {
            STARFLAG=0;
            Log.e("*", "Enter");
            cursor=db.rawQuery("SELECT * FROM " + TableName,null);

            //Returning DB of all Databases
            message curmess=new message();
            curmess.port=myPort;
            curmess.type="3";
            curmess.mess="A";
            curmess.key="";
            String send="";
            try {
                send=curmess.toJString(cursor, null);
                Log.v("START","*");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            //Wait for Object
            TTT=new message();
            synchronized(TTT) {
                try {
                    Log.v("NEXT", "PORT");
                    new ClientTaskSucc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send, myPort);
                    while(STARFLAG!=1) {
                        TTT.wait();
                    }
                    //Matrix Cursor
                    String[] tmp={"key","value"};
                    mat=new MatrixCursor(tmp);

                    Iterator it = mesg.hm.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        //System.out.println(pair.getKey() + " = " + pair.getValue());
                        String[] str={pair.getKey().toString(),pair.getValue().toString()};
                        mat.addRow(str);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            Cursor[] out={cursor,mat};
            mgr=new MergeCursor(out);

            //Return MatrixCursor as Cursor
            return mgr;
        }
        //For * Forward
        else if(selection.equals("#") )
        {
            Log.e("*", "Enter");
            cursor=db.rawQuery("SELECT * FROM " + TableName,null);

            //Returning DB of all Databases
            message curmess=new message();
            curmess.port=mesg.port;
            curmess.type="3";
            curmess.mess="A";
            curmess.key="";
            String send="";
            try {
                send=curmess.toJString(cursor, mesg.hm);
                Log.v("START","*");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Send Object
                    Log.v("NEXT","PORT");
                    new ClientTaskSucc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send, myPort);
            return null;

        }

        else if(selection.equals("$") ){
            queryBuilder.appendWhere(KEY_FIELD + " = " + "'" + mesg.key + "'");

            Log.e("$", "ENTER");

            Log.e(" Appendwhere", queryBuilder.buildQuery(null, null, null, null, null, null));

            cursor = queryBuilder.query(db, projection, null, null, null, null, null);


                message curmess=new message();
                curmess.port=mesg.port;
                curmess.key=mesg.key;
                curmess.mess="A";
                curmess.type="4";
                String send="";
                try {
                    send=curmess.toJString(cursor, mesg.hm);
                    Log.v("COUNT", String.valueOf(cursor.getCount()));
                    Log.v("NEXT", send);
                    new ClientTaskSucc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send, myPort);
                    Log.v("START","*");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
           return null;
        }

        else {

            STARFLAG=0;
            queryBuilder.appendWhere(KEY_FIELD + " = " + "'" + selection + "'");

            Log.e("SINGLE", "ENTER");
            Log.e(" Appendwhere", queryBuilder.buildQuery(null, null, null, null, null, null));

            cursor = queryBuilder.query(db, projection, null, null, null, null, null);

            if(cursor.getCount() != 0)
            {
                return cursor;
            }

            else{

                message curmess=new message();
                curmess.port=myPort;
                curmess.key=selection;
                curmess.type="4";
                curmess.mess="A";
                String send="";

                try {
                    send=curmess.toJString(cursor, null);

                    Log.v("START","*");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Wait for Object
                TTT=new message();
                synchronized(TTT) {
                    try {
                        Log.v("NEXT", "BEFORE");
                        Log.v("COUNT", String.valueOf(cursor.getCount()));
                        Log.v("NEXT", send);
                        new ClientTaskSucc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send, myPort);
                        while(STARFLAG!=1) {
                            TTT.wait();
                        }
                        //Matrix Cursor
                        String[] tmp={"key","value"};
                        mat=new MatrixCursor(tmp);
                        Log.v("NEXT","AFTER");

                        Iterator it = mesg.hm.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            //System.out.println(pair.getKey() + " = " + pair.getValue());
                            String[] str={pair.getKey().toString(),pair.getValue().toString()};
                            Log.v("NEXT K",str[0]);
                            Log.v("NEXT V",str[1]);
                            mat.addRow(str);
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Cursor[] out={cursor,mat};
                mgr=new MergeCursor(out);

                //Return MatrixCursor as Cursor
                //cursor= mgr;
            }
            return mgr;

        }

        //return mgr;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    //Server Task
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {


            while(true) {
                ServerSocket serverSocket = sockets[0];
                String str;
                Socket soc;


                try {

                    //Receive message
                    soc = serverSocket.accept();
                    InputStreamReader read = new InputStreamReader(soc.getInputStream());
                    BufferedReader bread = new BufferedReader(read);
                    str = bread.readLine();

                    //Convert to message format
                    message mess=new message();
                    mess.toJMsg(str);


                    //Type 0 means new Port
                    if(mess.type.equals("0"))
                    {

                        for(int k=0;k<5;k++)
                        {
                            if(mess.key.equals(Port[k]))
                            {
                                Stat[k]=true;

                                if(!Port[k].equals(myPort))
                                {
                                    INIT=1;
                                }
                                break;
                            }
                            Log.v("STAT",Stat.toString());
                        }
                        publishProgress("SEND", String.valueOf(1));



                    }

                    //Type 1 Set Pred and Succ
                    else if(mess.type.equals("1"))
                    {
                        INIT=1;
                        pred=mess.key;
                        Log.v("PORT MESS PRE",pred);
                        succ=mess.mess;
                        Log.v("PORT MESS SUCC",succ);
                        int k=Integer.valueOf(mess.port);
                        if(Port[k].equals(myPort))
                        {
                            POS=0;
                        }
                        else{
                            POS=1;
                        }

                        Log.v("POS",String.valueOf(POS));
                            publishProgress("UPDATE", String.valueOf(1));

                    }

                    // Message Sent to Insert if type = 2
                    //  Insert handles GenHash condition
                    else if(mess.type.equals("2")) {

                        Log.v("TYPE RECEIVED","2");
                        ContentValues cv = new ContentValues();
                        cv.put("key", mess.key);
                        cv.put("value", mess.mess);
                        ContentResolver cr = getContext().getContentResolver();
                        cr.insert(uri, cv);
                        publishProgress(str, "SentIns".valueOf(1));
                    }
                    // For *
                    else if(mess.type.equals("3")) {

                        Log.v("TYPE RECEIVED","3");
                        mesg=new message();
                        mesg.port=mess.port;
                        mesg.hm=mess.hm;
                        mesg.key=mess.key;

                        //If Start Port Reached
                        if(mess.port.equals(myPort))
                        {
                            //Notify
                            Log.v("NOTIFY", myPort);
                            //TTT.notify();// Notifies Object
                            publishProgress("NOTIFY", "NOTIFY".valueOf(1));
                        }
                        else{

                            Log.v("NOTIFY ELSE",mess.port);
                            query(uri, null,"#", null, null); // Send Message to next with updated DB
                            publishProgress(str, "SentNew".valueOf(1));
                        }

                        publishProgress(str, "SentNew".valueOf(1));
                    }

                    // For *
                    else if(mess.type.equals("4")) {

                        Log.v("TYPE RECEIVED","4");
                        mesg=new message();
                        mesg.port=mess.port;
                        mesg.hm=mess.hm;
                        mesg.key=mess.key;

                        //If Start Port Reached
                        if(mess.port.equals(myPort))
                        {
                            //Notify
                            Log.v("NOTIFY", myPort);
                            Log.v("NOTIFY", str);
                            //TTT.notify();// Notifies Object
                            publishProgress("NOTIFY", "NOTIFY".valueOf(1));
                        }
                        else{

                            Log.v("NOTIFY ELSE",mess.port);
                            query(uri, null,"$", null, null); // Send Message to next with updated DB
                            publishProgress(str, "SentNew".valueOf(1));
                        }


                    }

                    else
                    {
                        Log.v("SOME MESSAGE","RECEIVED");

                    }


                    Log.e("SER", "Server Message Received");
                    //soc.close();

                } catch (IOException e) {
                    Log.e("SER", "ServerSocket IOException");
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();


            if(strReceived.equals("SEND"))
            {
                Log.v("ACTIVE PROCESSES ", ":");
                for(int c=0;c<5;c++) {
                    if(Stat[c]) {
                        char[] tmp=Status.toCharArray();
                        tmp[c]='1';
                        Status=String.valueOf(tmp);

                    }
                }
                Log.v("PROCESS ", Status);

                // Send Pred and Succ to other processes
                new ClientTaskStat().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "STATCALC", myPort);
            }

            else if(strReceived.equals("UPDATE"))
            {
                Log.v("PRED SUCC", ":");
                Log.v("PRED ", pred);
                Log.v("SUCC ", succ);
            }
            else if(strReceived.equals("SentIns"))
            {
                Log.v("SENT for Insert", "1");

            }
            else if(strReceived.equals("NOTIFY"))
            {
                synchronized(TTT) {
                    STARFLAG=1;
                    TTT.notify();
                }
                Log.v("NOTIFIED", "1");

            }



            Log.v("String Received : ",strReceived);

            String filename = "GroupMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e("FILE", "File write failed");
            }

            return;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////////////////

    //Client Task To send message to Successor for Insertion
    private class ClientTaskSucc extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            //Log.e("Message Count : ", Integer.toString(KEY));


                try {
                    String remotePort = succ;

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    String msgToSend = msgs[0];
                    OutputStream out = socket.getOutputStream();
                    OutputStreamWriter owrite = new OutputStreamWriter(out);
                    BufferedWriter bwrite = new BufferedWriter(owrite);
                    bwrite.write(msgToSend);
                    bwrite.flush();
                    Log.e(TAG, "Server Message Sent");


                    socket.close();
                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }



            return null;
        }
    }


    //Client Task To send Activation to 5554
    private class ClientTaskActive extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.e("Message Count : ", Integer.toString(KEY));


            try {
                String remotePort = "11108";

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                message tmp=new message();
                tmp.type="0";
                tmp.key=myPort;
                tmp.port=myPort;
                tmp.mess="test";
                HashMap<String,String> ab= new HashMap<String, String>();
                tmp.hm=ab;
                String msgToSend = tmp.toJString();

                OutputStream out = socket.getOutputStream();
                OutputStreamWriter owrite = new OutputStreamWriter(out);
                BufferedWriter bwrite = new BufferedWriter(owrite);
                bwrite.write(msgToSend);
                bwrite.flush();
                Log.e(TAG, "Server Message Sent");


                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }


    //Client Task To send Pred and Succ to all active processes
    private class ClientTaskStat extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.e("Message Count : ", Integer.toString(KEY));


            try {
                for(int i=0;i<5;i++) {
                 if(Stat[i]) {
                     String remotePort = Port[i];

                     Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                             Integer.parseInt(remotePort));

                     message tmp = new message();

                     int a=-1;
                     int b=-1;
                     int l=i;
                     while(a==-1)
                     {
                         l--;
                         if(l<0)
                             l=4;
                         if(Stat[l])
                         {
                             a=l;
                             break;
                         }

                     }
                     l=i;
                     while(b==-1)
                     {
                         l++;
                         if(l>=5)
                             l=0;
                         if(Stat[l])
                         {
                             b=l;
                             break;
                         }

                     }

                     int q;
                     for(q=0;q<5;q++)
                     {
                         if(Stat[q])
                         {
                             break;
                         }
                     }

                     tmp.type = "1";
                     tmp.key = Port[a];
                     Log.v("PORT SENT A :",Port[a]);
                     tmp.mess = Port[b];
                     tmp.port=String.valueOf(q);// First position
                     HashMap<String,String> ab= new HashMap<String, String>();
                     tmp.hm=ab;
                     Log.v("PORT SENT B :",Port[b]);
                     String msgToSend = tmp.toJString();

                     OutputStream out = socket.getOutputStream();
                     OutputStreamWriter owrite = new OutputStreamWriter(out);
                     BufferedWriter bwrite = new BufferedWriter(owrite);
                     bwrite.write(msgToSend);
                     bwrite.flush();
                     Log.e(TAG, "PORT Message Sent");


                     socket.close();
                 }
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
}
