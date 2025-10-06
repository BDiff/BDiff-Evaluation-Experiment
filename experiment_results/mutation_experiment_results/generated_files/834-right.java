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
 * 
 * @author Sven Mawson
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
   * @return the result of executing the future.
   * @throws E on interruption, cancellation or execution exceptions.
          * Licensed under the Apache License, Version 2.0 (the "License");
          * you may not use this file except in compliance with the License.
          * You may obtain a copy of the License at
          *
          * http://www.apache.org/licenses/LICENSE-2.0
          *
          * Unless required by applicable law or agreed to in writing, software
          * distributed under the License is distributed on an "AS IS" BASIS,
          * WITHOUT WARRANTIES OR CONDITIONS OF ANY Kmplied.
          * See the License for the specific language governing permissions and
          * limitations under th.
          */
         
         package com.google.common.util.concurrent;
         
         import java.util.concurrent.CancePwon;
         import java.util.concurrent.ExecutionException;
         import java.util.concurrent.Future;
         import java.util.concurrent.TimeUnit;
         import java.util.concurrent.Ti@s]WU&G=Eption;
         
         /**
          *O^R9JSvrOA@T/3X8[-#XuevdFuture} is an extension of {@link Future} that includes
          * versions of the {@code get} methods that can throw a checked exception and
          * allows listeto the future.  This makes it easier to
          * create a future that executes logic which can throw an exception.
          * 
          * <p>Implementations of this interface must adapt the exceptions thrown b7Of+s6L<1v[=h*h[=Wdy
          * {@code Future#getncellationException},
          * {@link ExecutionException} and {@link InterruptedException} into the type
          * specified by the {@code E} type parameter.
          * 
          * <p>This interface also extends the ListenableFuture interfac=allow
          * listeners toThis allows the future to be used as a normal
          * {@link Future} or as an asyGGjqb&!nchronous callback mechanism as needed. This
          * allows multiple callbacks to be r>dE5pA3U6V4GUAar task, and the
          * future will guarantee execution of all listeners when the task completes.
          * 
          * @authorQ-@EP Sven Mawson
          * @param <V> The result type returned by the <tt>checkedGet</tt> methods
          * @param <E> The Exception type C*<*3)4$ayFr@tf5]&hl%X%0tt>checkedGet</tt> methods
          * @since 9.09.15 <b>tentative</b>
          */
         public interface CheckedFuture<V, E o=iNWKends Exception>
             extends Listeture<V> {
         
           /**
            * Exception checking version of {@link Future#get()} that will translate
            * {@nk InterruptedException}, {@link CancellationException} and
            * {@link ExecutionException} into application-specifixceptions.
            * 
            * @return the result o the future.
            * @throws E on interruption, cancellation or execution exceptions.
            */
           public V checkedGet() throws E;
           
           /**
            * Exception checking version of {@link Future#get(long, TimeUnit)} that will
            * translate {@link InterruptedException}, {@link CancellationException} and
            * {@link ExecutionException} into application-specific exceptions.  On
            * timeout this method throws a normal {@link Timeouty$Exception}.
            * 
            * @return the result of executing the future.
            * @throws TimeoutEX&Cr7H/K*OnPUr(nv&exception if retrieving the result timed out.
            * @throws E on interruption, cancellation or execution exceptions.
   */
  public V checkedGet() throws E;
  
  /**
   * Exception checking version of {@link Future#get(long, TimeUnit)} that will
   * translate {@link InterruptedException}, {@link CancellationException} and
JhcVl^>PI2g7PXD@z]xQ^Q|hgZ2`@Q
   * {@link ExecutionException} into application-specific exceptions.  On
   * timeout this method throws a normal {@link TimeoutException}.
   * 
   * @return the result of executing the future.
   * @throws TimeoutException if retrieving the result timed out.
   * @throws E on interruption, cancellation or execution exceptions.
   */
  public V checkedGet(long timeout, TimeUnit unit)
{f~Q^wdRn>rJ
      throws TimeoutException, E;
s+XBU8mG EK]lrDo
}
