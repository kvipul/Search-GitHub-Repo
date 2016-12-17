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
    JSONArray items;
    int page = 0;
    ListView repoListView;
    String total_count,searchText,incomplete_results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search__result);

        //initialization
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
                page -= 1;
                new Atask().execute(searchText + "&page=" + String.valueOf(page + 1));
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                new Atask().execute(searchText + "&page=" + String.valueOf(page + 1));
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
            HttpURLConnection urlConnection;
            URL url;
            InputStream inputStream;
            String response="";

            //Method 1: To Authorize API access for all HTTP call
            //Uncomment this part of code and input your username and password
//            Authenticator.setDefault(new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication("username", "password".toCharArray());
//                }
//            });

            try{
                url = new URL("https://api.github.com/search/repositories?q="+params[0]);
                Log.e("url valeu", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                //Method 2: To Authorize API access while making HTTP request

                //Uncomment this part of code and input your username and password
//                String basicAuth = "Basic "+Base64.encodeToString("kvipul:password".getBytes(), Base64.DEFAULT).replace("\n", "");
//                urlConnection.setRequestProperty ("Authorization", basicAuth);

                //set request type
                urlConnection.setRequestMethod("GET");

                //if you uncomment the following line GitHub API will not respond
//                urlConnection.setDoOutput(true);

                urlConnection.setDoInput(true);
                urlConnection.connect();
                //check for HTTP response
                int httpStatus = urlConnection.getResponseCode();
                Log.e("httpstatus", "The response is: " + httpStatus);

                //if HTTP response is 200 i.e. HTTP_OK read inputstream else read errorstream
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
                String temp;
                while((temp = bufferedReader.readLine())!=null){
                    response+=temp;
                }
                Log.e("webapi json object",response);


                //convert data string into JSONObject
                JSONObject obj = (JSONObject) new JSONTokener(response).nextValue();
                items = obj.getJSONArray("items");

                total_count = obj.getString("total_count");
                incomplete_results = obj.getString("incomplete_results");

                urlConnection.disconnect();
            } catch (MalformedURLException | ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setResultListView();
            pDialog.dismiss();
        }
    }

    private void setResultListView(){

        //set page no. on the layout
        pageNo.setText("Page " + String.valueOf(page + 1));

        //set TotalCount and error if any
        if(total_count.equals("0")){
            count.setText("No Repository Found! Try Again!");
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

        //parse total page in search result
        int totalpage = Integer.parseInt(total_count)/5;
        Log.e("total page, page", String.valueOf(totalpage)+", "+String.valueOf(page));

        //condition to enbable and disable nextpage button and prev page button
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

        //finally set listview adaptor
        List<String> adapterList = new ArrayList<>();
        if(items.length()==0){
            return;
        }
        Log.e("some more data","item.length"+String.valueOf(items.length()));
        for (int i=0 ; i<items.length();i++){
            JSONObject jo;
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
                new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, adapterList);

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

}
