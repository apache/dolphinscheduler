package org.apache.dolphinscheduler.common.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.datanucleus.store.rdbms.exceptions.UnsupportedDataTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;


public class SqlUtils {

    public static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    /**
     * 解析select语句查询列名
     *
     * @param dbType database type
     * @param sql select sql
     * @return select columns
     * @throws RuntimeException
     */
    public static String[] resolveStandardSelectSqlColumns(DbType dbType, String sql) {
        String[] columns;

        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType.getDesc());
            ValidUtils.notNull(parser, String.format("database driver [%s] is not support", dbType.toString()));

            SQLStatement sqlStatement = parser.parseStatement();
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
            SQLSelect sqlSelect = sqlSelectStatement.getSelect();

            List<SQLSelectItem> selectItemList = null;

            if (sqlSelect.getQuery() instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) sqlSelect.getQuery();
                selectItemList = block.getSelectList();
            } else if (sqlSelect.getQuery() instanceof SQLUnionQuery) {
                SQLUnionQuery unionQuery = (SQLUnionQuery) sqlSelect.getQuery();
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) unionQuery.getRight();
                selectItemList = block.getSelectList();
            }

            ValidUtils.notNull(selectItemList, String.format("select query type [%s] is not support", sqlSelect.getQuery().toString()));

            columns = new String[selectItemList.size()];
            for (int i = 0; i < selectItemList.size(); i++) {
                SQLSelectItem item = selectItemList.get(i);

                if (item.getAlias() != null) {
                    columns[i] = item.getAlias();
                } else if (item.getExpr() != null) {
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr expr = (SQLPropertyExpr) item.getExpr();
                        columns[i] = expr.getName();
                    } else if (item.getExpr() instanceof SQLIdentifierExpr) {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();
                        columns[i] = expr.getName();
                    }
                } else {
                    throw new RuntimeException(String.format("column [ %s ] parse fail", item.toString()));
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(),e);
            return null;
        }

        return columns;
    }

    /**
     * 解析sql语句select from表名
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlSelectTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.SELECT);
    }

    /**
     * 解析sql语句insert表名
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlInsertTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.INSERT);
    }

    /**
     * 解析sql语句update表名
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlUpdateTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.UPDATE);
    }

    /**
     * 解析sql语句delete表名
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlDeleteTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.DELETE);
    }

    /**
     * 解析标准sql语句查询表名
     *
     * @param dbType database type
     * @param sql sql
     * @return table map Token:List
     * @throws UnsupportedDataTypeException
     */
    public static Map<Token, List<String>> resolveSqlTables(DbType dbType, String sql) throws SQLException {
        Map<Token, List<String>> tableMap = new HashMap<>();
        tableMap.put(Token.SELECT, new ArrayList<>());
        tableMap.put(Token.INSERT, new ArrayList<>());
        tableMap.put(Token.UPDATE, new ArrayList<>());
        tableMap.put(Token.DELETE, new ArrayList<>());

        try {
            List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType.getDesc());

            for (SQLStatement stmt : stmtList) {
                SchemaStatVisitor visitor = getSchemaStatVisitor(dbType, stmt);

                if (visitor.getTables() != null) {
                    for (TableStat.Name name : visitor.getTables().keySet()) {
                        TableStat tableStat = visitor.getTables().get(name);
                        if (tableStat.getSelectCount() > 0) {
                            tableMap.get(Token.SELECT).add(name.getName());
                        }

                        if (tableStat.getInsertCount() > 0) {
                            tableMap.get(Token.INSERT).add(name.getName());
                        }

                        if (tableStat.getUpdateCount() > 0) {
                            tableMap.get(Token.UPDATE).add(name.getName());
                        }

                        if (tableStat.getDeleteCount() > 0) {
                            tableMap.get(Token.DELETE).add(name.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(),e);
            throw new SQLException(e);
        }

        return tableMap;
    }

    public static String[] parseKeywordsColumns(DbType dbType, String[] columns) {
        if (columns == null) {
            return columns;
        }

        for (int i = 0; i < columns.length; i++) {
            columns[i] = parseKeywordsColumn(dbType, columns[i]);
        }

        return columns;
    }

    public static String parseKeywordsColumn(DbType dbType, String column) {
        if (column == null) {
            return column;
        }

        column = column.trim();
        column = column.replace("`", "");
        column = column.replace("\"", "");

        if (DbType.MYSQL.name().equals(dbType.toString())){
            column = String.format("`%s`", column);
        }else if (DbType.POSTGRESQL.name().equals(dbType.toString())){
            column = String.format("\"%s\"", column);
        }else if (DbType.SQLSERVER.name().equals(dbType.toString())){
            column = String.format("`%s`", column);
        }else if (DbType.ORACLE.name().equals(dbType.toString())){
            column = String.format("\"%s\"", column);
        }

        return column;
    }

    private static SchemaStatVisitor getSchemaStatVisitor(DbType dbType, SQLStatement stmt) {
        SchemaStatVisitor visitor;

        switch (dbType) {
            case MYSQL:
                visitor = new MySqlSchemaStatVisitor();
                break;
            case POSTGRESQL:
                visitor = new PGSchemaStatVisitor();
                break;
            case ORACLE:
                visitor = new OracleSchemaStatVisitor();
                break;
            case SQLSERVER:
                visitor = new SQLServerSchemaStatVisitor();
                break;
            default:
                throw new UnsupportedDataTypeException(dbType.getDesc());
        }

        stmt.accept(visitor);
        return visitor;
    }

}
