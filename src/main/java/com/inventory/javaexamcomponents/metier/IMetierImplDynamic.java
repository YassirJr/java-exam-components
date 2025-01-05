package com.javaoop.examen.metier;

import com.javaoop.examen.database.SingletonConnexionDB;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IMetierImpl<T> implements IMetier<T> {

    private final Connection connexionDB = SingletonConnexionDB.getConnexion();
    private final String tableName;
    private final Class<T> type;

    public IMetierImpl(String tableName, Class<T> type) {
        this.tableName = tableName;
        this.type = type;
    }

    @Override
    public List<T> findAll() {
        List<T> resultList = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;

        try (PreparedStatement statement = connexionDB.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                T entity = mapResultSetToEntity(resultSet);
                resultList.add(entity);
            }
        } catch (SQLException e) {
            System.out.println("Error during findAll: " + e.getMessage());
        }

        return resultList;
    }

    @Override
    public T findById(Integer id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connexionDB.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToEntity(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("Error during findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void save(T t) {
        try {
            String query = buildInsertQuery(t);
            try (PreparedStatement statement = connexionDB.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                setPreparedStatementParams(statement, t);
                statement.executeUpdate();

                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    Field idField = type.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(t, keys.getInt(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Error during save: " + e.getMessage());
        }
    }

    @Override
    public void update(Integer id, T t) {
        try {
            String query = buildUpdateQuery(t);
            try (PreparedStatement statement = connexionDB.prepareStatement(query)) {
                setPreparedStatementParams(statement, t);
                statement.setInt(getFieldCount(t) + 1, id);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Error during update: " + e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connexionDB.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error during delete: " + e.getMessage());
        }
    }

    @Override
    public T search(String keyword) {
        String query = "SELECT * FROM " + tableName + " WHERE name LIKE ?";
        try (PreparedStatement statement = connexionDB.prepareStatement(query)) {
            statement.setString(1, "%" + keyword + "%");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToEntity(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("Error during search: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    private T mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        try {
            T entity = type.getDeclaredConstructor().newInstance();
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = resultSet.getObject(field.getName());
                field.set(entity, value);
            }
            return entity;
        } catch (Exception e) {
            throw new SQLException("Error mapping ResultSet to entity: " + e.getMessage());
        }
    }

    private String buildInsertQuery(T t) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (Field field : t.getClass().getDeclaredFields()) {
            columns.append(field.getName()).append(",");
            values.append("?,");
        }

        columns.setLength(columns.length() - 1);
        values.setLength(values.length() - 1);

        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
    }

    private String buildUpdateQuery(T t) {
        StringBuilder updates = new StringBuilder();

        for (Field field : t.getClass().getDeclaredFields()) {
            updates.append(field.getName()).append("=?,");
        }

        updates.setLength(updates.length() - 1);

        return "UPDATE " + tableName + " SET " + updates + " WHERE id=?";
    }

    private void setPreparedStatementParams(PreparedStatement statement, T t) throws Exception {
        int index = 1;
        for (Field field : t.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            statement.setObject(index++, field.get(t));
        }
    }

    private int getFieldCount(T t) {
        return t.getClass().getDeclaredFields().length;
    }
}
