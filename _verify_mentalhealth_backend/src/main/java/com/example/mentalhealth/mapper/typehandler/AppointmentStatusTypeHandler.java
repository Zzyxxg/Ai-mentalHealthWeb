package com.example.mentalhealth.mapper.typehandler;

import com.example.mentalhealth.enums.AppointmentStatus;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(AppointmentStatus.class)
public class AppointmentStatusTypeHandler extends BaseTypeHandler<AppointmentStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AppointmentStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public AppointmentStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return rs.wasNull() ? null : AppointmentStatus.fromCode(code);
    }

    @Override
    public AppointmentStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return rs.wasNull() ? null : AppointmentStatus.fromCode(code);
    }

    @Override
    public AppointmentStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return cs.wasNull() ? null : AppointmentStatus.fromCode(code);
    }
}
