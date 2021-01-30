package space.kalk.getraenkekarte;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!preferences.contains("uuid")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uuid", UUID.randomUUID().toString()); // value to store
            editor.commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String uuid = preferences.getString("uuid", "");
        TextView uuidView = (TextView) findViewById(R.id.uuid);
        uuidView.setText(uuid);
    }
}