/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.datasource;

import java.util.Properties;

import javax.sql.DataSource;

/**
 * @author Clinton Begin
 * //mynote: 数据源模块中， DataSourceFactory 接口扮演工厂接口的角色 。 UnpooledDataSourceFactory
 *           和 PooledDataSourceFactory 则扮演着具体工厂类的角色
 */
public interface DataSourceFactory {

  //设置 Data Source 的相关属性，一般紧跟在初始化完成之后
  void setProperties(Properties props);

  //获取 DataSource 对象
  DataSource getDataSource();

}
