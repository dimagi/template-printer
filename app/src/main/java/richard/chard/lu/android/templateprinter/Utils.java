package richard.chard.lu.android.templateprinter;

import android.net.Uri;

/**
 * @author Richard Lu
 */
public abstract class Utils {

    public static String getFilePathFromUri(Uri uri) {
        return last(uri.getPath().split("//"));
    }

    public static String last(String[] strings) {
        return strings[strings.length - 1];
    }

}
