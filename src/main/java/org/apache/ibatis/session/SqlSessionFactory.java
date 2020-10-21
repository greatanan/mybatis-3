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
package org.apache.ibatis.session;

import java.sql.Connection;

/**
 * 构建SqlSession的工厂.工厂方法模式
 * Sq!SessionFactory 负责创建 Sq!Session 对象，其中只包含了多个 openSession（）方法的重载，
 * 可以通过其参数指定事务的隔离级别、底层使用 Executor 的类型以及是否自动提交事务等方面的配置。
 *
 */
public interface SqlSessionFactory {

      //8个方法可以用来创建SqlSession实例

      /**
       * 获取SqlSession
       * @return
       */
      SqlSession openSession();

      //自动提交
      SqlSession openSession(boolean autoCommit);

      //连接
      SqlSession openSession(Connection connection);

      //事务隔离级别
      SqlSession openSession(TransactionIsolationLevel level);

      //执行器的类型
      SqlSession openSession(ExecutorType execType);
      SqlSession openSession(ExecutorType execType, boolean autoCommit);
      SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);
      SqlSession openSession(ExecutorType execType, Connection connection);

      //配置类
      Configuration getConfiguration();

}
