package org.opencrx.sample.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity
    extends Activity {

  public static final String PREFERENCES = "serverSettings";
  public static final String SERVERNAME = "serverName";
  public static final String LOGIN = "login";
  public static final String PASSWORD = "password";

  @Override
  protected void onCreate(
      Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final View view = this.getLayoutInflater().inflate(R.layout.settings,
        null);
    this.setContentView(view);

    final EditText serverName = (EditText) view.findViewById(R.id.serverName);
    final EditText login = (EditText) view.findViewById(R.id.login);
    final EditText password = (EditText) view.findViewById(R.id.password);

    final SharedPreferences prefs = this.getSharedPreferences(PREFERENCES,
        Context.MODE_PRIVATE);
    serverName.setText(prefs.getString(SERVERNAME,
        null));
    login.setText(prefs.getString(LOGIN,
        null));
    password.setText(prefs.getString(PASSWORD,
        null));

    Button buttonOk = (Button) view.findViewById(R.id.btn_ok);
    buttonOk.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(
          View v) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SERVERNAME, serverName.getText().toString());
        editor.putString(LOGIN, login.getText().toString());
        editor.putString(PASSWORD, password.getText().toString());
        editor.commit();
        SettingsActivity.this.finish();
      }
    });

  }

}
