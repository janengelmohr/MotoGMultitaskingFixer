package de.visi0nary.motogmultitaskingfixer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;


public class MainActivity extends Activity {

    private boolean onStartup;
    private boolean applyReasonableMinfrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // get settings storage reference
        final SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        // get current setting for the "on start up" switch
        onStartup = settings.getBoolean(getResources().getString(R.string.text_apply_on_boot), false);
        // get switch reference
        Switch startup = (Switch) findViewById(R.id.switch_onStartup);
        startup.setChecked(onStartup);
        // register a new listener that writes a button click in the settings storage
        startup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    settings.edit().putBoolean(getResources().getString(R.string.text_apply_on_boot), true).apply();
                }
                else {
                    settings.edit().putBoolean(getResources().getString(R.string.text_apply_on_boot), false).apply();
                }
            }
        });

        // get settings for "reasonable minfree values" switch
        applyReasonableMinfrees = settings.getBoolean(getResources().getString(R.string.text_minfree_values), false);
        // get switch reference
        Switch reasonableValues = (Switch) findViewById(R.id.switch_minfree_values);
        reasonableValues.setChecked(applyReasonableMinfrees);
        // register a new listener that writes a button click in the settings storage
        reasonableValues.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    settings.edit().putBoolean(getResources().getString(R.string.text_minfree_values), true).apply();
                }
                else {
                    settings.edit().putBoolean(getResources().getString(R.string.text_minfree_values), false).apply();
                }
            }
        });

    }


    public void onButtonClick(View view) {
        startService(new Intent(this, ApplyPermissionsService.class));
    }

}
