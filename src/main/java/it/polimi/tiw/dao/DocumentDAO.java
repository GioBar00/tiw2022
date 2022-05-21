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

    public DocumentDAO(Connection connection){
        this.connection = connection;
    }

    public boolean doesDocumentExists(){
         return false;
    }

    public boolean CheckOwner(String username,int documentId) throws SQLException {
        String ownerId;
        String userId;
        String query = "SELECT idProprietario FROM documento WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, String.valueOf(documentId));
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.isBeforeFirst())
                return false;
            ownerId = resultSet.getString("idProprieratio");

        }
        query = "SELECT id FROM uset WHERE username = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, String.valueOf(documentId));
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.isBeforeFirst())
                return false;
            resultSet.next();
            userId = resultSet.getString("id");
        }

        return userId.equals(ownerId);
    }

    public Document getDocument (String documentId) throws SQLException {
        String query = "SELECT * FROM document WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, documentId);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.isBeforeFirst())
                return null;
            resultSet.next();
            return new Document(Integer.parseInt(resultSet.getString("id")),
                    resultSet.getString("name"),
                    resultSet.getString("format"),
                    resultSet.getString("summary"),
                    (Date) new SimpleDateFormat("dd/MM/yyyy").parse(resultSet.getString("creationDate")),
                    Integer.parseInt(resultSet.getString("ownerId")),
                    Integer.parseInt(resultSet.getString("subFolderId"))
                    );


        } catch (ParseException e) {
            return null;
        }
    }

    public SubFolder getSubFolder(){
        return null;
    }

    public void moveDocument() {

    }
}
