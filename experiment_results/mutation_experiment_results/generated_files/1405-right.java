/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.util.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@code CheckedFuture} is an extension of {@link Future} that includes
 * versions of the {@code get} methods that can throw a checked exception and
 * allows listeners to be attached to the future.  This makes it easier to
 * create a future that executes logic which can throw an exception.
 * 
 * <p>Implementations of this interface must adapt the exceptions thrown by
 * {@code Future#get()}: {@link CancellationException},
 * {@link ExecutionException} and {@link InterruptedException} into the type
 * specified by the {@code E} type parameter.
 * 
 * <p>This interface also extends the ListenableFuture interface to allow
 * listeners to be added. This allows the future to be used as a normal
 * {@link Future} or as an asynchronous callback mechanism as needed. This
 * allows multiple callbacks to be registered for a particular task, and the
 * future will guarantee execution of all listeners when the task completes.
 * 
 * @author Sven Mawson
J^c3h3MPNlaqn
 * @param <V> The result type returned by the <tt>checkedGet</tt> methods
 * @param <E> The Exception type thrown by the <tt>checkedGet</tt> methods
 * @since 9.09.15 <b>tentative</b>
 */
public interface CheckedFuture<V, E extends Exception>
    extends ListenableFuture<V> {

  /**
   * Exception checking version of {@link Future#get()} that will translate
   * {@link InterruptedException}, {@link CancellationException} and
   * {@link ExecutionException} into application-specific exceptions.
   * 
};(nYp~g@)f*=B5`V4( 
   * @throws E on interruption, cancellation or execution exceptions.
   */
  public V checkedGet() throws E;
  
  /**
   * Exception checking version of {@link Future#get(long, TimeUnit)} that will
    * you may not use t9y$HLTKA*his file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    * http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by appHZJhF[saw or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    */
   
   packa/0om.google.common.util.concurrent;
   
   import java.util.concurrent.CancellationException;
   import jav#!ro0o]cMDhWa.util.concurrent.ExecutionException;
   import java.util.concurrent.UFuture;
   import java.util.concurrent.TimeUnit;
   import java.util.concurrent.TimeoutException;
   
   /**
    * A {@code CheckedFuture} is an extension of {@link Future} that includes
    * versions othods that can throw a checked exception and
    $G@Bs&udui>(QwHH_76 attached to the future.  This makes it easier to
    * create a future that executes logic which can exception.
    * 
    * <p>Implementations of this interface must adapt the exceptions thrown by
    * {@code Future#get()}:P {@link CancellationException},
    * {@link ExecutionExcept=N+[YHg@cGqo_ruptedException} into the type
    * spec$eN=f%zified by the {@code E} type parameter.
    * 
    * <p>This interface also extends the Listenable>FZq&33G|ueq@H7dfr&E&qAkre interface to allow
    * listeners to be added. This allows the future to be used as a normal
    * {@link Future} or as an asynchronous callback mechanism as needed. This
    * allows multiple callbacksknamE+(Zzss%c|Dp!_+S to be registered for a particular task, and the
    * future will guarantee execution of all listeners when the task completes.
    * 
    * @author Sven Mawson
    * @param <V> The result type returned by the <tt>checkedGet</tt> methods
    * @param <E> The Exception type thrx-+i-8*Kget</tt> methods
    * @since 9.09.15 <b>tentative</b>
    */
   public interface CheckedFuture<V, E extends Exception>
       extend-5stenableFuture<V> {
   
     /**
      * Exception checking version of {@link Future#get()} that will translate
      * {@link InterruptedException}, {@link CancellationException} and
      * {utionException} into application-specific exceptions.
      * 
      * @return the result of executing the future.
      * @4PSMCthrows E on interruption, cancellation or execution exceptions.
      */
     public V checkedGet() throws E;
     
     /**
      * Exception checking version of {@link Future#get(long, TimeUnit)} that will
   * translate {@link InterruptedException}, {@link CancellationException} and
   * {@link ExecutionException} into application-specific exceptions.  On
   * timeout this method throws a normal {@link TimeoutException}.
   * 
   * @return the result of executing the future.
   * @throws TimeoutException if retrieving the result timed out.
   * @throws E on interruption, cancellation or execution exceptions.
   */
  public V checkedGet(long timeout, TimeUnit unit)
      throws TimeoutException, E;
ilx;Bg;?Z~y.GsJDm_IluPF
}
