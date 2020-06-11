package ye.com.ebra.cantalk.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import com.mancj.materialsearchbar.MaterialSearchBar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.adapter.ContentAdapter;
import ye.com.ebra.cantalk.adapter.Item;
import ye.com.ebra.cantalk.database.DBConnection;
import ye.com.ebra.cantalk.database.SharedPref;
import ye.com.ebra.cantalk.fragments.SentenceBarFragment;
import ye.com.ebra.cantalk.interfaces.sendItemToSentenceBar;

public class Search extends Activity {
    // view results in gridView ..
    private GridView gridView;
    // to fill grid view by resulted items ..
    private ContentAdapter searchAdapter;
    // to search in database
    private DBConnection dbConnection;
    // to hold suggested search keywords from previous search
    private List<String> suggest;
    // to hold resulted items from dbconnection
    private ArrayList<Item> items;
    private Item item;

    // send data to sentence bar ... not working yet :(
    sendItemToSentenceBar connector;
    Context context;

    // text to speech to speak the result label vocally
    TextToSpeech toSpeech;
    int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // check theme day or night
        SharedPref sharedpref = new SharedPref(this);
        if (sharedpref.loadNightModeState())
        { setTheme(R.style.darktheme); }
            else { setTheme(R.style.AppTheme); }

        // start search activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //alternative to action bar back button
        ImageButton back1 = findViewById(R.id.backSearch);

        back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // get the grid view from search layout
        gridView = findViewById(R.id.items_id_gridView);
        // initialize dbconnection object
        dbConnection = new DBConnection(this);
        // material search bar
        MaterialSearchBar materialSearchBar = findViewById(R.id.search_bar);
        materialSearchBar.setHint("Search");
        // maximum suggestions
        materialSearchBar.setMaxSuggestionCount(5);
        // to hold suggestions
        suggest = new ArrayList<>();

        // initialize connector to communicate with sentence bar fragment ...
        connector=(sendItemToSentenceBar) context;

        // initialize to speech object with US language and check if the device supports TextToSpeech or not
        toSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS)
                { result=toSpeech.setLanguage(Locale.US); }
                else
                { Toast.makeText(getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show(); }}});

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            // when the search state changed by the user
            @Override
            public void onSearchStateChanged(boolean enabled) {
                gridView.setAdapter(searchAdapter);
            }
            // when the user clicked on search
            @Override
            public void onSearchConfirmed(CharSequence text) {
                // set suggestions
                suggest.add(text.toString());
                // fill images in arraylist from the database a
                items = new ArrayList<>(dbConnection.SearchImages(text.toString()));
                // fill the adapter by the items arraylist
                searchAdapter = new ContentAdapter(Search.this, items);
                // show not found message if there is no result
                if(searchAdapter.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Item not found!",Toast.LENGTH_LONG).show();
                }else{
                // show results in gridview
                gridView.setAdapter(searchAdapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    // control clicked images
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        item = items.get(position);
                        speak(item.getName());

                        // send item to sentence bar :(

                        //onBackPressed();
                    }});
                }
            }
            //
            @Override
            public void onButtonClicked(int buttonCode) {
                }
        });
    }

    // read the image label vocally
    public void speak(String label){
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
        { Toast.makeText(getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show(); }
        else
            { toSpeech.speak(label,TextToSpeech.QUEUE_FLUSH,null); }
    }

    // control back button and set animation ..
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
}