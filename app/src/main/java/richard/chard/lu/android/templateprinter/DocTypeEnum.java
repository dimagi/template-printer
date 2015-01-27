package richard.chard.lu.android.templateprinter;

/**
 * @author Richard Lu
 */
public enum DocTypeEnum {

    DOCX("word/document.xml"),
    ODT("content.xml");

    public static DocTypeEnum getFromExtension(String extension) {
        for (DocTypeEnum docType : DocTypeEnum.values()) {
            if (docType.toString().equalsIgnoreCase(extension)) {
                return docType;
            }
        }
        return null;
    }

    public static boolean isSupportedExtension(String extension) {
        return getFromExtension(extension) != null;
    }

    public final String CONTENT_FILE_NAME;

    private DocTypeEnum(String contentFileName) {
        this.CONTENT_FILE_NAME = contentFileName;
    }

}
