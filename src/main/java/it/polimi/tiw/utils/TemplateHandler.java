package it.polimi.tiw.utils;

import it.polimi.tiw.enums.TemplateResolverParameters;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

/**
 * This class handles the creation of the template engine.
 */
public abstract class TemplateHandler {

    /**
     * Creates a {@link TemplateEngine}.
     * @param context the {@link ServletContext} of the {@link javax.servlet.Servlet}.
     * @return the {@link TemplateEngine}.
     */
    public static TemplateEngine getTemplateEngine(ServletContext context) {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix(TemplateResolverParameters.PREFIX.getValue());
        templateResolver.setSuffix(TemplateResolverParameters.SUFFIX.getValue());

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
