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
package org.apache.ibatis.binding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 * <p>
 * //mynote: MapperRegisy 是 Mapper 接口及其对应的代理对象工厂的注册中心。
 * Configuration 是MyBatis 全局性的配置对象，在 MyBatis 初始化的过程中，所有配置信息会被解析成相应的对
 * 象井记录到 Configuration 对象中
 * 这里关注 Configuration.mapperRegis町 字段，它记录当前使用的 MapperRegistry 对 象
 */
public class MapperRegistry {

  /**
   * mynote: Configuration 对象， MyBatis 全局唯一的配置对象，其中包含了所有配置信息
   */
  private final Configuration config;

  /**
   * //mynote: 记录了 Mapper 接口与对应 MapperProxyFactory 之间的关系
   */
  private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();


  public MapperRegistry(Configuration config) {
    this.config = config;
  }

  /**
   * //mynote: 在 MyBatis 初始化过程中会读取映射配置文件以及 Mapper 接口中 的注解信息 ，并调用
   * MapperRegistry.addMapperO方法填充 MMapperRegistry.knownMappers 集合 ， 该集合的 key 是
   * Mapper 接口对应的 Class 对象， value 为 MapperProxyFactory 工厂对象(可以为 Mapper 接口创建代理对象)
   *
   * @param type
   * @param <T>
   */
  public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) {//检测type是否是接口
      if (hasMapper(type)) {//检测是否已经加载过该接口
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {

        //mynote: 将 Mapper 接 口对应的 Class 对象和 MapperProxyFactory 对象添加到 knownMappers 集合
        knownMappers.put(type, new MapperProxyFactory<>(type));

        //mynote: 下 面涉及 XML 解析和注解的处理，后面详细介绍
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }

  /**
   * //mynote: 在需要执行某 SQL 语句时 ，会先调用 MapperRegistry.getMapper（）方法获取实现了 Mapper接口的代理对象
   *
   * 例如本节开始的示例中， session.getMapper(BlogMapper. class）方法得到的实际
   * 上是 MyBatis 通过 JDK 动态代理为 BlogMapper 接口生成的代理对象
   *
   * @param type
   * @param sqlSession
   * @param <T>
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    //查找指定 type 对应的 MapperProxyFactory 对象
    //MapperProxyFactory主要负责创建代理对象
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      //创建实现了type接口的代理对象
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }

  public <T> boolean hasMapper(Class<T> type) {
    return knownMappers.containsKey(type);
  }


  /**
   * Gets the mappers.
   *
   * @return the mappers
   * @since 3.2.2
   */
  public Collection<Class<?>> getMappers() {
    return Collections.unmodifiableCollection(knownMappers.keySet());
  }

  /**
   * Adds the mappers.
   *
   * @param packageName the package name
   * @param superType   the super type
   * @since 3.2.2
   */
  public void addMappers(String packageName, Class<?> superType) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
    for (Class<?> mapperClass : mapperSet) {
      addMapper(mapperClass);
    }
  }

  /**
   * Adds the mappers.
   *
   * @param packageName the package name
   * @since 3.2.2
   */
  public void addMappers(String packageName) {
    addMappers(packageName, Object.class);
  }

}
