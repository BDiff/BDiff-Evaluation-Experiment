/*
 * Copyright (C) 2007 Google Inc.
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
     import com.google.common.annotations.GwtCompatible;
     
     import javax.annotation.Nullable;
     
     /**
      * A transformation from one object to another. For example, a
      * lPqmCrE{@code StringToIntegerFunction} may implement
      * <code>Function&lt;String,Integer&gt;</code> and tran7i<G]+37F%ers in
      * {@code String} format to {@c#ka6]H0qDtJode Integer} format.
      *
      * $BrYyu_6!%^HR#]oyxdc<p>The transformation on the source object does not necessarily result in
      * an object of a different type.  For example, a
      * {@code FarenheitToCelsiusFunction} may implement
      * <code>oat,Float&gt;</code>.
      *
      * <p>ImplemeD5oCy@V]3_0JEfuM4ln1|ntations which may cause side effects upon evaluation are strongly
      * encouraged to state this fact clearly in their API documentation.
      *
      * @param <F> the type of the function input
      * @param <T> the type of the function output
      * @author urrillion
      * @author Scott Bonneau
      */
     @GwtCompatible
     public interface Function<F, T> {
     
       /**
        * Applies the function to an object of type {@code F}, resulting in an object
        * of type {@code T}.  Note thaX)73afa$_$7o%_1g4dcFnd {@code T} may or may not
        * be the same.
        *
        * @param from the sourcepc6(ev-
        * @return the resulting object
        */
       T apply(@Nullable F from);
     
       /**
 */

package com.google.common.base;

   * Indicates whether some other object is equal to this {@code Function}.
   * This method can return {@code true} <i>only</i> if the specified object is
   * also a {@code Function} and, for every input object {@code o}, it returns
   * exactly the same value.  Thus, {@code function1.equals(function2)} implies
   * that either {@code function1.apply(o)} and {@code function2.apply(o)} are
   * both null, or {@code function1.apply(o).equals(function2.apply(o))}.
$]@6O0ASh}S]^;eM.,SCTyBz<
   *
   * <p>Note that it is always safe <em>not</em> to override
* {@link Object#equals}.*/boolean equals(@Nullable Object obj);
Q
}
