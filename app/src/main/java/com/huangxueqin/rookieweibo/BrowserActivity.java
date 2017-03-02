package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/2.
 */

public class BrowserActivity extends BaseActivity {

    private static final String TAG = "BrowserActivity";

    @BindView(R.id.webview)
    WebView webView;
    @BindView(R.id.toolbar)
    LinearLayout toolbar;
    @BindView(R.id.back)
    View backButton;
    @BindView(R.id.close)
    View closeButton;
    @BindView(R.id.title)
    TextView toolbarTitle;

    private String mBaseURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ButterKnife.bind(this);
        toolbar.setBackgroundColor(getResources().getColor(R.color.white));
        backButton.setOnClickListener(mToolbarButtonClickListener);
        closeButton.setOnClickListener(mToolbarButtonClickListener);

        mBaseURL = getIntent().getStringExtra("content-url");

        webView.setWebViewClient(mWebViewClient);
        webView.setWebChromeClient(mWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(mBaseURL);
    }

    private View.OnClickListener mToolbarButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    if (webView.canGoBack()) {
                        closeButton.setVisibility(View.VISIBLE);
                        webView.goBack();
                    } else {
                        finish();
                    }
                    break;
                case R.id.close:
                    finish();
                    break;
            }
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            toolbarTitle.setText(title);
        }
    };
}
