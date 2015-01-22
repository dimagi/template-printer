package richard.chard.lu.android.templateprinter.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import richard.chard.lu.android.templateprinter.Logger;
import richard.chard.lu.android.templateprinter.PopulateTemplateAsyncTask;
import richard.chard.lu.android.templateprinter.R;
import richard.chard.lu.android.templateprinter.Utils;

/**
 * @author Richard Lu
 */
public class MainActivity extends Activity
        implements PopulateTemplateAsyncTask.PopulateListener, DialogInterface.OnClickListener,
        View.OnClickListener {


    private static final Logger LOG = Logger.create(MainActivity.class);

    // TODO: no downloads folder?
    private static final File OUTPUT_FOLDER = new File(
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
            ),
            "TemplatePrinter"
    );

    private static final String PREFERENCE_KEY_TEMPLATE = "template_file_path";

    private static final int REQUEST_TEMPLATE = 0;

    private static final String DOCUMENT_VIEWER_PACKAGE = "at.tomtasche.reader";

    private SharedPreferences preferences;

    private String templateFilePath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOG.trace("Entry, requestCode={}, resultCode={}",
                requestCode,
                resultCode
        );

        if (requestCode == REQUEST_TEMPLATE) {

            if (resultCode == RESULT_OK
                    && data != null) {

                Uri uri = data.getData();

                LOG.debug("data.getDataString()={}", data.getDataString());

                LOG.debug("uri.toString()={}", uri.toString());

                LOG.debug("uri.getPath()={}", uri.getPath());

                if (uri.getPath().endsWith(".odt")) {

                    templateFilePath = Utils.getFilePathFromUri(uri);

                    preferences.edit()
                            .putString(
                                    PREFERENCE_KEY_TEMPLATE,
                                    templateFilePath
                            )
                            .apply();

                    findViewById(R.id.button_populate).setEnabled(true);

                    ((TextView) findViewById(R.id.textview_template)).setText(
                            Utils.last(templateFilePath.split("/"))
                    );

                } else {
                    showToast(getString(R.string.not_odt_file));
                }

            } else {
                showToast(getString(R.string.no_file_selected));
            }

        }

        LOG.trace("Exit");
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int clickId) {
        LOG.trace("Entry");

        finish();

        LOG.trace("Exit");
    }

    @Override
    public void onClick(View view) {
        LOG.trace("Entry");

        switch (view.getId()) {
            case R.id.button_template:

                startFileBrowser();
                break;

            case R.id.button_populate:

                Bundle data = getIntent().getExtras();

                if (data != null) {
                    startPopulateTask(
                            templateFilePath,
                            data
                    );
                } else {
                    showErrorDialog(getString(R.string.no_data));
                }
                break;
        }

        LOG.trace("Exit");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.trace("Entry");

        super.onCreate(savedInstanceState);

        if (OUTPUT_FOLDER.mkdirs() || OUTPUT_FOLDER.isDirectory()) {
            preferences= getPreferences(MODE_PRIVATE);

            templateFilePath = preferences.getString(PREFERENCE_KEY_TEMPLATE, null);

            setContentView(R.layout.activity_main);

            findViewById(R.id.button_template).setOnClickListener(this);
            findViewById(R.id.button_populate).setOnClickListener(this);

            if (templateFilePath != null) {

                findViewById(R.id.button_populate).setEnabled(true);

                ((TextView) findViewById(R.id.textview_template)).setText(
                        Utils.last(templateFilePath.split("/"))
                );

            }
        } else {
            showErrorDialog(getString(R.string.unable_to_create_output_folder));
        }

        LOG.trace("Exit");
    }

    @Override
    public void onError(String message) {
        LOG.trace("Entry, message={}", message);

        showErrorDialog(message);

        LOG.trace("Exit");
    }

    @Override
    public void onFinished(File result) {
        LOG.trace("Entry");

        startDocumentViewer(result);

        finish();

        LOG.trace("Exit");
    }

    private void showErrorDialog(String message) {
        LOG.trace("Entry");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(
                        R.string.ok,
                        this
                );

        dialogBuilder.show();

        LOG.trace("Exit");
    }

    private void showToast(String message) {
        LOG.trace("Entry");

        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();

        LOG.trace("Exit");
    }

    private void startDocumentViewer(File document) {
        LOG.trace("Entry");

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

            if (activityInfo.packageName.equals(DOCUMENT_VIEWER_PACKAGE)) {
                target = new ComponentName(
                        activityInfo.packageName,
                        activityInfo.name
                );

                break;
            }
        }

        Intent intent = new Intent()
                .setAction(android.content.Intent.ACTION_VIEW)
                .setData(Uri.fromFile(document));

        if (target == null) {
            intent.setType("application/*");
        } else {
            intent.setComponent(target);
        }

        startActivity(intent);

        LOG.trace("Exit");
    }

    private void startFileBrowser() {
        LOG.trace("Entry");

        Intent chooseTemplateIntent = new Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("file/*")
                .addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(
                chooseTemplateIntent,
                REQUEST_TEMPLATE
        );

        LOG.trace("Exit");
    }

    private void startPopulateTask(String templateFilePath, Bundle data) {
        LOG.trace("Entry");

        File template = new File(templateFilePath);

        new PopulateTemplateAsyncTask(
                template,
                OUTPUT_FOLDER,
                data,
                this
        ).execute();

        LOG.trace("Exit");
    }
}
