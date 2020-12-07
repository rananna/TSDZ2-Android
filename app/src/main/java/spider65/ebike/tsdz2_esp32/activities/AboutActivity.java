package spider65.ebike.tsdz2_esp32.activities;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import spider65.ebike.tsdz2_esp32.R;

public class AboutActivity extends AppCompatActivity {

    WebView webView;
    String content="<h1>Heading 1</h1>\n" +
            "        <h2>Heading 2</h2>\n" +
            "        <p>This is some html. Look, here\\'s an <u>underline</u>.</p>\n" +
            "        <p>Look, this is <em>emphasized.</em> And here\\'s some <b>bold</b>.</p>\n" +
            "        <p>Here are UL list items:\n" +
            "        <ul>\n" +
            "        <li>One</li>\n" +
            "        <li>Two</li>\n" +
            "        <li>Three</li>\n" +
            "        </ul>\n" +
            "        <p>Here are OL list items:\n" +
            "        <ol>\n" +
            "        <li>One</li>\n" +
            "        <li>Two</li>\n" +
            "        <li>Three</li>\n" +
            "        </ol>";

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