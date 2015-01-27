package de.visi0nary.motogmultitaskingfixer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;


public class MainActivity extends Activity {

    private boolean onStartup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // get settings storage reference
        final SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        // get current setting
        onStartup = settings.getBoolean(getResources().getString(R.string.text_apply_on_boot), false);
        // get switch reference
        Switch startup = (Switch) findViewById(R.id.switch_onStartup);
        startup.setChecked(onStartup);
        // register a new listener that writes a button click in the settings storage
        startup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    settings.edit().putBoolean(getResources().getString(R.string.text_apply_on_boot), true).commit();
                }
                else {
                    settings.edit().putBoolean(getResources().getString(R.string.text_apply_on_boot), false).commit();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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



    public void onButtonClick(View view) {
        startService(new Intent(this, ApplyPermissionsService.class));
    }

}
