package com.avantari.dictionary;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class MainActivity extends AppCompatActivity {
    Button btAddToDb;
    String execTime;
    private DictionaryDatabase database;
    private List<String> wordList = new ArrayList<>();
    private List<String> descList = new ArrayList<>();
    private TextView tvTotalWords;
    private TextView tvTotalTime;
    private TextView tv_progress;
    private Document document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = DictionaryDatabase.getInstance(this);
        btAddToDb = (Button) findViewById(R.id.bt_add);
        tvTotalWords = (TextView) findViewById(R.id.tv_totalWords);
        tvTotalTime = (TextView) findViewById(R.id.tv_totalTime);
        tv_progress = (TextView) findViewById(R.id.tv_progress);

        RequestQueue queue = Volley.newRequestQueue(this);

        for (int i = 97; i <= 122; i++) {

            // convert ASCII value
            final char word = (char) i;

            // generate URL to make network call
            final String url = "http://www.unreal3112.16mb.com/wb1913_" + word + ".html";

            // make a GET request using URL
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {

                        // will get the String Response(HTML)
                        @Override
                        public void onResponse(final String response) {

                            // parse HTML data using Jsoup Library
                            document = Jsoup.parse(response);
                            parseData();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error != null) {
                            Log.e("DataError", error.getLocalizedMessage());
                        }
                    } catch (NullPointerException nullPointer) {
                        Log.e("Null Pointer: ", nullPointer.getLocalizedMessage());
                    }
                }
            });

            // all requests will be Queued.
            queue.add(stringRequest);
        }

        btAddToDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.i("Start DB: ", "inserting");
                // will start writing to Database and return the Total time in Second Unit
                if (wordList.size() == 176077) {
                    new ShowProgress().execute();

                } else {
                    Toast.makeText(MainActivity.this, "Wait Until Timer stops", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void parseData() {

        // we will get all the P tag here which contains words and description
        String[] desc = document.select("p").toString().split("<p>");

        // here we are going to parse data again and get the Words and Description.
        for (String aDesc : desc) {
            document = Jsoup.parse(aDesc);

            // get words from B tag
            String innerWord = document.select("b").text().trim();
            try {

                // get description from P tab by removing Word
                String innerDesc = document.text().replaceFirst(innerWord, "").trim();
                wordList.add(innerWord);
                descList.add(innerDesc);
            } catch (PatternSyntaxException pattern) {
                Log.e("PatternError", pattern.getLocalizedMessage());
            } catch (NullPointerException nullPointer) {
                Log.e("NullPointer", nullPointer.getLocalizedMessage());
            }
        }
        processDone();
    }

    private void processDone() {
        
        // we are going to get total 176077 so we can use this if statement otherwise we can't
        if (wordList.size() == 176077) {
            Toast.makeText(MainActivity.this, "Data Parsed Successfully",
                    Toast.LENGTH_SHORT).show();
            tv_progress.setText("Done");
            btAddToDb.setClickable(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btAddToDb.setTextColor(getResources().getColor(R.color.textGreen, null));
            } else {
                btAddToDb.setTextColor(getResources().getColor(R.color.textGreen));
            }
        }
    }

    // shows the wait dialog while inserting data to Database
    private class ShowProgress extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_progress.setText("Please Wait... \nInserting Data to Database");
            Log.i("Started", "Process Started");
        }

        @Override
        protected String doInBackground(Void... params) {
            return database.insertValue(wordList, descList);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            execTime = s;
            Log.i("Ended", "Process Ended");
            try {
                tvTotalTime.setText("Total Time to add data to Database: " + execTime + "sec");
                tvTotalWords.setText("Total words: " + wordList.size() + "Words");

            } catch (Resources.NotFoundException resources) {
                Log.e("ResourceNotFound ", resources.getLocalizedMessage());
            }
            tv_progress.setText("Done");
        }
    }
}


