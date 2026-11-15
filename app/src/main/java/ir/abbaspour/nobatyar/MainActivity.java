package ir.abbaspour.nobatyar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.util.Base64;

public final class MainActivity extends Activity {
    private static final String APP_URL = "https://raymand.site/nobat-ranandegan/";
    private static final String ADMIN_LOGIN_URL = "https://raymand.site/wp-login.php?redirect_to=https%3A%2F%2Fraymand.site%2Fnobat-ranandegan%2F";
    private static final int GREEN = Color.rgb(16, 36, 31);
    private static final int GOLD = Color.rgb(200, 148, 50);
    private WebView webView;
    private ProgressBar progress;
    private Typeface persianFont;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(GREEN);
        getWindow().setNavigationBarColor(GREEN);
        getWindow().getDecorView().setSystemUiVisibility(0);
        CookieManager.getInstance().setAcceptCookie(true);
        persianFont = Typeface.createFromAsset(getAssets(), "fonts/iran-black.ttf");
        buildScreen();
        webView.loadUrl(APP_URL);
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 245, 239));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(12), dp(8), dp(12), dp(8));
        bar.setBackgroundColor(GREEN);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.nobat_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bar.addView(logo, new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView title = new TextView(this);
        title.setText("نوبت‌یار عباسپور");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(persianFont);
        title.setPadding(dp(10), 0, dp(8), 0);
        bar.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button driver = navButton("پنل راننده", false);
        driver.setOnClickListener(v -> webView.loadUrl(APP_URL));
        bar.addView(driver);

        Button manager = navButton("ورود مدیر", true);
        manager.setOnClickListener(v -> webView.loadUrl(ADMIN_LOGIN_URL));
        LinearLayout.LayoutParams managerParams = new LinearLayout.LayoutParams(-2, dp(40));
        managerParams.setMarginStart(dp(7));
        bar.addView(manager, managerParams);
        root.addView(bar, new LinearLayout.LayoutParams(-1, -2));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(247, 245, 239));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                return host != null && !host.equalsIgnoreCase("raymand.site");
            }
            @Override public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                if (url.startsWith("https://raymand.site/")) injectBranding(view);
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private Button navButton(String text, boolean primary) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setTypeface(persianFont);
        b.setTextColor(primary ? GREEN : Color.WHITE);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(primary ? GOLD : Color.rgb(35, 64, 55));
        shape.setCornerRadius(dp(12));
        b.setBackground(shape);
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setMinHeight(dp(40));
        b.setMinimumHeight(dp(40));
        return b;
    }

    private void injectBranding(WebView view) {
        try {
            String logo = base64("nobat-logo.png");
            String iran = base64("fonts/iran-black.ttf");
            String poster = base64("fonts/posters-italic.ttf");
            String ethno = base64("fonts/ethnocentric.otf");
            String css = "@font-face{font-family:ABPDIran;src:url(data:font/ttf;base64,"+iran+")}"
                    + "@font-face{font-family:ABPDPoster;src:url(data:font/ttf;base64,"+poster+")}"
                    + "@font-face{font-family:ABPDEthno;src:url(data:font/otf;base64,"+ethno+")}"
                    + ".abpd-wrap,.abpd-wrap button,.abpd-wrap input,.abpd-wrap select,.abpd-wrap textarea{font-family:ABPDIran,Tahoma,sans-serif!important}"
                    + ".abpd-wrap .abpd-logo{background:transparent url(data:image/png;base64,"+logo+") center/contain no-repeat!important;color:transparent!important}"
                    + ".abpd-wrap .abpd-intro h1{font-family:ABPDIran,Tahoma,sans-serif!important}"
                    + ".abpd-wrap .abpd-eyebrow{letter-spacing:.02em}"
                    + "body{background:#f7f5ef!important}";
            String js = "(function(){var s=document.getElementById('abpd-android-brand');if(!s){s=document.createElement('style');s.id='abpd-android-brand';document.head.appendChild(s);}s.textContent='"+css+"';})();";
            view.evaluateJavascript(js, null);
        } catch (Exception ignored) { }
    }

    private String base64(String asset) throws Exception {
        try (InputStream in = getAssets().open(asset); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
