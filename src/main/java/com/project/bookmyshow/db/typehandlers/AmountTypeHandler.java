package com.project.bookmyshow.db.typehandlers;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AmountTypeHandler implements TypeHandler<Double> {

    public void setParameter(PreparedStatement preparedStatement, int i, Double t, JdbcType jdbcType) throws SQLException {
        preparedStatement.setObject(i, (int) (t*100));
    }

    public Double getResult(ResultSet resultSet, String s) throws SQLException {
        return Integer.parseInt(resultSet.getString(s))/100.0;
    }

    public Double getResult(ResultSet resultSet, int i) throws SQLException {
        return Integer.parseInt(resultSet.getString(i))/100.0;
    }

    public Double getResult(CallableStatement callableStatement, int i) throws SQLException {
        return Integer.parseInt(callableStatement.getString(i))/100.0;
    }
}
