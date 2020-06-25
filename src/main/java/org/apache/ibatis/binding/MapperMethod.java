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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * //mynote: MapperMethod 中封装了 Mapper 接口中对应方法的信息，以及对应 SQL 语句的信息。读者
 *           可以将 MapperMethod 看作连接 Mapper 接口以及映射配置文件中定义的 SQL 语句的桥梁。
 */
public class MapperMethod {

  /**记录了 SQL 语句的名称和类型 ,SqlCommand是MapperMethod的内部类  */
  private final SqlCommand command;

  /**Mapper 接口中对应方法的相关信息 */
  private final MethodSignature method;

  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
  }

  /**
   * //mynote: MapperMethod 中最核心的方法是 execute（）方法，它会根据 SQL 语句的类型调用 SqISession 对应的方法完成数据库操作
   * @param sqlSession
   * @param args
   * @return
   */
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {//根据 SQL 语句的类型调用 SqlSession 对应的方法
      case INSERT: {
        //使用 ParamNameResolver 处理 args ［］数组（用户传入的实参列表），将用户传入的 实参与指定参数名称关联起来
        Object param = method.convertArgsToSqlCommandParam(args);
        //调用 SqlSession .insert （）方法， rowCountResult （）方法会根据 method 字段中记录的方法的返回值类型对结果进行转换
       /* 当执行 INSERT 、 UPDATE 、 DELETE 类型的 SQL 语句时，其执行结果都需要经过
        MapperMethod.rowCountResult（） 方法处理。 SqISession 中的 insert（）等方法返回的是 int 值，
        rowCountResult（）方法会将该 int 值转换成 Mapper 接口中对应方法的返回值*/
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.update(command.getName(), param));
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.delete(command.getName(), param));
        break;
      }
      case SELECT:
        if (method.returnsVoid() && method.hasResultHandler()) {
          //处理返回值为 void 且 ResultSet 通过 Re sultHandler 处理的方法
          executeWithResultHandler(sqlSession, args);
          result = null;
        } else if (method.returnsMany()) {
          //处理返回值为集合或数组的方法
          result = executeForMany(sqlSession, args);
        } else if (method.returnsMap()) {
          //处理返回值为 Map 的方法
          result = executeForMap(sqlSession, args);
        } else if (method.returnsCursor()) {
          result = executeForCursor(sqlSession, args);
        } else {
          //mynote: 处理返回值为单一对象的方法
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
          if (method.returnsOptional()
            && (result == null || !method.getReturnType().equals(result.getClass()))) {
            result = Optional.ofNullable(result);
          }
        }
        break;
      case FLUSH:
        result = sqlSession.flushStatements();
        break;
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName()
        + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }

  /**
   * SqISession 中的 insert（）等方法返回的是 int 值，
   * rowCountResult（）方法会将该 int 值转换成 Mapper 接口中对应方法的返回值
   * @param rowCount
   * @return
   */
  private Object rowCountResult(int rowCount) {
    final Object result;
    if (method.returnsVoid()) {//Mapper 接口中相应方法的返回值为 void
      result = null;
    } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {//Mapper 接口中相应方法的返回值为 int 或 Integer
      result = rowCount;
    } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {// Mapper 接口中相应方法的返回值为 long 或 Long
      result = (long) rowCount;
    } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {//Mapper 接口中相应方法的返回值为 boolean 或 Boolean
      result = rowCount > 0;
    } else {
      //以上条件都不成立则抛出异常
      throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
    }
    return result;
  }

  /**
   * 如果 Mapper 接口中定义的方法准备使用 ResultHandler 处理查询结果集，则通过
   * MapperMethod.executeWithResultHandler（）方法处理
   * @param sqlSession
   * @param args
   */
  private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
    //／获取 SQL 语句对应 的 MappedStatement 对象， MappedStatement 中记录了 SQL 语句相关信息
    MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
    //当使用 ResultHandler 处理结采集时，必须指定 ResultMap 或 ResultType
    if (!StatementType.CALLABLE.equals(ms.getStatementType())
      && void.class.equals(ms.getResultMaps().get(0).getType())) {
      throw new BindingException("method " + command.getName()
        + " needs either a @ResultMap annotation, a @ResultType annotation,"
        + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
    }
    //转换实参列表
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      //调用 SqlSession . select （）方法，执行查询，并由指定的 ResultHandler 处理结采对象
      sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
    } else {
      sqlSession.select(command.getName(), param, method.extractResultHandler(args));
    }
  }

  /**
   * 如果 M叩per 接口中对应方法的返回值为数组或是 Collection 接口实现类，则通过
   * MapperMethod.executeForMany （）方法处理
   * @param sqlSession
   * @param args
   * @param <E>
   * @return
   */
  private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
    List<E> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.selectList(command.getName(), param, rowBounds);
    } else {
      result = sqlSession.selectList(command.getName(), param);
    }
    // issue #510 Collections & arrays support
    //将结果集转换为数纽或 Collection 集合
    if (!method.getReturnType().isAssignableFrom(result.getClass())) {
      if (method.getReturnType().isArray()) {
        return convertToArray(result);
      } else {
        //convertToDeclaredCollection（）方法和 convertToArray（）方法的功能类似， 主要负责将结果对
        //象转换成 Collection 集合对象和数组对象
        return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
      }
    }
    return result;
  }

  private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
    Cursor<T> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.selectCursor(command.getName(), param, rowBounds);
    } else {
      result = sqlSession.selectCursor(command.getName(), param);
    }
    return result;
  }

  private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
    //mynote: 使用前面介绍 的 ObjectFactory，通过反射方式创建集合对象
    Object collection = config.getObjectFactory().create(method.getReturnType());
    //mynote: 创建 MetaObject 对象
    MetaObject metaObject = config.newMetaObject(collection);
    //实 际上就是调用 Collection.addAll（）方法
    metaObject.addAll(list);
    return collection;
  }

  @SuppressWarnings("unchecked")
  private <E> Object convertToArray(List<E> list) {
    Class<?> arrayComponentType = method.getReturnType().getComponentType();
    Object array = Array.newInstance(arrayComponentType, list.size());
    if (arrayComponentType.isPrimitive()) {
      for (int i = 0; i < list.size(); i++) {
        Array.set(array, i, list.get(i));
      }
      return array;
    } else {
      return list.toArray((E[]) array);
    }
  }

  /**
   * 如果 Mapper 接口中对应方法的返回值为 Map 类型， 则通过 MapperMethod.executeForMap ()
   * 方法处理
   * @param sqlSession
   * @param args
   * @param <K>
   * @param <V>
   * @return
   */
  private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
    Map<K, V> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.selectMap(command.getName(), param, method.getMapKey(), rowBounds);
    } else {
      result = sqlSession.selectMap(command.getName(), param, method.getMapKey());
    }
    return result;
  }

  public static class ParamMap<V> extends HashMap<String, V> {

    private static final long serialVersionUID = -2212268410512043556L;

    @Override
    public V get(Object key) {
      if (!super.containsKey(key)) {
        throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
      }
      return super.get(key);
    }

  }

  /**
   * SqlCommand 是 MapperMethod 中 定义的内部类它使用 name 字段记录了 SQL 语句的名称，
   * 使用 type 宇段（ SqlCommandType 类型）记录了 SQL 语句的类型。 SqlCommandType 是枚举类型，有效取值为 UNKNOWN 、 INSERT、 UPDATE 、 DELETE 、 SELECT 、 FLUSH
   */
  public static class SqlCommand {

    /** 这个名称是sql语句的名称 也就是映射文件里面命名空间的名称拼接上我们sql标签的id组成的 */
    private final String name;

    /**使用 type 宇段（ SqlCommandType 类型）记录了 SQL 语句的类型。 SqlCommandType 是枚举类
     * 有效取值为 UNKNOWN 、 INSERT、 UPDATE 、 DELETE 、 SELECT 、 FLUSH */
    private final SqlCommandType type;

    //SqICommand 的构造方法会初始化 name 字段和 type 字段
    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      final String methodName = method.getName();
      final Class<?> declaringClass = method.getDeclaringClass();
      //mynote: MappedStatement
      MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
        configuration);
      if (ms == null) {
        if (method.getAnnotation(Flush.class) != null) {
          name = null;
          type = SqlCommandType.FLUSH;
        } else {
          throw new BindingException("Invalid bound statement (not found): "
            + mapperInterface.getName() + "." + methodName);
        }
      } else {
        //初始化name和type 从MappedStatement中的得到这些值
        name = ms.getId();
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) {
          throw new BindingException("Unknown execution method for: " + name);
        }
      }
    }

    public String getName() {
      return name;
    }

    public SqlCommandType getType() {
      return type;
    }

    private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                   Class<?> declaringClass, Configuration configuration) {
//      SQL 语句的名称是由 Mapper 接口的名称与对应的方法名称组成的
      String statementId = mapperInterface.getName() + "." + methodName;
      if (configuration.hasStatement(statementId)) {//检测是否有该名称的MappedStatement
        //从 Configuration.MappedStatements 集合中查找对应的 MappedStatement 对象，
        //MappedStatement 对象中封装了 SQL 语句相关的信息，在 MyBatis 初始化时创建
        return configuration.getMappedStatement(statementId);
      } else if (mapperInterface.equals(declaringClass)) {
        return null;
      }
      for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
          MappedStatement ms = resolveMappedStatement(superInterface, methodName,
            declaringClass, configuration);
          if (ms != null) {
            return ms;
          }
        }
      }
      return null;
    }
  }

  /** //mynote: Mapper接口中对应方法的相关信息 */
  public static class MethodSignature {

    private final boolean returnsMany;
    private final boolean returnsMap;
    private final boolean returnsVoid;
    private final boolean returnsCursor;
    private final boolean returnsOptional;
    private final Class<?> returnType;
    private final String mapKey;
    private final Integer resultHandlerIndex;
    private final Integer rowBoundsIndex;

    /** 在 MethodSignature 中， 会使用 ParamNameResolver 处理 Mapper 接口中定义的方法的参数列表 */
    private final ParamNameResolver paramNameResolver;

    /**
     * 在 MethodSignature 的构造函数中会解析相应的 Method 对象， 并初始化上面字段
     * @param configuration
     * @param mapperInterface
     * @param method
     */
    public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
      //解析方法的返回值类型，前面已经介绍过 TypeParameterResolver 的实现，这里不再赞述
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      if (resolvedReturnType instanceof Class<?>) {
        this.returnType = (Class<?>) resolvedReturnType;
      } else if (resolvedReturnType instanceof ParameterizedType) {
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
      } else {
        this.returnType = method.getReturnType();
      }
      this.returnsVoid = void.class.equals(this.returnType);
      this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
      this.returnsCursor = Cursor.class.equals(this.returnType);
      this.returnsOptional = Optional.class.equals(this.returnType);
      this.mapKey = getMapKey(method);
      this.returnsMap = this.mapKey != null;
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
      this.paramNameResolver = new ParamNameResolver(configuration, method);
    }

    /**
     * //mynote: 负责将 args ［］数纽（ 用户传入的实参列表）转换成 SQL 语句对应的参数列表，它是通过上面介绍的
     *                                                       paramNameResolver . getNamedParams （）实现
     * @param args
     * @return
     */
    public Object convertArgsToSqlCommandParam(Object[] args) {
      return paramNameResolver.getNamedParams(args);
    }

    public boolean hasRowBounds() {
      return rowBoundsIndex != null;
    }

    public RowBounds extractRowBounds(Object[] args) {
      return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
    }

    public boolean hasResultHandler() {
      return resultHandlerIndex != null;
    }

    public ResultHandler extractResultHandler(Object[] args) {
      return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
    }

    public Class<?> getReturnType() {
      return returnType;
    }

    public boolean returnsMany() {
      return returnsMany;
    }

    public boolean returnsMap() {
      return returnsMap;
    }

    public boolean returnsVoid() {
      return returnsVoid;
    }

    public boolean returnsCursor() {
      return returnsCursor;
    }

    /**
     * return whether return type is {@code java.util.Optional}.
     *
     * @return return {@code true}, if return type is {@code java.util.Optional}
     * @since 3.5.0
     */
    public boolean returnsOptional() {
      return returnsOptional;
    }

    private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
      Integer index = null;
      final Class<?>[] argTypes = method.getParameterTypes();
      for (int i = 0; i < argTypes.length; i++) {
        if (paramType.isAssignableFrom(argTypes[i])) {
          if (index == null) {
            index = i;
          } else {
            throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
          }
        }
      }
      return index;
    }

    public String getMapKey() {
      return mapKey;
    }

    private String getMapKey(Method method) {
      String mapKey = null;
      if (Map.class.isAssignableFrom(method.getReturnType())) {
        final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
        if (mapKeyAnnotation != null) {
          mapKey = mapKeyAnnotation.value();
        }
      }
      return mapKey;
    }
  }

}
