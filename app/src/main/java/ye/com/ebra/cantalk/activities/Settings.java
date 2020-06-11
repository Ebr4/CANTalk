package ye.com.ebra.cantalk.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.database.SharedPref;

public class Settings extends AppCompatActivity {
    private Switch myswitch;
    SharedPref sharedpref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // check theme day or night
        sharedpref = new SharedPref(this);
        if(sharedpref.loadNightModeState()){
            setTheme(R.style.darktheme); }
        else {setTheme(R.style.AppTheme); }
        // start settings activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //create back button on the action bar
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException nullPointer){
            Log.d("back ActionBar"," Unsupported phone API BackActionBar");
        }

        // switch between day and night themes ..
        myswitch=findViewById(R.id.myswitch);
        if(sharedpref.loadNightModeState()){
            myswitch.setChecked(true);
        }

        myswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            // save the theme stats to shared preferences storage then restart the activity
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    sharedpref.setNightModeState(true);
                    restartApp();
                }
                else{
                    sharedpref.setNightModeState(false);
                    restartApp();   }
            }
        });
    }
    // restart the activity to enable the new theme
    public void restartApp(){
        Intent i=new Intent(getApplicationContext(), getClass());
        startActivity(i);
        // after start the new activity finish it to ensure the theme only in one activity (remove duplicated activities)
        finish();
    }

    //make the back button on the action bar perform the same as onBackPressed()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
    // control back button and set animation ..
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }


}
