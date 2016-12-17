package com.example.vipulsublaniya.searchgithub;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class MainSearchActivity extends AppCompatActivity {
    Button search,filters,clearFilters;
    EditText searchText,sizeOfRepo,forksSize, languageName;
    Boolean showFilters = false;
    Spinner limitSizeSpinner,sizeDimenSpinner,forkLimitSpinner,languageIncludeSpinner,sortFilter, orderBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search);

        //search option elements
        search = (Button) findViewById(R.id.search);
        filters = (Button) findViewById(R.id.showFilters);
        searchText = (EditText) findViewById(R.id.searchText);
        clearFilters = (Button) findViewById(R.id.clearFilters);
        //size filter elements
        limitSizeSpinner = (Spinner) findViewById(R.id.limitSizeSpinner);
        sizeOfRepo = (EditText) findViewById(R.id.size);
        sizeDimenSpinner = (Spinner) findViewById(R.id.sizeDimenSpinner);

        //forks filter elements
        forkLimitSpinner = (Spinner) findViewById(R.id.forksLimitSpinner);
        forksSize =(EditText) findViewById(R.id.forksSize);

        //language filter elements
        languageIncludeSpinner = (Spinner) findViewById(R.id.LanguageIncludeSpinner);
        languageName = (EditText) findViewById(R.id.languageName);

        //sortBy filter element
        sortFilter = (Spinner) findViewById(R.id.sortFilter);

        //orderBy filter element
        orderBy = (Spinner) findViewById(R.id.orderBy);


        //Add value to all the spinners=========================================================================
        String[] a = new String[]{"Less than", "Greater than"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, a);
        limitSizeSpinner.setAdapter(adapter1);

        String[] b = new String[]{"KB", "MB", "GB"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, b);
        sizeDimenSpinner.setAdapter(adapter2);

        String[] c = new String[]{"More than", "Less than"};
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, c);
        forkLimitSpinner.setAdapter(adapter3);

        String[] d = new String[]{"Include Only", "Exclude"};
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, d);
        languageIncludeSpinner.setAdapter(adapter4);

        String[] e = new String[]{"Best Match", "stars", "forks", "updated"};
        ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, e);
        sortFilter.setAdapter(adapter5);

        String[] f = new String[]{"Desc", "Asc"};
        ArrayAdapter<String> adapter6 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, f);
        orderBy.setAdapter(adapter6);
        //======================================================================================================

        //initially hide filters by default
        final RelativeLayout filters_layout = (RelativeLayout) findViewById(R.id.filters_layout);
        filters_layout.setVisibility(View.INVISIBLE);

        //show and hide filters
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide keyboard
                View view = MainSearchActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (showFilters == false) {
                    clearAllFilter();
                    filters_layout.setVisibility(View.VISIBLE);
                    showFilters = true;
                } else {
                    clearAllFilter();
                    filters_layout.setVisibility(View.INVISIBLE);
                    showFilters = false;
                }
            }
        });

        clearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide keyboard
                View view = MainSearchActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                clearAllFilter();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //hide keyboard
                View view = MainSearchActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                String txt = searchText.getText().toString();
                if(txt.isEmpty()){
                    Toast.makeText(MainSearchActivity.this,"Search Box can't be Empty!!", Toast.LENGTH_LONG ).show();
                    return;
                }

                if(!isInternetConnected(getBaseContext())){
                    Toast.makeText(MainSearchActivity.this, "No Internet Connection!",Toast.LENGTH_SHORT).show();
                    return;
                }

                //manipulation for size filter
                String x = sizeOfRepo.getText().toString();
                String x3 = "";
                String x4 = "";
                if(!x.isEmpty()){
                    String x1 = limitSizeSpinner.getSelectedItem().toString();
                    if(x1=="Less than"){
                        x4 += "<";
                    }else{
                        x4 += ">";
                    }

                    int x2 = sizeDimenSpinner.getSelectedItemPosition();
                    if(x2==1){
                        x3 += String.valueOf(Integer.parseInt(x)*1024);
                    }else if(x2==2){
                        x3 += String.valueOf(Integer.parseInt(x)*1024*1024);
                    }else{
                        x3 += x;
                    }
                    txt+=("+size:"+x4+x3);


                }

                //manipulation for forks filter
                String y = forksSize.getText().toString();
                if(!y.isEmpty()){
                    String y1 = forkLimitSpinner.getSelectedItem().toString();
                    if(y1.equals("More than")){
                        txt+="+forks:>";
                    }else{
                        txt+="+forks:<";
                    }

                    txt+=y;
                }

                //manipulation for language filter
                String z = languageName.getText().toString();
                if(!z.isEmpty()){
                    String z1 = languageIncludeSpinner.getSelectedItem().toString();
                    if(z1.equals("Exclude")){
                        txt+=("+-language:"+z);

                    }else{
                        txt+=("+language:"+z);
                    }
                }

                //manipulation for SortBy filter
                String w = sortFilter.getSelectedItem().toString();
                if(!w.equals("Best Match")){
                    txt+=("&sort="+w);
                }

                //manipulation for Order filter
                String w1 = orderBy.getSelectedItem().toString();
                if(w1.equals("Asc")){
                    txt+="&order=asc";
                }else{
                    txt+="&order=desc";
                }

                Log.e("final search text string:", txt);

                //limit per page data to 5 entries
                txt+="&per_page=5";

                Intent i = new Intent(MainSearchActivity.this, Search_Result.class);
                i.putExtra("searchText",txt);
                startActivity(i);

            }
        });
    }

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void clearAllFilter() {
        sizeOfRepo.setText("");
        forksSize.setText("");
        languageName.setText("");
        sortFilter.setSelection(0);
        orderBy.setSelection(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
