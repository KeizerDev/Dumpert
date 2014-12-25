package io.jari.dumpert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * JARI.IO
 * Date: 23-12-14
 * Time: 0:33
 */
public class About extends Base {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dontApplyTheme = true;
        super.onCreate(savedInstanceState);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.about);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://webartisans.nl/")));
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
