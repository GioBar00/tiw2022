package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.SubFolder;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class is the Data Access Object for the Document
 */
public class DocumentDAO {

    private final Connection connection;

    public DocumentDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean doesDocumentExists() {
        return false;
    }

    public boolean checkOwner(int userId, int documentId) throws SQLException {
        String query = "SELECT user_iduser FROM (document d INNER JOIN subfolder s ON d.subforlder_idsubforlder = s.idsubforlder) INNER JOIN folder f ON s.folder_idfolder = f.idfolder WHERE iddocument = ? AND user_iduser = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(documentId));
            statement.setString(2, String.valueOf(userId));
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        }
    }

    public Document getDocument(String documentId) throws SQLException {
        String query = "SELECT * FROM document WHERE iddocument = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, documentId);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst())
                return null;
            resultSet.next();
            return new Document(resultSet.getInt("iddocument"),
                    resultSet.getString("name"),
                    resultSet.getString("format"),
                    resultSet.getString("summary"),
                    (Date) new SimpleDateFormat("dd/MM/yyyy").parse(resultSet.getString("creationDate")),
                    resultSet.getInt("subfolder_idsubfolder")
            );


        } catch (ParseException e) {
            return null;
        }
    }

    public SubFolder getSubFolder() {
        return null;
    }

    public void moveDocument() {

    }
}
