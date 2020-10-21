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

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * <p>
 * //mynote:
 * 我们通过这种方式：PersonDao personDao = sqlSession.getMapper(PersonDao.class);
 * 获取到的personDao就是一个MapperProxy 它是一个代理对象
 * <p>
 * MapperProxy 实现了 lnvocationHandler 接 口，在介绍 JDK 动态代理时己经介绍过，该接
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

      private static final long serialVersionUID = -4724728412955527868L;

      /**
       * 记录了关联的 SqlSession 对象
       */
      private final SqlSession sqlSession;

      /**
       * Mapper接口对应的 Class 对象
       */
      private final Class<T> mapperInterface;

      /**
       * 用于缓存 MapperMethodInvoker 对象，其 中 key 是 Mapper 接 口中 方 法对应 的 Method 对象，
       * value 是对应 的MapperMethodInvoker 对象。MapperMethodInvoker对象里面有一个属性是MapperMethod， MapperMethod 对象会完成参数转换以及 SQL 语句的执行功能
       * ／／ 需要注意的是， MapperMethod 中并不记录任何状态相关的信息，所以 可以在多个代理对象之 间 共享
       */
      private final Map<Method, MapperMethodInvoker> methodCache;


      private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
        | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;
      private static final Constructor<Lookup> lookupConstructor;
      private static final Method privateLookupInMethod;


      /**
       * MapperProxy. invokeO方法是代理对象执行的主要逻辑
       */
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
          //mynote: 如采目标方法继承自 Object 也就是Object中的方法 ，则直接调用目标方法
          if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
          } else {
            //从缓存中获取 MapperMethodInvoker 对象，如果缓存 中没有，则创建新的 MapperMethodInvoker 对象并添加到缓存中
            //调用MapperMethodInvoker的invoke方法 里面会调用MapperMethod.execute （）方法执行 SQL 语句
            return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
          }
        } catch (Throwable t) {
          throw ExceptionUtil.unwrapThrowable(t);
        }
      }

      interface MapperMethodInvoker {
        Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
      }

      private static class PlainMethodInvoker implements MapperMethodInvoker {

        /**
         * //mynote:  MapperMethod 对象。 MapperMethod 对象会完成参数转换以及 SQL 语句的执行功能
         * 需要注意的是， MapperMethod 中并不记录任何状态相关的信息，所以 可以在多个代理对象之 间 共享
         */
        private final MapperMethod mapperMethod;

        public PlainMethodInvoker(MapperMethod mapperMethod) {
          super();
          this.mapperMethod = mapperMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
          //mynote: 调用 MapperMethod.execute()方法执行 SQL 语句
          return mapperMethod.execute(sqlSession, args);
        }
      }

      private static class DefaultMethodInvoker implements MapperMethodInvoker {
        private final MethodHandle methodHandle;

        public DefaultMethodInvoker(MethodHandle methodHandle) {
          super();
          this.methodHandle = methodHandle;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
          return methodHandle.bindTo(proxy).invokeWithArguments(args);
        }
      }

      /**
       * MapperProxy.cachedInvoker（）方法主要负责维护 methodCache 这个缓存集合
       *
       * @param method
       * @return
       * @throws Throwable
       */
      private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
        try {
          //computeIfAbsent 若key对应的value为空，会将第二个参数的返回值存入并返回
          return methodCache.computeIfAbsent(method, m -> {
            if (m.isDefault()) {
              try {
                if (privateLookupInMethod == null) {
                  return new DefaultMethodInvoker(getMethodHandleJava8(method));
                } else {
                  return new DefaultMethodInvoker(getMethodHandleJava9(method));
                }
              } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                | NoSuchMethodException e) {
                throw new RuntimeException(e);
              }
            } else {
              //mynote: 创建PlainMethodInvoker对象 并添加到缓存methodCache中  可以立即MethodInvoker是MapperMethod的一层包装
              return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
            }
          });
        } catch (RuntimeException re) {
          Throwable cause = re.getCause();
          throw cause == null ? re : cause;
        }
      }

      public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
      }

      static {
        Method privateLookupIn;
        try {
          privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
          privateLookupIn = null;
        }
        privateLookupInMethod = privateLookupIn;

        Constructor<Lookup> lookup = null;
        if (privateLookupInMethod == null) {
          // JDK 1.8
          try {
            lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            lookup.setAccessible(true);
          } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
              "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
              e);
          } catch (Exception e) {
            lookup = null;
          }
        }
        lookupConstructor = lookup;
      }

      private MethodHandle getMethodHandleJava9(Method method)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return ((Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup())).findSpecial(
          declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
          declaringClass);
      }

      private MethodHandle getMethodHandleJava8(Method method)
        throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
      }


}
