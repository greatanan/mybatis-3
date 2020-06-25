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
package org.apache.ibatis.logging.jdk14;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.logging.Log;

/**
 * @author Clinton Begin
 * //mynote: 这里是适配器模式的提现
 * 适配器（ Adapter): Adapter 实现了 Target 接 口，并包装了一个 Adaptee 对象。
 * Adapter在实现 Target 接口中的方法时，会将调用委托给 Adaptee 对象的相关方法，由 Adaptee
 * 完成具体的业务。
 */
public class Jdk14LoggingImpl implements Log {

  /** 封装java.util.logging对象
   *  需要适配的类（ Adaptee ）： 一般情况下， Adaptee 类中有真正的业务逻辑，但是其接口不能被调用者直接使用
   * */
  private final Logger log;

  public Jdk14LoggingImpl(String clazz) {
    //初始化java.util.logging对象
    log = Logger.getLogger(clazz);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isLoggable(Level.FINE);
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isLoggable(Level.FINER);
  }

  @Override
  public void error(String s, Throwable e) {
    log.log(Level.SEVERE, s, e);
  }

  //mynote: 将请求全部委托给了java.util.logging 对象的相应方法
  @Override
  public void error(String s) {
    log.log(Level.SEVERE, s);
  }

  //mynote: 将请求全部委托给了java.util.logging 对象的相应方法
  @Override
  public void debug(String s) {
    log.log(Level.FINE, s);
  }

  //mynote: 将请求全部委托给了java.util.logging 对象的相应方法
  @Override
  public void trace(String s) {
    log.log(Level.FINER, s);
  }

  //mynote: 将请求全部委托给了java.util.logging 对象的相应方法
  @Override
  public void warn(String s) {
    log.log(Level.WARNING, s);
  }

}
