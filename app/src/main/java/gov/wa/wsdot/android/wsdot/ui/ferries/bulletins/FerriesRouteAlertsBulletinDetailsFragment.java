/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.ferries.bulletins;

import android.annotation.SuppressLint;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;

public class FerriesRouteAlertsBulletinDetailsFragment extends BaseFragment implements Injectable {

    private static final String TAG = FerriesRouteAlertsBulletinDetailsFragment.class.getSimpleName();
    private WebView webview;
	private DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	private String mAlertPublishDate;
	private String mAlertFullTitle;
	private String mAlertDescription;
	private String mAlertFullText;

	private Integer mRouteId;
	private Integer mAlertId;

	private String mContent;
	private View mLoadingSpinner;

    private static FerriesBulletinsViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getActivity() != null) {
			Bundle args = getActivity().getIntent().getExtras();
			if (args != null) {
				mRouteId = args.getInt("routeId", 0);
				mAlertId = args.getInt("alertId", 0);
			}
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_webview_with_spinner, null);
		mLoadingSpinner = root.findViewById(R.id.progress_bar);
		mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = root.findViewById(R.id.webview);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);	
		
		disableAds(root);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FerriesBulletinsViewModel.class);
		viewModel.init(mRouteId, mAlertId);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        break;
					case SUCCESS:
					    if (mAlertFullText == null){
                            mContent = "<p>Sorry, this bulletin has expired.</p>";
                            webview.loadDataWithBaseURL(null, mContent, "text/html", "utf-8", null);
                        }
                        break;
                    case ERROR:
                        if (mAlertFullText == null) {
                            mLoadingSpinner.setVisibility(View.GONE);
                            mContent = "<p>Connection error, failed to load bulletin.</p>";
                            webview.loadDataWithBaseURL(null, mContent, "text/html", "utf-8", null);
                        } else {
                            Toast.makeText(getContext(), "Connection error", Toast.LENGTH_LONG).show();
                        }
                }
            }
        });

        viewModel.getAlert().observe(this, alert -> {
            if (alert != null) {

                mAlertPublishDate = displayDateFormat.format( alert.getPublishDate());
                mAlertDescription = alert.getAlertDescription();
                mAlertFullText = alert.getAlertFullText();
                mAlertFullTitle = alert.getAlertFullTitle();
                mContent = formatText(mAlertPublishDate, mAlertDescription, mAlertFullText);

                webview.loadDataWithBaseURL(null, mContent, "text/html", "utf-8", null);
            }
        });

		return root;
	}

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem menuItem_Share = menu.findItem(R.id.action_share);
        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem_Share);
        shareAction.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        shareAction.setShareIntent(createShareIntent());
	}
    
	private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mAlertFullTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mAlertDescription + "\n\n" + mAlertFullText);
        
        return shareIntent;
	}
	
	private String formatText(String date, String description, String text)	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<p>" + date + "</p>");
		sb.append("<p><b>" + description + "</b></p>");
		sb.append("<p>" + text + "</p>");
			
		return sb.toString();
	}
	
	public class myWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("http:") || url.startsWith("https:")) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(browserIntent);
			} else {
				view.loadUrl(url);
			}
			
			return true;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			mLoadingSpinner.setVisibility(View.GONE);
		}
	}
	
}
