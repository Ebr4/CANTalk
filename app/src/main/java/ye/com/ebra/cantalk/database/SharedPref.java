package ye.com.ebra.cantalk.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    /*
    * shared preferences storage to store the theme only .. day or night theme
    * */
    private SharedPreferences mySharedPref;

    public SharedPref(Context context){
        mySharedPref = context.getSharedPreferences("filename",Context.MODE_PRIVATE);
    }
    // This method will save nightMode state : True or False
    public void setNightModeState(Boolean state){
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("NightMode",state);
        editor.apply();
    }
    //this method will load the night mode state
    public Boolean loadNightModeState(){
        Boolean state=mySharedPref.getBoolean("NightMode", false);
        return state;
    }

}

