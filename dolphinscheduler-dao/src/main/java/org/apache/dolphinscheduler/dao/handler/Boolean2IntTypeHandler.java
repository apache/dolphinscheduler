package org.apache.dolphinscheduler.dao.handler;

import org.apache.dolphinscheduler.dao.entity.Resource;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * MYSQL can convert Boolean to Int automatically, but POSTGRESQL can not,
 * so a typeHandler can be specified to solve this problem.
 *
 * @author 天仇
 * @see Resource#isDirectory()
 */
public class Boolean2IntTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        if (Objects.nonNull(parameter)) {
            ps.setInt(i, parameter ? 1 : 0);
        }
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int result = rs.getInt(columnName);
        return result == 0 && rs.wasNull() ? null : result == 1;
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int result = rs.getInt(columnIndex);
        return result == 0 && rs.wasNull() ? null : result == 1;
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int result = cs.getInt(columnIndex);
        return result == 0 && cs.wasNull() ? null : result == 1;
    }
}
