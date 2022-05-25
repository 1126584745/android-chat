/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import butterknife.BindView;
import cn.wildfire.chat.kit.workspace.JsApi;
import wendu.dsbridge.DWebView;

public class WfcWebViewActivity extends WfcBaseActivity {
    private String url;

    @BindView(R2.id.webview)
    DWebView webView;
    private JsApi jsApi;

    public static void loadUrl(Context context, String title, String url) {
        Intent intent = new Intent(context, WfcWebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    public static void loadHtmlContent(Context context, String title, String htmlContent) {
        Intent intent = new Intent(context, WfcWebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", htmlContent);
        context.startActivity(intent);
    }

    @Override
    protected int contentLayout() {
        return R.layout.activity_webview;
    }

    @Override
    protected int menu() {
        return R.menu.web;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

    @Override
    protected void afterViews() {
        url = getIntent().getStringExtra("url");
        String htmlContent = getIntent().getStringExtra("content");

        String title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        jsApi = new JsApi(this, webView, url);
        webView.addJavascriptObject(jsApi, null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String webTitle = view.getTitle();
                if (!TextUtils.isEmpty(webTitle)) {
                    if (TextUtils.isEmpty(title) || !TextUtils.equals(webTitle, "about:blank")) {
                        setTitle(webTitle);
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals(WfcWebViewActivity.this.url)) {
                    jsApi.setCurrentUrl(url);
                    return false;
                }
                WfcWebViewActivity.loadUrl(WfcWebViewActivity.this, "loading", url);
                return true;
            }
        });
        if (!TextUtils.isEmpty(htmlContent)) {
            webView.loadDataWithBaseURL("", htmlContent, "text/html", "UTF-8", "");
        } else {
            webView.loadUrl(url);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!jsApi.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
