package it.polimi.tiw.enums;

/**
 * This enum contains all the parameters used by the TemplateResolver.
 */
public enum TemplateResolverParameters {
    /**
     * The prefix of the path to the template.
     */
    PREFIX("/WEB-INF/"),
    /**
     * The suffix of the template.
     */
    SUFFIX(".html");

    private final String value;

    TemplateResolverParameters(String value) {
        this.value = value;
    }

    /**
     * @return the value of the parameter.
     */
    public String getValue() {
        return value;
    }
}
