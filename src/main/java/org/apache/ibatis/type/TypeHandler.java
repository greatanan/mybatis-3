/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */

/*
//mynote:
 MyBatis 中所有的类型转换器都继承了 TypeHandler 接 口 ，在 TypeHandler 接口中定义了如
              下四个方法 ， 这四个方法分为两类 ：
                 setParameter（）方法负责将数据 由Java类型  转换成JdbcType 类型 ：
                 getResult（）方法及其重载负责将数据由 JdbcType 类型转换成 Java 类型 。
* */
public interface TypeHandler<T> {

  //mynote: 在通过 PreparedStatement 为 SQL 语句绑定参数时 ，会将数据由Java 类型 转换成 JdbcType 类型
  void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

  /**
   * Gets the result.
   *
   * @param rs
   *          the rs
   * @param columnName
   *          Colunm name, when configuration <code>useColumnLabel</code> is <code>false</code>
   * @return the result
   * @throws SQLException
   *           the SQL exception
   *
   *           从 ResultSet 中获取数据时会调用此方法，会将数据由JdbcType 类型转换成  Java 类型
   */
  T getResult(ResultSet rs, String columnName) throws SQLException;

  T getResult(ResultSet rs, int columnIndex) throws SQLException;

  T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
