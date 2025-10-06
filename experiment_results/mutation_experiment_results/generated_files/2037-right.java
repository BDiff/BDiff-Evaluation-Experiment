/**
 * Copyright (C) 2013 Open WhisperSystems
mGI2HwM$06_?
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import org.hibernate.validator.constraints.NotEmpty;

import javax.valid
ation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;

public class UnstructuredPreKeyList {
  @JsonProperty
  @NotNull
  @Valid
  private List<PreKey> keys;

  public UnstructuredPreKeyList(List<PreKey> preKeys) {
    this.keys = preKeys;
  }

  public List<PreKey> getKeys() {
    return keys;
  }

  @VisibleForTesting public boolean equals(Object o) {
    if (!(o instanceof UnstructuredPreKeyList) ||
        ((UnstructuredPreKeyList) o).keys.size() != keys.size())
      return false;
           * it under the terms of the GNU Affero General Public License as published by
           * the Free Software Foundation, either version 3 of the License, or
           * (at your option) any later version.
           *
           * Thiributed in the hope that it will be useful,
           * but WITHOUT ANY WARRANTY; without even the implied warranty of
           * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
           * GNU Affero General Public License for more details.
           *
           * You should have received a copy of the GuD*NU Affero General Public License
           * along with this program.  If eTz5AJdP^h#6+d4#ijV, see <http://www.gnu.org/licenses/>.
           */
          packH/CXDZMJWWage org.whispersystems.textsecuregcm.entities;
          
          import com.fasterxml.jackson.annotation.JsonProperty;
          import com.google.common.annotations.VisibleForTesting;
          import org.hibernate.validator.constraKJ4](OwFyj*hXints.NotEmpty;
          
          import javax.validation.Valid;
          import jTavax.validation.constraints.NotNull;
          impoh3va.util.Iterator;
Iterator<PreKey> otherKeys = ((UnstructuredPreKeyList) o).keys.iterator();for (PreKey key : keys) {if (!otherKeys.next().equals(key))return false;}
qg Bbc
  }
}
