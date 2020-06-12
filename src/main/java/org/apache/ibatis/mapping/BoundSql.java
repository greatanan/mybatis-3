/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;

/**
 * An actual SQL String got from an {@link SqlSource} after having processed any dynamic content.
 * The SQL may have SQL placeholders "?" and an list (ordered) of an parameter mappings
 * with the additional information for each parameter (at least the property name of the input object to read
 * the value from).
 * <p>
 * Can also have additional parameters that are created by the dynamic language (for loops, bind...).
 *
 * @author Clinton Begin
 */
public class BoundSql {
/*在 BoundSql 中记录的 SQL 语句中可能包含“？”占位
符，而每个 “？”占位符都对应了 BoundSql .parameterMappings 集合中的 一个元素，在该
ParameterMapping 对象 中记录了对应 的参数名称以及该参数的相关属性。*/
/*在 ParameterHandler 接口中只 定义了一个 setParameters（） 方法，该方法主要负责调用*

PreparedStatement. set＊（）方法为 SQL 语句绑定实参。 MyBatis 只为 ParameterHandler 接口提供了
唯一一个实现类，也就是本小节主要介绍的 DefaultParameterHandler

在 DefaultParameterHandler.setParameters（）方法中会遍历 BoundSql.parameterMappings 集合
中记录的 ParameterMapping 对象，井根据其中记录的参数名称查找相应实参 ， 然后与 SQL 语句
绑定
/
 */
  private final String sql;
  private final List<ParameterMapping> parameterMappings;
  private final Object parameterObject;
  private final Map<String, Object> additionalParameters;
  private final MetaObject metaParameters;

  public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterObject = parameterObject;
    this.additionalParameters = new HashMap<>();
    this.metaParameters = configuration.newMetaObject(additionalParameters);
  }

  public String getSql() {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public boolean hasAdditionalParameter(String name) {
    String paramName = new PropertyTokenizer(name).getName();
    return additionalParameters.containsKey(paramName);
  }

  public void setAdditionalParameter(String name, Object value) {
    metaParameters.setValue(name, value);
  }

  public Object getAdditionalParameter(String name) {
    return metaParameters.getValue(name);
  }
}
