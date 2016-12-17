package com.example.vipulsublaniya.searchgithub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Search_Result extends AppCompatActivity {
    TextView count,pageNo;
    Button prev,next;
//    JSONObject obj;
    JSONArray items;
    int page = 0;
    ListView repoListView;
    String total_count,searchText,incomplete_results;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search__result);

        count = (TextView) findViewById(R.id.count);
        pageNo = (TextView) findViewById(R.id.page);
        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        repoListView = (ListView) findViewById(R.id.listView);


        searchText = getIntent().getStringExtra("searchText");
        new Atask().execute(searchText+"&page="+String.valueOf(page + 1));

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page-=1;
                new Atask().execute(searchText+"&page="+String.valueOf(page + 1));
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                new Atask().execute(searchText+"&page="+String.valueOf(page + 1));
            }
        });




    }

    private void setResultListView(){
        pageNo.setText("Page " + String.valueOf(page + 1));

        if(total_count.equals("0")){
            count.setText("No Data Matched! Try Again!");
            count.setTextColor(Color.RED);
            prev.setEnabled(false);
            next.setEnabled(false);
            return;
        }
        if(incomplete_results.equals("true")){
            count.setText("Total Count:"+String.valueOf(total_count)+"(NetworkError:Incomplete Result!)");
            count.setTextColor(Color.RED);
        }else{
            count.setText("Total Count:"+String.valueOf(total_count));
            count.setTextColor(Color.BLUE);
        }

        int totalpage = Integer.parseInt(total_count)/5;
        Log.e("total page, page", String.valueOf(totalpage)+", "+String.valueOf(page));

        if(page==0){
            prev.setEnabled(false);
        }else{
            prev.setEnabled(true);
        }

        if(page==totalpage ){
            next.setEnabled(false);
        }else{
            next.setEnabled(true);
        }

        List<String> adapterList = new ArrayList<String>();
        if(items.length()==0){
            return;
        }
        Log.e("some more data","item.length"+String.valueOf(items.length()));
        for (int i=0 ; i<items.length();i++){
            JSONObject jo = null;
            try {
                jo = items.getJSONObject(i);
                adapterList.add(String.valueOf(page*5+i+1)+". Repo Name: "+jo.getString("full_name")+"\n"
                                +"   Size: "+jo.getString("size")+"KB"+"\n"
                                +"   Forks: "+jo.getString("forks")+"\n"
                                +"   Language: "+jo.getString("language")+"\n"
                                +"   Watch Count: "+jo.getString("watchers_count")+"\n"
                                +"   Updated At: "+jo.getString("updated_at")+"\n"
                );

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, adapterList);

        //set adapter to list view
        repoListView.setAdapter(adapter);
        //set onClickListener to each list item
        repoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String st = (String) parent.getItemAtPosition(position);
                Toast.makeText(Search_Result.this, "Repo No." +String.valueOf(page*5+1+position)+ " Selected", Toast.LENGTH_LONG).show();

                Intent i = new Intent(Search_Result.this, Repository_details.class);
                try {
                    i.putExtra("repo", items.getJSONObject(position).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });

    }

    class Atask extends AsyncTask<String,Void,Void> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(Search_Result.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url;
            JSONObject object = null;
            InputStream inputStream;
            String response="";

            //Method 2: To authorize api access for all http call
//            Authenticator.setDefault(new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication("kvipul", "password".toCharArray());
//                }
//            });

            try{

                url = new URL("https://api.github.com/search/repositories?q="+params[0]);
                Log.e("url valeu", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                //Method 2: To authorize api access while make http request
//                urlConnection.setRequestProperty("Authorization", "OAuth " + "4e67064ae94f685df6862120c625ab484e07b82b");
//                String basicAuth = "Basic "+Base64.encodeToString("kvipul:password".getBytes(), Base64.DEFAULT).replace("\n", "");
//                urlConnection.setRequestProperty ("Authorization", basicAuth);

                //set request type
                urlConnection.setRequestMethod("GET");

                //if you uncomment the following line GitHub api will not respond
//                urlConnection.setDoOutput(true);

                urlConnection.setDoInput(true);
                urlConnection.connect();
                //check for http response
                int httpStatus = urlConnection.getResponseCode();
                Log.e("httpstatus", "The response is: " + httpStatus);


                if (httpStatus != HttpURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getErrorStream();
                    Map<String, List<String>> map = urlConnection.getHeaderFields();
                    System.out.println("Printing Response Header...\n");
                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                        System.out.println(entry.getKey()
                                + " : " + entry.getValue());
                    }
                }
                else
                    inputStream = urlConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp="";
                while((temp = bufferedReader.readLine())!=null){
                    response+=temp;
                }
                Log.e("webapi json object",response);

                //convert data string into JSONObject
                JSONObject obj = (JSONObject) new JSONTokener(response).nextValue();
                items = obj.getJSONArray("items");

                total_count = obj.getString("total_count");
                incomplete_results = obj.getString("incomplete_results");

//                Log.d("webapi json object1",object.getClass().getName());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
//            return response;
            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.dismiss();
//            JSONObject obj = null;
//            try {
//                obj = (JSONObject) new JSONTokener(s).nextValue();
//                int it = obj.getJSONArray("items").length();
//                Log.e("data in mainSearchActivity", String.valueOf(it));
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

//            Log.e("Async task response", s);
            setResultListView();

        }
    }

}
