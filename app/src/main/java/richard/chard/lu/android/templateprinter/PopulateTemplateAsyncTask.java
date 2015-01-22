package richard.chard.lu.android.templateprinter;

import android.os.AsyncTask;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Richard Lu
 */
public class PopulateTemplateAsyncTask extends AsyncTask<Void, Void, File> {

    public interface PopulateListener {

        public void onError(String message);
        public void onFinished(File result);

    }

    private static final int BUFFER_SIZE = 1024;
    private static final String CONTENT_FILE_NAME = "content.xml";

    private static final Logger LOG = Logger.create(PopulateTemplateAsyncTask.class);

    private final File input;
    private final File outputFolder;
    private final Bundle values;
    private final PopulateListener listener;

    public PopulateTemplateAsyncTask(File input, File outputFolder, Bundle values, PopulateListener listener) {
        LOG.trace("Entry, input={}, values={}",
                input,
                values
        );

        this.input = input;
        this.outputFolder = outputFolder;
        this.values = values;
        this.listener = listener;

        LOG.trace("Exit");
    }

    @Override
    protected File doInBackground(Void... params) {
        LOG.trace("Entry");

        File result = null;

        try {
            result = populate(input, values);
        } catch (Exception e) {
            listener.onError(e.getMessage());
        }

        LOG.trace("Exit");
        return result;
    }

    @Override
    protected void onPostExecute(File result) {
        LOG.trace("Entry");

        if (result != null) {
            listener.onFinished(result);
        }

        LOG.trace("Exit");
    }

    // http://isip-blog.blogspot.com/2010/04/extracting-xml-files-from-odt-file-in.html
    // http://stackoverflow.com/questions/11502260/modifying-a-text-file-in-a-zip-archive-in-java
    private File populate(File input, Bundle values) throws IOException {
        LOG.trace("Entry");

        File output;

        if (input.getName().endsWith(".odt")) {

            String inputName = input.getName().substring(0, input.getName().lastIndexOf('.'));

            output = new File(outputFolder, inputName + "-out.odt");

            ZipFile file = new ZipFile(input);
            Enumeration entries = file.entries();

            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(output));

            while (entries.hasMoreElements()) {

                ZipEntry entry = (ZipEntry) entries.nextElement();

                InputStream inputStream = file.getInputStream(entry);
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;

                outputStream.putNextEntry(new ZipEntry(entry.getName()));

                if (entry.getName().equals(CONTENT_FILE_NAME)) {

                    // read entire file as String
                    String fileText = "";

                    while ((length = inputStream.read(buffer)) > 0) {
                        fileText += new String(Arrays.copyOf(buffer, length));
                    }

                    if (fileText.contains("{{")) {
                        fileText = replace(fileText, values);
                    }

                    byte[] fileTextBytes = fileText.getBytes();

                    outputStream.write(fileTextBytes, 0, fileTextBytes.length);

                } else {

                    // straight up copy file
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                }

                outputStream.closeEntry();

            }

            outputStream.close();
            file.close();

        } else {
            throw new RuntimeException("Not an ODT file");
        }

        LOG.trace("Exit");
        return output;
    }

    private static String replace(String input, Bundle values) {
        LOG.trace("Entry");

        // TODO: check well-formed

        // Split input into tokens bounded by {{ and }}
        String[] tokens = Utils.splitKeepDelimiter(input, "\\{{2}", "\\}{2}");

        for (int i=0; i<tokens.length; i++) {
            String token = tokens[i];

            LOG.debug("token={}", token);

            // Every 2nd token is a attribute enclosed in {{ }}
            if (i % 2 == 1) {

                // Split token into tokenSplits bounded by < and >
                String[] tokenSplits = Utils.splitKeepDelimiter(token, "<|(\\}{2})", ">|(\\{{2})");

                // First and last tokenSplits are {{ and }}
                for (int j=1; j<tokenSplits.length-1; j++) {
                    String tokenSplit = tokenSplits[j];

                    LOG.debug("tokenSplit={}", tokenSplit);

                    // tokenSplit is key or whitespace
                    if (!tokenSplit.startsWith("<")) {

                        // Remove whitespace from key
                        String key = Utils.remove(tokenSplit, " ");

                        LOG.debug("key={}", key);

                        if (values.containsKey(key) && (key = values.getString(key)) != null) {
                            // populate with value
                            tokenSplits[j] = key;
                        } else {
                            // empty if not found
                            tokenSplits[j] = "";
                        }
                    }

                }

                // remove {{ and }}
                tokenSplits[0] = "";
                tokenSplits[tokenSplits.length-1] = "";

                tokens[i] = Utils.join(tokenSplits);
            }
        }

        String result = Utils.join(tokens);
        LOG.trace("Exit");
        return result;
    }
}
