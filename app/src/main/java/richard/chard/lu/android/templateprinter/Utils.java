package richard.chard.lu.android.templateprinter;

import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Richard Lu
 */
public abstract class Utils {

    private static final String FORMAT_REGEX_WITH_DELIMITER = "((?<=%2$s)|(?=%1$s))";

    private static final Logger LOG = Logger.create(Utils.class);

    /**
     * Concatenate all Strings in a String array to one String.
     *
     * @param strings String array to join
     * @return Joined String
     */
    public static String join(String[] strings) {
        return TextUtils.join("", strings);
    }

    /**
     * Get the last element of a String array.
     * @param strings String array
     * @return Last element
     */
    public static String last(String[] strings) {
        return strings[strings.length - 1];
    }

    /**
     * Remove all occurrences of the specified String segment
     * from the input String.
     *
     * @param input String input to remove from
     * @param toRemove String segment to remove
     * @return input with all occurrences of toRemove removed
     */
    public static String remove(String input, String toRemove) {
        return TextUtils.join("", input.split(toRemove));
    }

    /**
     * Split a String while keeping the specified start and end delimiters.
     *
     * Sources:
     * http://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
     *
     * @param input String to split
     * @param delimiterStart Start delimiter; will split immediately before this delimiter
     * @param delimiterEnd End delimiter; will split immediately after this delimiter
     * @return Split string array
     */
    public static String[] splitKeepDelimiter(
            String input,
            String delimiterStart,
            String delimiterEnd) {
        LOG.trace("Entry, input={}, delimiterStart={}, delimiterEnd={}",
                input,
                delimiterStart,
                delimiterEnd
        );

        String delimiter = String.format(FORMAT_REGEX_WITH_DELIMITER, delimiterStart, delimiterEnd);

        String[] result = input.split(delimiter);
        LOG.trace("Exit, result={}", result);
        return result;
    }

}
