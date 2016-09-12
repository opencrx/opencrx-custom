package org.opencrx.sample.android;

import java.util.List;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import org.opencrx.sample.android.AccountLoader.AccountItem;

public class MainActivity
    extends ListActivity
    implements LoaderManager.LoaderCallbacks<List<AccountLoader.AccountItem>> {

  public static class AccountListAdapter
      extends ArrayAdapter<AccountLoader.AccountItem> {

    public AccountListAdapter(
        Context context) {
      super(context,
          android.R.layout.simple_list_item_1);
    }

    public void setData(
        List<AccountLoader.AccountItem> data) {
      clear();
      if (data != null) {
        addAll(data);
      }
    }
  }

  private AccountListAdapter adapter;

  @Override
  protected void onCreate(
      Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final View view = this.getLayoutInflater().inflate(R.layout.activity_main,
        null);
    this.setContentView(view);

    final SharedPreferences prefs = this.getSharedPreferences(SettingsActivity.PREFERENCES,
        Context.MODE_PRIVATE);
    if (!prefs.contains(SettingsActivity.SERVERNAME) || !prefs.contains(SettingsActivity.LOGIN)
        || !prefs.contains(SettingsActivity.PASSWORD)) {
      Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
      this.startActivity(intent);
    }

    this.adapter = new AccountListAdapter(this);
    this.setListAdapter(this.adapter);

    final Button search = (Button) view.findViewById(R.id.btn_search);
    search.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(
          View v) {
        MainActivity.this.getLoaderManager().destroyLoader(0);
        MainActivity.this.getLoaderManager().initLoader(0,
            null,
            MainActivity.this).forceLoad();
      }
    });

  }

  @Override
  public boolean onCreateOptionsMenu(
      Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main,
        menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(
      Menu menu) {
    super.onPrepareOptionsMenu(menu);

    final MenuItem settings = menu.findItem(R.id.action_settings);
    settings.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(
          MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(intent);
        return false;
      }
    });

    return true;
  }

  @Override
  public Loader<List<AccountItem>> onCreateLoader(
      int id,
      Bundle args) {
    final EditText name = (EditText) this.findViewById(R.id.searchName);
    final EditText city = (EditText) this.findViewById(R.id.searchCity);
    return new AccountLoader(this, name.getText() != null && name.getText().length() > 0 ? name
        .getText().toString() : null, city.getText() != null && city.getText().length() > 0 ? city
        .getText().toString() : null);
  }

  @Override
  public void onLoadFinished(
      Loader<List<AccountItem>> loader,
      List<AccountItem> accounts) {
    this.adapter.setData(accounts);
  }

  @Override
  public void onLoaderReset(
      Loader<List<AccountItem>> arg0) {
    this.adapter.setData(null);
  }

}
