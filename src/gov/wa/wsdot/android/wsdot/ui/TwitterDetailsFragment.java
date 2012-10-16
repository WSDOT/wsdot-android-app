/*
 * Copyright (c) 2012 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class TwitterDetailsFragment extends SherlockFragment {
	
	private String mUserName;
	private String mCreatedAt;
	private String mText;
	private String mHtmlText;
	private WebView webview;
	private View mLoadingSpinner;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Bundle args = activity.getIntent().getExtras();
		mUserName = args.getString("userName");
		mCreatedAt = args.getString("createdAt");
		mText = args.getString("text");	
		mHtmlText = args.getString("htmlText");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true); 		
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_webview_with_spinner, null);
		mLoadingSpinner = root.findViewById(R.id.loading_spinner);
		mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = (WebView)root.findViewById(R.id.webview);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);	

		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		webview.loadDataWithBaseURL(null, formatText(mUserName, mCreatedAt, mHtmlText), "text/html", "utf-8", null);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        //actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareHistoryFileName(null);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        actionProvider.setShareIntent(createShareIntent());
    }
	
	private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mText);
        
        return shareIntent;
	}
	
	private String formatText(String userName, String createdAt, String htmlText)	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<p><b>" + userName + "</b></p>");
		sb.append("<p style='color:#7d7d7d;'>" + createdAt + "</p>");
		sb.append("<p>"+ htmlText +"</p>");
			
		return sb.toString();
	}
	
	public class myWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			
			return true;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			
			mLoadingSpinner.setVisibility(View.GONE);
		}
	}

}
