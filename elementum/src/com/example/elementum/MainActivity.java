package com.example.elementum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elementum.MainApplication.ConnectionOptions;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	static ListView searchResults;
	ListAdapter searchResultsAdapter;
	static Context mContext;
	static MainActivity mActivity;
	static TextView sectionHeaderTextView;
	static IncomingHandler mHandler;
	static String PREFS_FILE_NAME = "myprefs";
	static SharedPreferences.Editor prefEditor;
	static boolean connected = true;

	@Override
	protected void onPause() {
		super.onPause();
		mHandler = null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.isConnected()) {
			connected = true;
		} else {
			connected = false;
		}

		SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE_NAME,
				MODE_PRIVATE);
		prefEditor = sharedPref.edit();
		prefEditor.putBoolean("connectionStatus", connected);

		// check if there was a term that needed to be searched and wasn't
		// searched
		// due to being offline
		if (connected && sharedPref.getString("savedTerm", null) != null) {
			new SearchTask().execute(sharedPref.getString("savedTerm", null));
			prefEditor.remove("savedTerm");
		}

		prefEditor.commit();

		MainApplication.service = new ServiceBuilder().provider(YelpApi2.class)
				.apiKey(MainApplication.consumerKey)
				.apiSecret(MainApplication.consumerSecret).build();
		MainApplication.accessToken = new Token(MainApplication.token,
				MainApplication.tokenSecret);
	}

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		mContext = this;
		mActivity = this;
		mHandler = new IncomingHandler();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.e("edmund", "onNewIntent()");
		super.onNewIntent(intent);

		// Get the intent, verify the action and get the query
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}
	}

	private void doMySearch(String query) {
		new SearchTask().execute(query);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.e("edmund", "onCreateOptionsMenu()");

		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();

			MenuItem searchItem = menu.findItem(R.id.action_search);
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView = (SearchView) MenuItemCompat
					.getActionView(searchItem);
			// Configure the search info and add any event listeners
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));

			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onSearchRequested() {
		Log.e("edmund", "onSearchRequested()");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			sectionHeaderTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			sectionHeaderTextView.setText(Integer.toString(getArguments()
					.getInt(ARG_SECTION_NUMBER)));
			searchResults = (ListView) rootView.findViewById(R.id.listView1);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

	private class SearchTask extends AsyncTask<String, Integer, Boolean> {
		Response response;
		String responseBody;
		String queryTerm;

		@Override
		protected Boolean doInBackground(String... params) {
			OAuthRequest request = new OAuthRequest(Verb.GET,
					"http://api.yelp.com/v2/search");
			queryTerm = params[0];
			request.addQuerystringParameter("term", queryTerm);

			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String bestProvider = lm.getBestProvider(criteria, false);
			Location location = lm.getLastKnownLocation(bestProvider);

			if (location != null) {
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				request.addQuerystringParameter("ll", latitude + ","
						+ longitude);
			} else {
				request.addQuerystringParameter("ll", 30.361471 + ","
						+ -87.164326);
			}

			MainApplication.service.signRequest(MainApplication.accessToken,
					request);
			response = request.send();
			Log.e("edmund", "response code: " + response.getCode());
			responseBody = response.getBody();
			if (response.getCode() == 200) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				try {
					JSONObject responseObject = new JSONObject(responseBody);
					JSONArray businessesList = responseObject
							.getJSONArray("businesses");

					int totalBusinesses = responseObject.getInt("total");
					sectionHeaderTextView.setText("" + totalBusinesses);

					String[] listItems = new String[totalBusinesses];

					for (int i = 0; i < businessesList.length(); i++) {
						listItems[i] = businessesList.getJSONObject(i)
								.getString("name");
					}

					searchResultsAdapter = new ArrayAdapter(mContext,
							android.R.layout.simple_list_item_1, listItems);
					searchResults.setAdapter(searchResultsAdapter);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				// save search term into savedprefs to resume when connection is
				// good again
				SharedPreferences sharedPref = getSharedPreferences(
						PREFS_FILE_NAME, MODE_PRIVATE);
				prefEditor = sharedPref.edit();
				prefEditor.putString("savedTerm", queryTerm);
				prefEditor.commit();
			}
		}
	}

	static class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == ConnectionOptions.CONNECTED.getValue()) {
				// connected
				connected = true;
				// check saved prefs to see if there is a pending term to be
				// searched... if so, search for it
				SharedPreferences sharedPref = mContext.getSharedPreferences(
						PREFS_FILE_NAME, MODE_PRIVATE);
				if (sharedPref.getString("savedTerm", null) != null) {
					mActivity.new SearchTask().execute(sharedPref.getString(
							"savedTerm", null));
					prefEditor.remove("savedTerm");
					prefEditor.commit();
				}
			} else {
				// disconnected
				connected = false;
			}
		}
	}
}
