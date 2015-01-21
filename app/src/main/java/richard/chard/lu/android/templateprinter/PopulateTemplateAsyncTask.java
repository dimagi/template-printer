package richard.chard.lu.android.templateprinter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

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
public class PopulateTemplateAsyncTask extends AsyncTask<Object, Void, Pair<File, PopulateTemplateAsyncTask.PopulateListener>> {

    public interface PopulateListener {

        public void onFinished(File result);

    }

    private static final int BUFFER_SIZE = 1024;
    private static final String CONTENT_FILE_NAME = "content.xml";

    @Override
    protected Pair<File, PopulateListener> doInBackground(Object... params) {

        File input = (File) params[0];
        Bundle values = (Bundle) params[1];
        PopulateListener listener = (PopulateListener) params[2];

        Pair<File, PopulateListener> result;

        try {
            result = new Pair<>(
                    populate(input, values),
                    listener
            );
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        return result;
    }

    @Override
    protected void onPostExecute(Pair<File, PopulateListener> result) {

        result.second.onFinished(result.first);

    }

    // http://isip-blog.blogspot.com/2010/04/extracting-xml-files-from-odt-file-in.html
    // http://stackoverflow.com/questions/11502260/modifying-a-text-file-in-a-zip-archive-in-java
    private static File populate(File input, Bundle values) throws IOException {

        File output;

        if (input.getName().endsWith(".odt")) {

            String inputName = input.getName().substring(0, input.getName().length() - 4);

            output = new File(inputName + "-out.odt");

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
                    while ((length = inputStream.read(buffer)) > 0) {
                        String text = new String(Arrays.copyOf(buffer, length));
                        if (text.contains("{{")) {
                            text = replace(text, values);
                            outputStream.write(text.getBytes(), 0, text.getBytes().length);
                        } else {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                } else {
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

        return output;
    }

    private static String replace(String input, Bundle values) {

        String[] tokens = input.split("(\\{{2})|(\\}{2})");

        // every 2nd token is a attribute formerly enclosed in {{ }}
        for (int i=1; i<tokens.length; i+=2) {
            String key = TextUtils.join("", tokens[i].split(" "));
            if (values.containsKey(key) && (key = values.getString(key)) != null) {
                tokens[i] = key;
            } else {
                tokens[i] = "{{" + tokens[i] + "}}";
            }
        }

        return TextUtils.join("", tokens);
    }
}
