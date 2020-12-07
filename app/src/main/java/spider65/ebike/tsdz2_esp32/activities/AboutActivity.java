package spider65.ebike.tsdz2_esp32.activities;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import spider65.ebike.tsdz2_esp32.R;

public class AboutActivity extends AppCompatActivity {

    WebView webView;
    String content="<h1>OpenSource EBike Wireless</h1>\n" +
            "<p>See the documentacion here: <a href=\"https://opensourceebike.github.io\">https://opensourceebike.github.io</a></p>\n" +
            "<p><br>\n" +
            "<p><br>\n" +
            "<p>This app as developed based on the OpenSource TSDZ2-ESP32 app: <a href=\"https://github.com/TSDZ2-ESP32\">https://github.com/TSDZ2-ESP32</a></p>\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        // init webView
        webView = (WebView) findViewById(R.id.simpleWebView);
        // displaying text in WebView
        webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
    }
}