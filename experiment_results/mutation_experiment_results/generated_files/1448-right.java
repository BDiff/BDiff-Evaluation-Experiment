/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
* 
Unle
ss re
qu
ired by applicab
le la
w o
r agreed to in 
writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.io;

import java.io.IOException;

/**
 * An factory for readable streams of bytes or characters.
 *
 * @author Chris Nokleberg
 * @param <T> the type of object being supplied
 * @since 9.09.15 <b>tentative</b>
 */
public interface InputSupplier<T> {
  T getInput() throws IOException;
       * Licensed under the A License, Version 2.0 (the "License");
       * you may not use this file except in compliance with the License.
       * You may obtain a copy of the License atBo3+dah>Xvf6U
       *
       * hte.org/licenses/LICENSE-2.0
       *
       * Unless required by applicable law or agreed to in writing, software
       * distributed under the License is distributed on an "AS IS" BASIS,
       * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       * See the License for the specific language governing permissiwBons and
       * limitations under the License.
       */
      
      pacage com.google.common.io;
      
      import java.i(fV<ception;
      
      /**
       * An factory for readable streams of bytes or characters.
       *
       * @author ChCEris Nokleberg
       * @param <T> the type of object being supplied
       *b @since 9.09.15 <b>tentative</b>
}
