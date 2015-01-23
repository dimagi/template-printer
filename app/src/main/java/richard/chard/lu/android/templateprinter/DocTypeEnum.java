package richard.chard.lu.android.templateprinter;

/**
 * @author Richard Lu
 */
public enum DocTypeEnum {

    DOCX("docx", "word/document.xml", null, "application/msword"),
    ODT("odt", "content.xml", "at.tomtasche.reader", "application/*");

    public static DocTypeEnum getFromExtension(String extension) {
        for (DocTypeEnum docType : DocTypeEnum.values()) {
            if (docType.EXTENSION.equals(extension)) {
                return docType;
            }
        }
        return null;
    }

    public static boolean isSupportedExtension(String extension) {
        return getFromExtension(extension) != null;
    }

    public final String CONTENT_FILE_NAME;
    public final String EXTENSION;
    public final String MIMETYPE;
    public final String VIEWER_PACKAGE;

    private DocTypeEnum(String extension, String contentFileName, String viewerPackage, String mimeType) {
        this.CONTENT_FILE_NAME = contentFileName;
        this.EXTENSION = extension;
        this.MIMETYPE = mimeType;
        this.VIEWER_PACKAGE = viewerPackage;
    }

}
