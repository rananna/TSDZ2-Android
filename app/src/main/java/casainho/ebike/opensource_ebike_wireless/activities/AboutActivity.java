package casainho.ebike.opensource_ebike_wireless.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.WebView;
import casainho.ebike.opensource_ebike_wireless.R;

public class AboutActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String versionName = "";
        PackageInfo pInfo;
        {
            try {
                pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        String content = "<h1>OpenSource EBike Wireless</h1>\n" +
                "<p>Version: " + versionName + "\n" +
                "<p><br>\n" +
                "<p>See the documentacion here: <a href=\"https://opensourceebike.github.io\">https://opensourceebike.github.io</a></p>\n" +
                "<p><br>\n" +
                "<p><br>\n" +
                "<p>This app as developed based on the OpenSource TSDZ2-ESP32 app: <a href=\"https://github.com/TSDZ2-ESP32\">https://github.com/TSDZ2-ESP32</a></p>\n";

        // init webView
        webView = (WebView) findViewById(R.id.simpleWebView);
        // displaying text in WebView
        webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
    }
}