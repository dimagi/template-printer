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

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.List;

import richard.chard.lu.android.templateprinter.DocTypeEnum;
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

    private static final String PREFERENCE_KEY_OUTPUT = "output_folder_path";
    // TODO: load default template as resource
    private static final String PREFERENCE_KEY_TEMPLATE = "template_file_path";

    private static final int REQUEST_OUTPUT = 0;
    private static final int REQUEST_TEMPLATE = 1;

    private static final File DEFAULT_OUTPUT_FOLDER = new File(
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
            ),
            "TemplatePrinter"
    );

    private String outputFolderPath;

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
                String path = uri.getPath();

                if (DocTypeEnum.isSupportedExtension(Utils.getExtension(path))) {

                    templateFilePath = uri.getPath();

                    preferences.edit()
                            .putString(
                                    PREFERENCE_KEY_TEMPLATE,
                                    templateFilePath
                            )
                            .apply();

                    ((TextView) findViewById(R.id.textview_template)).setText(
                            Utils.last(templateFilePath.split("/"))
                    );

                } else {
                    showToast(R.string.not_supported_file_format);
                }

            } else {
                showToast(R.string.no_file_selected);
            }

        } else if (requestCode == REQUEST_OUTPUT) {

            if (resultCode == RESULT_OK
                    && data != null) {

                Uri uri = data.getData();

                if (new File(uri.getPath()).isDirectory()) {

                    outputFolderPath = uri.getPath();

                    preferences.edit()
                            .putString(
                                    PREFERENCE_KEY_OUTPUT,
                                    outputFolderPath
                            )
                            .apply();

                    ((TextView) findViewById(R.id.textview_output)).setText(
                            Utils.last(outputFolderPath.split("/"))
                    );

                } else {
                    showToast(R.string.not_a_folder);
                }

            } else {
                showToast(R.string.no_folder_selected);
            }

        }

        if (templateFilePath != null && outputFolderPath != null) {

            findViewById(R.id.button_populate).setEnabled(true);

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

                startFileBrowser(false);
                break;

            case R.id.button_output:

                startFileBrowser(true);
                break;

            case R.id.button_populate:

                Bundle data = getIntent().getExtras();

                if (data != null) {
                    startPopulateTask(data);
                } else {
                    showErrorDialog(R.string.no_data);
                }
                break;
        }

        LOG.trace("Exit");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.trace("Entry");

        super.onCreate(savedInstanceState);

        preferences= getPreferences(MODE_PRIVATE);

        outputFolderPath = preferences.getString(
                PREFERENCE_KEY_OUTPUT,
                DEFAULT_OUTPUT_FOLDER.getPath()
        );
        templateFilePath = preferences.getString(
                PREFERENCE_KEY_TEMPLATE,
                null
        );

        setContentView(R.layout.activity_main);

        findViewById(R.id.button_template).setOnClickListener(this);
        findViewById(R.id.button_output).setOnClickListener(this);
        findViewById(R.id.button_populate).setOnClickListener(this);

        if (templateFilePath != null) {

            ((TextView) findViewById(R.id.textview_template)).setText(
                    Utils.last(templateFilePath.split("/"))
            );

        }
        if (outputFolderPath != null) {

            ((TextView) findViewById(R.id.textview_output)).setText(
                    Utils.last(outputFolderPath.split("/"))
            );

        }
        if (templateFilePath != null && outputFolderPath != null) {

            findViewById(R.id.button_populate).setEnabled(true);

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

    private void showErrorDialog(int messageResId) {
        showErrorDialog(getString(messageResId));
    }

    /**
     * Displays an error dialog with the specified message.
     * Activity will quit upon exiting the dialog.
     *
     * @param message Error message
     */
    private void showErrorDialog(String message) {
        LOG.trace("Entry, message={}", message);

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

    private void showToast(int messageResId) {
        LOG.trace("Entry");

        Toast.makeText(
                getApplicationContext(),
                getString(messageResId),
                Toast.LENGTH_SHORT
        ).show();

        LOG.trace("Exit");
    }

    /**
     * Attempts to open the document with its default viewer.
     * If default viewer is unavailable, opens a dialog from which
     * a viewer application can be selected.
     *
     * @param document Document to open
     */
    private void startDocumentViewer(File document) {
        LOG.trace("Entry");

        ComponentName target = null;

        DocTypeEnum docType = DocTypeEnum.getFromExtension(
                Utils.getExtension(
                        document.getName()
                )
        );

        String viewerPackage = docType.VIEWER_PACKAGE;

        if (viewerPackage != null) {

            PackageManager packageManager = getPackageManager();

            List<ResolveInfo> results = packageManager.queryIntentActivities(
                    new Intent()
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setAction(Intent.ACTION_MAIN)
                    ,
                    0
            );

            for (ResolveInfo resolveInfo : results) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;

                if (activityInfo.packageName.equals(viewerPackage)) {
                    target = new ComponentName(
                            activityInfo.packageName,
                            activityInfo.name
                    );

                    break;
                }
            }

        }

        Intent intent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.fromFile(document));

        // TODO: office mobile doesn't seem to open?
        if (target == null) {
            intent.setType(docType.MIMETYPE);
        } else {
            intent.setComponent(target);
        }

        startActivity(intent);

        LOG.trace("Exit");
    }

    /**
     * Starts the file/folder picker.
     */
    private void startFileBrowser(boolean folder) {
        LOG.trace("Entry");

        Intent chooseTemplateIntent = new Intent(
                this,
                FilePickerActivity.class
        ).putExtra(
                FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                false
        ).putExtra(
                FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,
                false
        ).putExtra(
                FilePickerActivity.EXTRA_MODE,
                folder ? FilePickerActivity.MODE_DIR : FilePickerActivity.MODE_FILE
        );

        startActivityForResult(
                chooseTemplateIntent,
                folder ? REQUEST_OUTPUT : REQUEST_TEMPLATE
        );

        LOG.trace("Exit");
    }

    private void startPopulateTask(Bundle data) {
        LOG.trace("Entry");

        File template = new File(templateFilePath);
        File outputFolder = new File(outputFolderPath);

        if (template.exists() && !template.isDirectory()) {

            if (outputFolder.mkdirs() || outputFolder.isDirectory()) {

                new PopulateTemplateAsyncTask(
                        template,
                        outputFolder,
                        data,
                        this
                ).execute();

            } else {
                showToast(R.string.unable_to_create_output_folder);
            }

        } else {
            showToast(R.string.template_does_not_exist);
        }

        LOG.trace("Exit");
    }
}
