package ye.com.ebra.cantalk.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.adapter.Item;
import ye.com.ebra.cantalk.database.SharedPref;
import ye.com.ebra.cantalk.fragments.AddNewDialogFragment;
import ye.com.ebra.cantalk.fragments.ContentFragment;
import ye.com.ebra.cantalk.fragments.SentenceBarFragment;
import ye.com.ebra.cantalk.interfaces.sendItemToSentenceBar;

public class Home extends AppCompatActivity implements sendItemToSentenceBar {
    // Permissions that the program will need to achieve its job in correct way
    private final String[] permissions =
            {   Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA  };
    private static final int MULTIPLE_PERMISSIONS = 123;


    // Content Fragment
    static ContentFragment Contentfragment;
    static FragmentManager fragmentManager;
    static FragmentTransaction transaction;
    // for theme day or night
    SharedPref sharedpref;

    // take an image to send it to sentence bar.. Not complete yet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // check theme
        sharedpref = new SharedPref(this);
        if(sharedpref.loadNightModeState())
        { setTheme(R.style.darktheme); }
        else {setTheme(R.style.AppTheme); }

        // animation when activity starts ...
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );

        // start activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // check permissions ...
        if(!checkPermissions()){
            Toast.makeText(this, "yow will not be able to use some features if you deny any permission ",Toast.LENGTH_LONG).show();
        }

        //create back button on the action bar
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException nullPointer){
            Log.d("back ActionBar"," Unsupported phone API BackActionBar");
        }

        // get and fill the content fragment with items and categories....
        getContentFragment();
    }

    // implement content fragment in home activity
    private void getContentFragment(){
        Contentfragment = new ContentFragment();
        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content_Layout,Contentfragment,"ContentFragment");
        transaction.commit();
    }

    // set theme for action bar menu buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        sharedpref=new SharedPref(this);
        getMenuInflater().inflate(R.menu.home_menu,menu);
        Drawable menuItems[]={
                menu.findItem(R.id.backSpace).getIcon(),
                menu.findItem(R.id.settings).getIcon(),
                menu.findItem(R.id.search).getIcon(),
                menu.findItem(R.id.add).getIcon()
        };

        for(int i=0;i<menuItems.length;i++){
            if (menuItems[i]!= null) {
                menuItems[i].mutate();
                if (sharedpref.loadNightModeState()){
                    menuItems[i].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);}
                else{
                    menuItems[i].setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);}
            }
        }
        return true;
    }


    // controlling menu items if they're clicked :D
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            // for back button in action bar
            case android.R.id.home:
                onBackPressed();
                break;
            // start search activity
            case R.id.search:
                searchActivity();
                break;
            // start add new dialog Fragment
            case R.id.add:
                addNew();
                break;
            // start settings activity
            case R.id.settings:
                settings();
                break;
            // remove last image in sentence bar
            case R.id.backSpace:
                removeData();
                break;
        }
        return true;
    }

    public void searchActivity(){
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up );
    }
    // show up add new dialog fragment
    public void addNew(){
        FragmentManager manager=getFragmentManager();
        AddNewDialogFragment add_new=new AddNewDialogFragment();
        add_new.show(manager,null);
        onPause();
    }

    public void settings(){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }

    // tell user to click back buttons two times to exit the program
    boolean doubleBackToExitPressedOnce = false;
    // back button controller
    @Override
    public void onBackPressed() {
        // get the fragments that are stored in the backStack ..
        // works on categories ..
        // for fragments when user click back button show the previous category ... so on
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() { doubleBackToExitPressedOnce=false; }}, 2000);
            // if the backStack finished and back button clicked two times then close the app
            onResume();
            } else {
            // to keep tracking the category id ..
            AddNewDialogFragment.Cat_id=ContentFragment.getBackID();
            getFragmentManager().popBackStack();
        }
    }

    //Request permission from user :)
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getBaseContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    // override methods from sentence bar interface to send and remove items from content fragment to sentence bar and when click backspace button
    @Override
    public void sendData(Item item) {
        FragmentManager fragmentManager=getFragmentManager();
        // send item object
        SentenceBarFragment s= (SentenceBarFragment) fragmentManager.findFragmentById(R.id.sentenceBarFragment);
        s.AddItem(item);
    }

    @Override
    public void removeData() {
        SentenceBarFragment remove=(SentenceBarFragment) getFragmentManager().findFragmentById(R.id.sentenceBarFragment);
        remove.removeLastItem();
    }


}