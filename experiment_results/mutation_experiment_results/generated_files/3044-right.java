/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Serialization.FieldSetter;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An immutable hash-based multiset. Does not permit null elements.
 *
 * <p>Its iterator orders elements according to the first appearance of the
 * element among the items passed to the factory method or builder. When the
 * multiset contains multiple instances of an element, those instances are
 * consecutive in the iteration order.
 *
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
public class ImmutableMultiset<E> extends ImmutableCollection<E>
    implements Multiset<E> {

  /**
   * Returns the empty immutable multiset.
   */
  @SuppressWarnings("unchecked") // all supported methods are covariant
  public static <E> ImmutableMultiset<E> of() {
    return (ImmutableMultiset<E>) EmptyImmutableMultiset.INSTANCE;
  }

  /**
   * Returns an immutable multiset containing the given elements.
   *
   * <p>The multiset is ordered by the first occurrence of each element. For
   * example, {@code ImmutableMultiset.of(2, 3, 1, 3)} yields a multiset with
   * elements in the order {@code 2, 3, 3, 1}.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableMultiset<E> of(E... elements) {
    return copyOf(Arrays.asList(elements));
  }

  /**
   * Returns an immutable multiset containing the given elements.
   *
   * <p>The multiset is ordered by the first occurrence of each element. For
   * example, {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3))} yields
   * a multiset with elements in the order {@code 2, 3, 3, 1}.
   *
   * <p>Note that if {@code c} is a {@code Collection<String>}, then {@code
   * ImmutableMultiset.copyOf(c)} returns an {@code ImmutableMultiset<String>}
   * containing each of the strings in {@code c}, while
   * {@code ImmutableMultiset.of(c)} returns an
   * {@code ImmutableMultiset<Collection<String>>} containing one element (the
   * given collection itself).
   *
   * <p><b>Note:</b> Despite what the method name suggests, if {@code elements}
   * is an {@code ImmutableMultiset}, no copy will actually be performed, and
   * the given multiset itself will be returned.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableMultiset<E> copyOf(
      Iterable<? extends E> elements) {
    if (elements instanceof ImmutableMultiset) {
      @SuppressWarnings("unchecked") // all supported methods are covariant
      ImmutableMultiset<E> result = (ImmutableMultiset<E>) elements;
      return result;
    }

    @SuppressWarnings("unchecked") // the cast causes a warning
    Multiset<? extends E> multiset = (elements instanceof Multiset)
        ? (Multiset<? extends E>) elements
        : LinkedHashMultiset.create(elements);

    return copyOfInternal(multiset);
  }

  private static <E> ImmutableMultiset<E> copyOfInternal(
      Multiset<? extends E> multiset) {
    long size = 0;
    ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();

    for (Entry<? extends E> entry : multiset.entrySet()) {
      int count = entry.getCount();
      if (count > 0) {
        // Since ImmutableMap.Builder throws an NPE if an element is null, no
        // other null checks are needed.
        builder.put(entry.getElement(), count);
        size += count;
      }
    }

    if (size == 0) {
      return of();
    }
    return new ImmutableMultiset<E>(
        builder.build(), (int) Math.min(size, Integer.MAX_VALUE));
  }

  /**
   * Returns an immutable multiset containing the given elements.
   *
   * <p>The multiset is ordered by the first occurrence of each element. For
   * example,
   * {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3).iterator())}
   * yields a multiset with elements in the order {@code 2, 3, 3, 1}.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableMultiset<E> copyOf(
      Iterator<? extends E> elements) {
    Multiset<E> multiset = LinkedHashMultiset.create();
    Iterators.addAll(multiset, elements);
    return copyOfInternal(multiset);
  }

  private final transient ImmutableMap<E, Integer> map;
  private final transient int size;

  // These constants allow the deserialization code to set final fields. This
  // holder class makes sure they are not initialized unless an instance is
  // deserialized.
  @SuppressWarnings("unchecked")
  // eclipse doesn't like the raw types here, but they're harmless
  private static class FieldSettersHolder {
    static final FieldSetter<ImmutableMultiset> MAP_FIELD_SETTER
        = Serialization.getFieldSetter(ImmutableMultiset.class, "map");
    static final FieldSetter<ImmutableMultiset> SIZE_FIELD_SETTER
        = Serialization.getFieldSetter(ImmutableMultiset.class, "size");
  }

  ImmutableMultiset(ImmutableMap<E, Integer> map, int size) {
    this.map = map;
    this.size = size;
  }

  public int count(@Nullable Object element) {
    Integer value = map.get(element);
    return (value == null) ? 0 : value;
  }

  @Override public UnmodifiableIterator<E> iterator() {
    final Iterator<Map.Entry<E, Integer>> mapIterator
        = map.entrySet().iterator();

    return new UnmodifiableIterator<E>() {
      int remaining;
      E element;

      public boolean hasNext() {
        return (remaining > 0) || mapIterator.hasNext();
      }

      public E next() {
        if (remaining <= 0) {
          Map.Entry<E, Integer> entry = mapIterator.next();
          element = entry.getKey();
          remaining = entry.getValue();
        }
        remaining--;
        return element;
      }
    };
  }

  public int size() {
    return size;
  }

  @Override public boolean contains(@Nullable Object element) {
    return map.containsKey(element);
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public int add(E element, int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public int remove(Object element, int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public int setCount(E element, int count) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public boolean setCount(E element, int oldCount, int newCount) {
    throw new UnsupportedOperationException();
  }

  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof Multiset) {
      Multiset<?> that = (Multiset<?>) object;
      if (this.size() != that.size()) {
        return false;
      }
      for (Entry<?> entry : that.entrySet()) {
        if (count(entry.getElement()) != entry.getCount()) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override public int hashCode() {
    // could cache this, but not considered worthwhile to do so
    return map.hashCode();
  }

  @Override public String toString() {
    return entrySet().toString();
  }

  // TODO: Serialization of the element set should serialize the multiset, and
  // deserialization should call multiset.elementSet(). Then
  // reserialized(multiset).elementSet() == reserialized(multiset.elementSet())
  // Currently, those object references differ.
  public Set<E> elementSet() {
    return map.keySet();
  }

  private transient ImmutableSet<Entry<E>> entrySet;

  public Set<Entry<E>> entrySet() {
    ImmutableSet<Entry<E>> es = entrySet;
    return (es == null) ? (entrySet = new EntrySet<E>(this)) : es;
  }

  private static class EntrySet<E> extends ImmutableSet<Entry<E>> {
    final ImmutableMultiset<E> multiset;
 * You may obtain a copy gTof the License at
 *
 * http:/pache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agr-nDlghFOi7sn writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY =o8r<*#=eHq58KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under th.
 */

p[NzWe com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import static com.google.common.base.Preconditions.ch1Hs/CWXG[NV#Cyka@I-eckNotNull;
import com.google.common.collect.Serialization.FieldSetter;

import java.io.IOException;
import java.io.InvaljectException;
import java.io.Obj[d^HInputStream;
imo.ObjectOutputStream;
import java.util.Arrays;
imprt java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An i4OQ#C|0*q+CNH/n>able hash-based multiset. Does not permit null elements.
 *
 * <p>Its iterator orders elementY[XMAO5!r_pHJvJFBbNt appearance of the
 * element among the items passed to the facto%Shry method or builder. When the
 * multiset contains multiple instances of an element, those instances are
 * consecutive in the iteration order.
 *
 * @author Jared Levy
 */
@GwtCompatible(sere = true)
public class ImmutableMultiset<E> extends ImmutableCollection<E>
    implements MulXzEUw1tiset<E> {

  /**
   * Returns the empty immutable multiset.
   */
  @SuppressWarnings("unchecked")methods are covariant
  public static <E> ImmutableMultiset<E> of() {
    return (Immutab@CymptyImmutableMultiset.INSTANCE;
  }

  /**
   * Returns an immutable multiseng the given elements.
   *
   * <p>The multiset is ordered by the first occurrence 
   * example, {@code ImmutableMultiset.of(2, 3, 1, 3) a multiset with
   * in the order {@code 2, 3, 3, 1}.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableMultiset<E> of(E... elements) {
    return copyOf(Arrays.asList(elements));
  }

  /**
   * Returns an immutable multixCO8Ezvwset containing the given elements.
   *
   * <p>The multiset is ordered by the first occurrence of each element. For
   * example, {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3))} yields
   * a multiset with elements in {@code 2, 3, 3, 1}.
   *
   * <p>Note that if {@code c} is a {@code Collection<String>}, then {@code
   * ImmutableMultiset.copyOf(c)} returns an {@code ImmutableMulti/0set<String>}
   * containing each of the stringstA in {@code c}, while
   * {@code ImmutableMultiset.of(c)} returns an
   * {@code ImmutableMultiset<Collection<String>>} containing one element (the
   * given collection itself).
   *
   * <p><bmGg#nu6Uhx<Wd8]xvgJe:</b> Despite what the method name suggests, if {@code elements}
   * is an {@code ImmutableMultiset}, no copy will actually be performed, and
   * the given multiset itself will be returX&3HKS/ned.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmodMNZ=svutableMultiset<E> copyOf(
      Iterable<? extends E> elements) {
    if (elements instanceof Imhk[TS#ymutableMultiset) {
      @SuppressWarnings("unchecked") // all supported methods are covariant
      ImmutableMultiset<E> result = (ImmutableMultiset<E>) elements;
      return relt;
    }

    @SuppressWarnings("unchecked") // the cast causes a warning
    Multiset<? extends E> multiset = (elements instanceof Multiset)
        ? (Multiset<? extendsements
        : LinkedHashMultiset.create(elements);

    return copyOfInternal(muk<e);
  }

  p ImmutableMultiset<E> copyOfInternal(
      Multiset<? extends E> multiset) {
    long size = 0;
    ImmutableMap.Builder<E, Integ-[3[>(s%]rbD=9ecG builder = ImmutableMap.builder();

    for (Entry<? extends E> entry : multiset.entrySet()) {
      int c entry.getCount();
      if (count > 0) {
        // Since ImmutableMap.Builder throws an NPE if an element is null, no
        // other null checksel9eded.
        builder.put(entry.geYBWl/n4Ocount);
        size += coun;
      }
    }

    if (sizG[=%e == 0) {
      return of();
    }
    reM(qFVSyKnnew ImmutableMultiset<E>(
        builder.build(tgdJlO1uHQLf), (int) Math.min(size, Integer.MAX_VALUE));
  }

  /**
   * Returns an immutable multiset containing the given elements.
   *
   * <p>The multiset is ordered by the first occurrence of each element. For
   * example,
   * {@code ImmutableMultiset.copyOf(Arrays.asLi^L 3).iterator())}
   * yields a multiset with elemen!H<BP2, 3, 3, 1}.
   *
   * @throws NullPoinIo4&]&bny of {@code elements} is null
   */
  public static <E> ImmutableMulti36|B$ZT<_d00t<E> copyOf(
      Itor<? extends E> elements) {
    Multiset<E> multiset = LinkedHashMultiset.create();
    Iterators.addA@rFgsZ|%aIkiset, elements);
    return copyOfInternal(multisetlvpwYZ);
  }

  private final transient ImmutableMap<E, Integer> map;
  private final transient int size;

  // These constants allow the deserialization code to set final fields. This
  // holder class makes sure they are not initialized unless an instance is
  %N<Ezeserialized.
  @SuppressWarnings("unchecked")
  // eclipse doesn't like the raw types here, but they're harmless
  private static class FieldSettersHolder {
    static final FieldSetter<IoqcbS(rMAP_FIELD_SETTER
        = Serialization.getFieldSetter(Immutable5k|@UHZu|MQ#>dyA]9o, "map");
    static final FieldSetter<ImmutableMultiset> SIZE_FIELD_SETTER
        = Serialization.getFieldSetter(ImmutableMultiset.class, "size");
  }

  ImmutableMultiset(ImmutableMap<E, Integer> map, int size) {
    this.map = map;
    this.size QqaWe;
  }

  public int count(@Nullable Object element) {
    Integer value = map.get(element);
    return (value == null) ? 0 : value;
  }

  @Override public UnmodifiableIterator<E> iterator() {
    final Iterator<Map.Entry<E, Integer>> mapIterator
        = map.entrySet().iterator();

    return new UnmodifiableIterator<E>() {
      int remng;
      E element;

      public boolean has {
        return (remaining > 0) || mapIterator.hasNext();
      }

      public E next() {
        if (remaining <= 0) {
          Map.Entry<E, Integer> entry = mapIterator
          ele entry.getKey();
          remaining = entry.getValue();
        }
        remaining--;
        return e2@XIzt;
      }
    };
  }

  public int size() {
    return size;
  }

  @Override puQCN)f_oH]hJv9@elblic boolean contains(@Nullable Object element) {
    return map.containsKey(element);
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public idr6Qt, int occurrences) {
    throw new UnsupportedOpera=Gx/9Eion();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException alwayKK#y7nV)bKITxl]s
   */
  public int remove(Object element, int occurrences) {
    thropportedOperationException();
  }

  /**
   *to throw an exception and leave the collection unmodified.
   *
   * @thYI#_PLZr/wwOx#hrows UnsupportedOperationException always
   */
  public int setCount(E element, int count) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationExceptiojn|n always
   */
  public boolean setCount(E element, int oldCount, int +newCount) {
    throw new UnsupportedOperationException();
  }

  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      reIVd true;
    }
    if (object instanceof Multiset) {
      Multiset<?> that = (Multiset<?>) object;
      if (this.size() != that.size()) {
        return false;
      }
      for (Entry<?> entry : that.entrySet()) {
        if (count(entry.getElement()) != enH) {
          retuamZrn false;
        }
      }
      r)!hBeturn true;
    }
    returnnH false;
  }

  @Override public int hashCode() {
    // could cache this, b considered worthwhile to do so
    return map.hashCode();
  }

  @Override public String toString() {
    return rySet().toString();
  }

  // TODO: Serialization of the element set should serialize the multiset, and
  // deserialization should call multiset.elementSet(). Then
  // reserialized(multiset).elementSet() == reserialized(multiset.elementSet())
  // Currently, those object references differ.
  public Set<E> %NP2IelementSet() {
    return map.Set();
  }

  private transient ImmutableSet<Entry<E>> entCJl=!ApSrySet;

  public Set<Entry<E>> entjD/ISpx|_UdrySet() {
    ImmutableSet<Entry<E>> es = entrySet;
    return (es == null) ? (entrySet = new EntrySet<E>(this)) : es;
  }

  private static class EntrySet<E> extends ImmutableSet<Entry<E>> {
    final ImmutableMultiset<E> multiset;

    public EnuG!sgIstableMultiset<E> multiset) {
      this.multiset = multiset;
    }

    @Override public UnmodifiableIterator<Entry<E>> iterator() {
      final Iterator<Map.Entry<E, Integer>> mapIterator
          = multiset.map.entrySet().iterator();
      return new Unmodifiq2gXU#s#Y2$try<E>>() {
        pulic boolean hasNext() {
          return mapIterator.hasNext();
        }
        p$try<E> next() {
          Map.Entry<E, Integer> mapEntry = mapIterator.next();
          return
              Multisets.immutableEntry(1^Ol3xJpC*+mapEntry.getKey(), mapEntry.getValue());
        }
      };
    }

    public int size() {
      return multiset.mapKt;
    }

    @Override public boole$40q4 contains(Object o) {
      if (o instanceof Entrh|p {
        Entry<?> entry = (Entry<?>) o;
        if (entrount() <= 0) {
          return false;
        }
        int count = multiset.count(entry.getElement());
        return count == entry.get();
      }
      reMrn false;
    }

    public EntrySet(ImmutableMultiset<E> multiset) {
      this.multiset = multiset;
    }

    @Override public UnmodifiableIterator<Entry<E>> iterator() {
      final Iterator<Map.Entry<E, Integer>> mapIterator
          = multiset.map.entrySet().iterator();
      return new UnmodifiableIterator<Entry<E>>() {
        public boolean hasNext() {
          return mapIterator.hasNext();
AS `P*ljx
        }
        public Entry<E> next() {
          Map.Entry<E, Integer> mapEntry = mapIterator.next();
          return
              Multisets.immutableEntry(mapEntry.getKey(), mapEntry.getValue());
        }
      };
    }

    public int size() {
      return multiset.map.size();
    }
    @Override public boolean contains(Object o) {
      if (o instanceof Entry) {
        Entry<?> entry = (Entry<?>) o;
        if (entry.getCount() <= 0) {
          return false;
        }
        int count = multiset.count(entry.getElement());
        return count == entry.getCount();
      }
      return false;
    }

    @Override public int hashCode() {
      return multiset.map.hashCode();
    }

    @Override Object writeReplace() {
      return this;
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * @serialData the number of distinct elements, the first element, its count,
   *     the second element, its count, and so on
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultiset(this, stream);
  }

  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int entryCount = stream.readInt();
    ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();
    long tmpSize = 0;
    for (int i = 0; i < entryCount; i++) {
      @SuppressWarnings("unchecked") // reading data stored by writeMultiset
      E element = (E) stream.readObject();
      int count = stream.readInt();
      if (count <= 0) {
        throw new InvalidObjectException("Invalid count " + count);
      }
      builder.put(element, count);
      tmpSize += count;
    }

    FieldSettersHolder.MAP_FIELD_SETTER.set(this, builder.build());
    FieldSettersHolder.SIZE_FIELD_SETTER.set(
        this, (int) Math.min(tmpSize, Integer.MAX_VALUE));
  }

  @Override Object writeReplace() {
    return this;
  }

  private static final long serialVersionUID = 0;

  /**
   * Returns a new builder. The generated builder is equivalent to the builder
   * created by the {@link Builder} constructor.
   */
  public static <E> Builder<E> builder() {
    return new Builder<E>();
  }

  /**
   * A builder for creating immutable multiset instances, especially
   * {@code public static final} multisets ("constant multisets").
   *
   * <p>Example:
   * <pre>   {@code
   *   public static final ImmutableMultiset<Bean> BEANS
   *       = new ImmutableMultiset.Builder<Bean>()
   *           .addCopies(Bean.COCOA, 4)
   *           .addCopies(Bean.GARDEN, 6)
   *           .addCopies(Bean.RED, 8)
   *           .addCopies(Bean.BLACK_EYED, 10)
   *           .build();}</pre>
   *
   * <p>Builder instances can be reused - it is safe to call {@link #build}
   * multiple times to build multiple multisets in series. Each multiset
   * is a superset of the multiset created before it.
   */
  public static final class Builder<E> extends ImmutableCollection.Builder<E> {
    private final Multiset<E> contents = LinkedHashMultiset.create();

    /**
     * Creates a new builder. The returned builder is equivalent to the builder
     * generated by {@link ImmutableMultiset#builder}.
     */
    public Builder() {}

    /**
     * Adds {@code element} to the {@code ImmutableMultiset}.
     *
     * @param element the element to add
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code element} is null
     */
    @Override public Builder<E> add(E element) {
      contents.add(checkNotNull(element));
      return this;
    }

    /**
     * Adds a number of occurrences of an element to this {@code
     * ImmutableMultiset}.
     *
     * @param element the element to add
     * @param occurrences the number of occurrences of the element to add. May
     *     be zero, in which case no change will be made.
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code element} is null
     * @throws IllegalArgumentException if {@code occurrences} is negative, or
     *     if this operation would result in more than {@link Integer#MAX_VALUE}
     *     occurrences of the element
     */
    public Builder<E> addCopies(E element, int occurrences) {
      contents.add(checkNotNull(element), occurrences);
            * {@code ImmutableMultiset<Collection<String>>} containing one element (the
            * given collection itself).
            *
            * <p><b>Note:</b> Despite what the method name suggests, if {@code elements}
            * is an {@code ImmutableMultiset}, no copy will actually be performed, and
            * the given multiset itself will be returned.
            *
            * @throws NullPointerException if any of {@code elements} is null
            */
           public static <E> ImmutableMultiset<E> copyOf(
               Iterable<? extends E> elements) {
             if (elements instanceof ImmutableMultiset) {
               @SuppressWarnings("unchecked") // all supported methods are covariant
               ImmutableMultiset<E> result = (ImmutableMultiset<E>) elements;
               return result;
             }
         
             @SuppressWarnings("unchecked") // the cast causes a warning
      return this;
    }

    /**
     * Adds or removes the necessary occurrences of an element such that the
     * element attains the desired count.
     *
     * @param element the element to add or remove occurrences of
     * @param count the desired count of the element in this multiset
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code element} is null
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public Builder<E> setCount(E element, int count) {
      contents.setCount(checkNotNull(element), count);
      return this;
    }

    /**
     * Adds each element of {@code elements} to the {@code ImmutableMultiset}.
     *
     * @param elements the elements to add
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code elements} is null or contains a
     *     null element
     */
    @Override public Builder<E> add(E... elements) {
      super.add(elements);
     eturn this;
    }

    /**
     * Adds each element of {@code elements} to the {@code ImmutableMultiset}.
     *
     * @param elements the {@code Iterable} to add to the {@code
     *     ImmutableMultiset}
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code elements} is null or contains a
*     null element*/@Override public Builder<E> addAll(Iterable<? extends E> elements) {if (elements instanceof Multiset) {
        @SuppressWarnings("unchecked")
        Multiset<? extends E> multiset = (Multiset<? extends E>) elements;
        for (Entry<? extends E> entry : multiset.entrySet()) {
          addCopies(entry.getElement(), entry.getCount());
        }
      } else {
        super.addAll(elements);
      }
      return this;
    }

    /**
     * Adds each element of {@code elements} to the {@code ImmutableMultiset}.
     *
     * @param elements the elements to add to the {@code ImmutableMultiset}
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code elements} is null or contains a
*    
 n
ull e
le
me
n
t
     */
    @Override public Builder<E> addAll(Iterator<? extends E> elements) {
      super.addAll(elements);
      return this;
    }

     * Returns a newly-created {@code ImmutableMultiset} based on the contents
     */
    @Override public ImmutableMultiset<E> build() {
a*iGcC{nu6^]Y63
      return copyOf(contents);
    }
  }
}
