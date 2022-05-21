package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DocumentDAO;
import it.polimi.tiw.enums.TemplatePages;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "Document Details", value = "/document")
public class DocumentDetails extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * {@link Connection} to the database.
     */
    private Connection connection;
    /**
     * {@link TemplateEngine} to render the template.
     */
    private TemplateEngine templateEngine;

    /**
     * Initialize the {@link Connection} to the database and the {@link TemplateEngine}.
     *
     * @throws ServletException if the {@link Connection} to the database cannot be initialized.
     */
    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        connection = ConnectionHandler.getConnection(context);
        templateEngine = TemplateHandler.getTemplateEngine(context);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String identifier = request.getParameter("id");
        try {
            int id = Integer.parseInt(identifier);
            DocumentDAO documentDAO = new DocumentDAO(this.connection);
            User user = (User) request.getSession().getAttribute("user");
            if(documentDAO.checkOwner(user.id(),id)){

                Document document = documentDAO.getDocument(id);
                String path = "WEB-INF/home/documents/document";

                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                ctx.setVariable("document", document);
                templateEngine.process(path, ctx, response.getWriter());
            }
            else response.sendRedirect(String.valueOf(TemplatePages.HOME));

        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document id");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
        }
    }

    /**
     * Close the {@link Connection} to the database.
     */
    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException ignored) {
        }
    }
}
