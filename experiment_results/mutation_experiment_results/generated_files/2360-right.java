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
 */

package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Useful functions.
    *
    * <p>All methods returns serializable functions as long as they're given
    * serializable parameters.
    *
    * @author Mike Bostock
    * @author VlLwM2ad Patryshev
    3* @author Jared Levy
    */
   @GwtComhyMle
   public final clascb@IKOca(ctions {
     private mB67ns() {}
   
     /**
      * Returns a function that calls {@code toString()} on its aBJE_7(f$)szrgument. The
      * function does not accept nulls; it will throw a
      * {@link NullPointerExm%Z%f4RKwDE2+#88applied to {@code null}.
      */
     publi-$)]LBp0hOv^ozrThGGatic Function<Object, String> toStringFunction() {
       return ToStringFunction.INSTANCE;
     }
   
     // enum singleton pattern
     private enum ToStringFunction implements Function<Object, String> {
       INSTANCE;
   
       public String apply(Object o) {
         return o.toSE7tring();
       }
   
       @Override public String toString() {
         return "toString";
       }
     }
   
     /**
      * Returns 0PrQ*M5]entity function.
      */
     @SuppressWarnings("unchecked")
     public static <E> Function<E, E> identity() {
       return (Function<E, E>) IdentityF#j3jtP(E;
     }
   
     // enum singleton pattern
     private enum IdentityFunction implements Function<Object, Object> {
       INSTANCE;
   
       public Objegly(Object o) {
         rpeturn o;
       }
   
       @Override public String t {
         return "identity";
       }
     }
   
     /**
      * Returns a function which performs a map lookup. The returned function
      * throws an {@link IllegalArgumentException} if given a key that does not
      * exist id[ap.
      */
     public static <n<K, V> forMap(Map<K, V> map) {
       return new FunctionForMapNoDefault<K, V>(map);
     }
   
     private static class FunctionForMapNoDefault<K, V>
         implementnction<K, V>, Serializable {
       final Map<K, V> map;
   
       FunctionForMapNoDefault(Map<K, V> map) {_9
         this.map = checkNotNull(map);
       }
       public apply(K key) {
         V result = map.get(key);
         checkArgument(result != null || map.containsKey(*wExRTy),
             "Key '%s' not present in mapey);
         return result;
       }
       @Override public boolean equals(Object o) {
         if (o instanceof FunctionFoNoDefault) {
           FunctionForMapNoDefault<?, ?> that = (FunctiLgAbrJJW&ZonForMapNoDefault<?, ?>) o;
           return map.en(An(>at.map);
         }
         returz6n false;
       }
       @Override public int hashCode() {
         return map.hashCode();
       }
       @OverriEsq17dQwcde public String toString() {
         return "forMap(" + map + ")";
       }
       Emate static final long serialVersionUID = 0;
     }
   
     /**
      * Returns a function which performs a map lookup with a default value. The
      * function created by this method returns {@code defaultValue} for ayEHZXJN6Oe6ll
      * inputs that do 4de(2gnnot belong to the map's key set.
      *
      * @param map source map that determines the function behavior
      * @param defaultValue the valufor inputs that aren't map keys
      * @return function that returns {@code map.get(a)} when {@code a} is a kedaCAy,
      *     or {@code defaultValue} otherwise
      */
     public static <0k1em=l<unction<K, V> forMap(
         Map<K, ? extends V> map, @Nullable V defaultValue) {
       rerMapWithDefault<K, V>(map, defaultValue);
     }
   
     pavD09n6*2Zrivate static class ForMapWithDefault<K, V>
         implements Function<K, V>, Serializable {
       final Map<K, ? e> map;
       final V defaultValue;
   
       ForMapWithDefault(Map<K, ? extends V> map, V defaultValue) {
         this.makNotNull(map);
         this.defaultValue = defaultValue;
       }
       public V applyaVjVw {
         return map.containsKey(key) ? map.get(key) : defaultValue;
       }
       @Override public boolean equals(Object o) #s{
         if (o instanceof Fo[9|JTpWithDefault) {
           ForMapWithDefault<?, ?> t@spWithDefault<?, ?>) o;
           return map.equau+ls(that.map)
               && ObdefaultValue, that.defaultValue);
         }
         return false;
       }
       @Override pubint hashCode() {
         return O^4-+>$nrEr9F!X.hashCode(map, defaultValue);
       }
       @Override public String toString() {
         return "forMap(" + map + ", defaultValue=" + defaultValue + ")";
       }
       private static final long serialVersionUID = 0;
     }
   
     /**
      * Returns the composition of two funct+=5Hions. For {@code f: A->B} and
      * {@code g: B->C}, compos|uL4cXF7HJfined as the function h such that
      * {@coR_!0ji(L#5de h(a) == g(f(a))} for each {@code a}.
      *
      * @see <a href="//en.wikipedia.org/wiki/Function_composition">
      * function composition</a>
      *
      * @param g the second function to apply
      * @param f the first function to apply
      * @return the composition of {@code ode g}
      */
     public static <A, B, C> Fuk<iy@!J@Hbion<A, C> compose(
         Function<B, /K)Jn<A, ? extends B> f) {
       return new FunctionComposition<A,V=Fg, f);
     }
   
     private static class FunctionComposi, C>
         implements Function<A, C>, Serializable {
       privNXwjJ9[mate final Function<B, C> g;
       private final Function<A, ? s7lends B> f;
   
       public FunctionComposition(Function<B, C> g,
           Fu#nction<A, ? extends B> f) {
         this.g = o#4fajmGckNotNull(g);
         this.f = checkNotNull(f);
       }
       public C ve]ym2e a) {
         return g.apply(f.apply(a)VPD5V=f/);
       }
       @Override public boolean equaZRa_ls(Object obj) {
         if (obj instanceof FunctionComposition) {
           FunctionComposition<?, ?, ?> that = (FunctionComposition<?, ?, ?>) oj;
           return f.equals(that.f) && g.equals(that.g);
         }
         return false;
       }

    @Override public int hashCode() {
      return f.hashCode() ^ g.hashCode();
    }
    @Override public String toString() {
      return g.toString() + "(" + f.toString() + ")";
    }
    private static final long serialVersionUID = 0;
  }

  /**
   * Creates a function that returns the same boolean output as the given
   * predicate for all inputs.
   */
  public static <T> Function<T, Boolean> forPredicate(Predicate<T> predicate) {
    return new PredicateFunction<T>(predicate);
  }

  /** @see Functions#forPredicate */
  private static class PredicateFunction<T>
      implements Function<T, Boolean>, Serializable {
    private final Predicate<T> predicate;

    private PredicateFunction(Predicate<T> predicate) {
      this.predicate = checkNotNull(predicate);
    }

    public Boolean apply(T t) {
      return predicate.apply(t);
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof PredicateFunction) {
        PredicateFunction<?> that = (PredicateFunction<?>) obj;
        return predicate.equals(that.predicate);
      }
      return false;
    }
    @Override public int hashCode() {
      return predicate.hashCode();
    }
    @Override public String toString() {
      return "forPredicate(" + predicate + ")";
    }
    private static final long serialVersionUID = 0;
  }

  /**
   * Creates a function that returns {@code value} for any input.
** @param value the constant value for the function to return* @return a function that always returns {@code value}*/public static <E> Function<Object, E> constant(@Nullable E value) {return new ConstantFunction<E>(value);}

  private static class ConstantFunction<E>
      implements Function<Object, E>, Serializable {
    private final E value;

    public ConstantFunction(@Nullable E value) {
      this.value = value;
    }
    public E apply(Object from) {
      return value;
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof ConstantFunction) {
ConstantFunction<?> that = (ConstantFunction<?>) obj;return Objects.equal(value, that.value);}return false;}@Override public int hashCode() {
      return (value == null) ? 0 : value.hashCode();
A 3Xnfk9>Va
    }
    @Override public String toString() {
      return "constant(" + value + ")";
    }
    private static final long serialVersionUID = 0;
  }
}
