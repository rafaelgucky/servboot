package net.servboot.service;

import net.servboot.database.DataBase;

import java.util.*;
import java.io.*;
import java.sql.*;

public class FileService {
    public int save(File file) {
        Connection connection = DataBase.getConnection();
        try(
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO FILE (FILENAME, FILESIZE, FILETYPE, FILECONTENT)" +
                    "VALUES ( ?, ?, ?, ? );");
                PreparedStatement getIdStatement = connection.prepareStatement("SELECT FILEID FROM FILE WHERE FILENAME = ?");
                InputStream inputStream = new FileInputStream(file);
        ){
            statement.setString(1, file.getName());
            statement.setLong(2, file.length());
            statement.setString(3, file.getName().substring(file.getName().lastIndexOf('.') + 1));
            statement.setBlob(4, inputStream);
            if(statement.executeUpdate() > 0){
                getIdStatement.setString(1, file.getName());
                ResultSet resultSet = getIdStatement.executeQuery();
                if(!resultSet.next()) return -1;
                return resultSet.getInt("fileid");
            } else {
                return -1;
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        } catch (FileNotFoundException fnf){
            fnf.printStackTrace();
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
        return -1;
    }

    public Map<String, InputStream> find(String fileName){
        Connection connection = DataBase.getConnection();
        Map<String, InputStream> map = new HashMap<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM FILE WHERE FILENAME = ?");
            statement.setString(1, fileName);
            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                if(rs.getString("FILENAME").equals(fileName)) {
                    map.put(rs.getString("FILENAME"), rs.getBinaryStream("FILECONTENT"));
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;
    }

    public Map<String, InputStream> find(int id){
        Connection connection = DataBase.getConnection();
        Map<String, InputStream> map = new HashMap<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM FILE WHERE FILEID = ?");
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                if(rs.getInt("FILEID") == id) {
                    map.put(rs.getString("FILENAME"), rs.getBinaryStream("FILECONTENT"));
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;
    }

    public long count(){
        Connection connection = DataBase.getConnection();

        try(
            Statement stmt = connection.createStatement();
        ) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(1) AS COUNT FROM FILE");
            if(rs.next()){
                return rs.getLong("COUNT");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public List<String> findAllNames(){
        List<String> names;
        Connection connection = DataBase.getConnection();

        try(
            Statement stmt = connection.createStatement();
        ) {
            ResultSet rs = stmt.executeQuery("SELECT FILENAME FROM FILE");
            names = new LinkedList<>();
            while(rs.next()){
                names.add(rs.getString("FILENAME"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return names;
    }
}
