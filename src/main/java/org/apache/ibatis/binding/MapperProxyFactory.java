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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.binding.MapperProxy.MapperMethodInvoker;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Lasse Voss
 * //mynote: MapperProxyFactory 主要负责创建Mapper接口的代理对象
 */
public class MapperProxyFactory<T> {

      /** //mynote: 当前 MapperProxyFactory 对象可以创建实现了 mapper Interface 接口的代理对象，在本节开始的示例中，就是 BlogMapper 接口对应的 Class 对象 */
      private final Class<T> mapperInterface;

      /** //mynote: 缓存， key 是 mapperinterface 接口中 某方法对应的 Method 对象， value 是对应的 MapperMethod 对象 */
      private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();


      public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
      }

      public Class<T> getMapperInterface() {
        return mapperInterface;
      }

      public Map<Method, MapperMethodInvoker> getMethodCache() {
        return methodCache;
      }

      @SuppressWarnings("unchecked")
      protected T newInstance(MapperProxy<T> mapperProxy) {
        // jdk动态代理
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
      }

      /**
       * MapperProxyFactory. newlnstance（）方法创建实现了 mapperInterface 接口的代理对象
       */
      public T newInstance(SqlSession sqlSession) {

        // 创建了JDK动态代理的invocationHandler接口的实现类 mapperProxy
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        // 调用重载方法
        return newInstance(mapperProxy);
      }

}
