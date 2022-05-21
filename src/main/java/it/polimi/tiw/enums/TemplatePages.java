package it.polimi.tiw.enums;

/**
 * This enum contains all the templates file names.
 */
public enum TemplatePages {
    /**
     * The login template.
     */
    LOGIN("login"),
    REGISTER("register"),
    DOCUMENT("document"),
    DOCUMENTS("documents"),

    HOME("home"),

    CONTENT_MANAGEMENT("contentManagement");

    private final String value;

    TemplatePages(String value) {
        this.value = value;
    }

    /**
     * @return the file name of the template.
     */
    public String getValue() {
        return value;
    }
}
