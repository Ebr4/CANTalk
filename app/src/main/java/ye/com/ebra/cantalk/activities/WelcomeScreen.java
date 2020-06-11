package ye.com.ebra.cantalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import ye.com.ebra.cantalk.R;


public class WelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // when start the app start the splash screen activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        // run the splash screen in a thread for 3 seconds ..
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    // waiting for 3 seconds then if there's no errors occur open home activity
                    sleep(3000);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    goToHome();
                }
            }
        };
        thread.start();
    }
    // when the activity finishes its job finish it (because if you clicked back button you should not see the splash screen again)
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void goToHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

}
