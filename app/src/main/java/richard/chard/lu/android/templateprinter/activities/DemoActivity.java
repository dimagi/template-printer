package richard.chard.lu.android.templateprinter.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.List;

import richard.chard.lu.android.templateprinter.Logger;

/**
 * @author Richard Lu
 */
public class DemoActivity extends Activity {

    private static final Logger LOG = Logger.create(DemoActivity.class);

    private static final String TARGET_PACKAGE = "at.tomtasche.reader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.trace("Entry");

        super.onCreate(savedInstanceState);

        PackageManager packageManager = getPackageManager();

        List<ResolveInfo> results = packageManager.queryIntentActivities(
                new Intent()
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setAction(Intent.ACTION_MAIN)
                ,
                0
        );

        ComponentName target = null;

        for (ResolveInfo resolveInfo : results) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;

            if (activityInfo.packageName.equals(TARGET_PACKAGE)) {
                target = new ComponentName(
                        activityInfo.packageName,
                        activityInfo.name
                );
            }
        }

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        for (File file : downloadsFolder.listFiles()) {

            if (file.getName().endsWith(".odt")) {

                Intent intent = new Intent()
                        .setAction(android.content.Intent.ACTION_VIEW)
                        .setData(Uri.fromFile(file));

                if (target == null) {
                    intent.setType("application/*");
                } else {
                    intent.setComponent(target);
                }

                startActivity(intent);

                break;

            }

        }

        LOG.trace("Exit");
    }

    @Override
    protected void onPause() {
        LOG.trace("Entry");

        super.onPause();

        finish();

        LOG.trace("Exit");
    }

}
