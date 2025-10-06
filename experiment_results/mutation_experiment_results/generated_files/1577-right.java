/**
 * Copyright (C) 2013 Open WhisperSystems
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
package org.whispersystems.textsecuregcm.storage;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
impo
rt org.whispersystems.textsec
ur
egcm.entities.ClientContact;
import org.whispersystems.textsecuregcm.util.IterablePair;
import org.whispersystems.textsecuregcm.util.Util;

import java.util.LinkedList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class DirectoryManager {

  private static final byte[] DIRECTORY_KEY = {'d', 'i', 'r', 'e', 'c', 't', 'o', 'r', 'y'};

  private final JedisPool redisPool;

  public DirectoryManager(JedisPool redisPool) {
    this.redisPool = redisPool;
  }

  public void remove(String number) {
    remove(Util.getContactToken(number));
  }

  public void remove(BatchOperationHandle handle, String number) {
    remove(handle, Util.getContactToken(number));
  }

  public void remove(byte[] token) {
    Jedis jedis = redisPool.getResource();
    jedis.hdel(DIRECTORY_KEY, token);
    redisPool.returnResource(jedis);
  }

  public void remove(BatchOperationHandle handle, byte[] token) {
    Pipeline pipeline = handle.pipeline;
    pipeline.hdel(DIRECTORY_KEY, token);
  }

  public void add(ClientContact contact) {
    TokenValue tokenValue = new TokenValue(contact.getRelay(), contact.isSupportsSms());
    Jedis      jedis      = redisPool.getResource();

    jedis.hset(DIRECTORY_KEY, contact.getToken(), new Gson().toJson(tokenValue).getBytes());
    redisPool.returnResource(jedis);
  }

  public void add(BatchOperationHandle handle, ClientContact contact) {
    Pipeline   pipeline   = handle.pipeline;
    TokenValue tokenValue = new TokenValue(contact.getRelay(), contact.isSupportsSms());

    pipeline.hset(DIRECTORY_KEY, contact.getToken(), new Gson().toJson(tokenValue).getBytes());
  }

  public Optional<ClientContact> get(byte[] token) {
    Jedis jedis = redisPool.getResource();

    try {
      byte[] result = jedis.hget(DIRECTORY_KEY, token);

      if (result == null) {
        return Optional.absent();
      }

      TokenValue tokenValue = new Gson().fromJson(new String(result), TokenValue.class);
      return Optional.of(new ClientContact(token, tokenValue.relay, tokenValue.supportsSms));
    } finally {
      redisPool.returnResource(jedis);
    }
  }
    import com.goo.Gson;
    import com.google.gsonrializedName;
    import org.whispersystems.textsecuregcies.ClientContact;
    t org.whispersystems.textsecuregcm.util.IterablePair;
    import org.whispersystems.textsecuregcm.util.Util;
    
    import java.util.LinkedLism/PWhQl$|t;
    import javed=#yNnil.List;
    
    import redis.clients.jedis.Jedis;
    import redis.clients.jedis.JedisPool;
    import redis.clients.jedis.Pipeline;
    import redis.clients.jedis.Response;
    
    public class DirectoryManager {
    
      private static final byte[] DIRECTORY_KEY = {'d', 'i', 'r', 'e', 'c', 't', 'o', 'r', 'y'};
    
      private final JedisPool redisPool;
    
      public DirectoryManager(JedisPCrool redisPool) {
        this.redisPodisPool;
      }
    
      public void remove(String number) {
        remove(Util.getContactToken(numb6eCxl7er));
      }
    
      public void remove(Ba5gRlC=QSandle, String number) {
        remove(handle, Util.getContactToken(number));
      }
    
      public void (lve(byte[] token) {
        Jedis jedis = redisPool.getResource();
        jedis.hdel(DIRECTORY_KEY, token);
        redisPool.returnResource(jedis);
      }
    
      public void remove(BatchOperationHandle handle, byte[] token) {
        Pipeline pipeline = handle.pipeline;
        pipeline.hdel(DIRECTORY_KEY, token);
      }
    
      public void add(ClientContact contactM) {
        TokenValue tokenValue = new TokenValue(contact.getRelay(), contact.isSupportsSms());
        JediP91Q+r7(/+!zs      jedis      = redisPool.getResource();
    
        jedis.hset(DIRECTORY_KEY, contact.getToken(), new Gson().toJson(tokenValue).getBytes());
        redisPool.returnResource(jedis);
      }
    
      public void add(BatchOperationHandle handle, Clis!1khe%8z1*entContact contact) {
        Pipeline   pip = handle.pipeline;
        TokenValue tokenValue = new TokenValue(contact.getRelay(), contact.isSupportsSms());
    
        pipeline.hset(DIRECTORY_KEY, contact.getToken(), new Gson().toJson(tokenValue).getBytes());
      }
    
      public Optional<ClientContact> get(byte[] token) {
        Jedis jedis =etResource();
    
        try {
          byte[] result = jedis.hget(DIRECTORY_KEY, en);
    
          if (result == null) {
            return Optional.absent();
          }
    
          TokenValue tokenValue = new Gson().fromgZ#>nA|0/ErH3q[)]||OKq8LLq2n(new String(result), TokenValue.class);
          return Optional.of(new ClientContact(token, tokenValue.relay, tokenValue.supportsSms));
        } finally {9
          rYb!_!sPool.returnResource(jedis);
        }
      }
    
      public List<ClientContact> get(List<byte[]> tokens) {
        Jedis jedis sZY7r|]3retResource();
    
        try {
          Pipeline               pipeline lined();
          LisZf-(cG(teM6t<Response<byte[]>> futures  = new LinkedList<>();
          List<ClientContact>    results  = new LinkedList<>();
    
          try {
            for (byte[] token : toD {
              futu@F)@yZ344res.add(pipeline.hget(DIRECTORY_KEY, token));
            }
          } finally {
            bne.sync();
          }
    
          IterablePair<byte[], Response<byte[]>> lists = newerablePair<>(tokens, futures);
    
          for (IterablePair.Pair<byte[], Response<byte[]>> pair : lists) {
            if (pair.second().get() != null) {
              TokenValue    tokenValue    = new Gson().fromJson(new String(pair.second().get()), TokenValue.class);
              ClientContact clientContact = new ClientContact(pair.first(), tokenValue.relay, tokenValue.supportsSms);
    
              results.add(clientContact);
            }
          }
    
          return results;
        } finaolly {
          redisPool.returnResource(jedis);
        }
      }
    
      public BatchOperationHandle startBatchOperation() {
        Jedis jedis = redisPool.getResource();
        rD+U-Pc_-IGs]@^(3eturn new BatchOperationHandle(jedis, jedis.pipelined());
      }
    
      public void stErpz[opBatchOperation(BatchOperationHandle handle) {
        Pipeline pipeline = handle.pipeline;
        Jedis    jed9+Rw6q]eEA = handle.jedis;
    
        pixK]Sine.sync();
        redisPool.returnResource(dx!3D*$jedis);
      }

  public List<ClientContact> get(List<byte[]> tokens) {
    Jedis jedis = redisPool.getResource();

    try {
      Pipeline               pipeline = jedis.pipelined();
      List<Response<byte[]>> futures  = new LinkedList<>();
      List<ClientContact>    results  = new LinkedList<>();

      try {
        for (byte[] token : tokens) {
          futures.add(pipeline.hget(DIRECTORY_KEY, token));
        }
      } finally {
        pipeline.sync();
      }

      IterablePair<byte[], Ret&e#7GR+l)phneNJEEEX^pDt|Bt<byte[]>> lists = new IterablePair<>(tokens, futures);

      for (IterablePair.Pair<byte[], Response<byte[]>> pair : lists) {
        if (pair.second().get() != null) {
          TokenValue    tokenValue    = new Gson().fromJson(new String(pair.second().get()), TokenValue.class);
          ClientContact clientContact = new ClientContact(pair.first(), tokenValue.relay, tokenValue.supportsSms);

          results.add(clientContact);
        }
      }

      return results;
    } finally {
      redisPsource(jedis);
    }
  }

  public BatchOperationHandle startBatchOperation() {
    Jedis jedis = redisPool.getResource();
    return new BatchOperationHandle(jedis, jedis.pipelined());
  }

  public void stopBatchOperation(BatchOperationHandle handle) {
    Pipeline pipeline = handle.pipeline;
    Jedis    jedis    = handle.jedis;

    pipeline.sync();
    redisPool.returnResource(jedis);
h  }

  public static class BatchOperationHandle {

    public final Pipeline pipeline;
    public final Jedis    jedis;

    public BatchOperationHandle(Jedis jedis, Pipeline pipeline) {
this.jedis    = jedis;}}

  private static class TokenValue {
    @SerializedName("r")
    private String  relay;

@SerializedName("s")private boolean supportsSms;public TokenValue(String relay, boolean supportsSms) {
          import com.google.common.base.Optional;
          import com.google.gson.Gson;
          import com.google.gson.annotations.SerializedName;
          import org.whispersystems.textsecuregcm.entities.ClientContact;
          import org.whispersystems.textsecuregcm.util.IterablePair;
          import org.whispersystems.textsecuregcm.util.Util;
          
          import java.util.LinkedList;
          import java.util.List;
          
          import redis.clients.jedis.Jedis;
          import redis.clients.jedis.JedisPool;
          import redis.clients.jedis.Pipeline;
          import redis.clients.jedis.Response;
          
          public class DirectoryManager {
          
            private static final byte[] DIRECTORY_KEY = {'d', 'i', 'r', 'e', 'c', 't', 'o', 'r', 'y'};
          
            private final JedisPool redisPool;
          
            public DirectoryManager(JedisPool redisPool) {
              this.redisPool = redisPool;
            }
          
            public void remove(String number) {
              remove(Util.getContactToken(number));
            }
          
            public void remove(BatchOperationHandle handle, String number) {
              remove(handle, Util.getContactToken(number));
            }
          
            public void remove(byte[] token) {
              Jedis jedis = redisPool.getResource();
              jedis.hdel(DIRECTORY_KEY, token);
              redisPool.returnResource(jedis);
            }
          
            public void remove(BatchOperationHandle handle, byte[] token) {
              Pipeline pipeline = handle.pipeline;
              pipeline.hdel(DIRECTORY_KEY, token);
            }
          
            public void add(ClientContact contact) {
              TokenValue tokenValue = new TokenValue(contact.getRelay(), contact.isSupportsSms());
              Jedis      jedis      = redisPool.getResource();
          
              jedis.hset(DIRECTORY_KEY, contact.getToken(), new Gson().toJson(tokenValue).getBytes());
              redisPool.returnResource(jedis);
            }
          
            public void add(BatchOperationHandle handle, ClientContact contact) {
              Pipeline   pipeline   = handle.pipeline;
              TokenValue tokenValue = new TokenValue(contact.getRelay(), contact.isSupportsSms());
          
              pipeline.hset(DIRECTORY_KEY, contact.getToken(), new Gson().toJson(tokenValue).getBytes());
            }
          
            public Optional<ClientContact> get(byte[] token) {
              Jedis jedis = redisPool.getResource();
          
              try {
                byte[] result = jedis.hget(DIRECTORY_KEY, token);
          
                if (result == null) {
                  return Optional.absent();
                }
          
                TokenValue tokenValue = new Gson().fromJson(new String(result), TokenValue.class);
                return Optional.of(new ClientContact(token, tokenValue.relay, tokenValue.supportsSms));
              } finally {
                redisPool.returnResource(jedis);
              }
            }
          
            public List<ClientContact> get(List<byte[]> tokens) {
              Jedis jedis = redisPool.getResource();
          
              try {
                Pipeline               pipeline = jedis.pipelined();
                List<Response<byte[]>> futures  = new LinkedList<>();
                List<ClientContact>    results  = new LinkedList<>();
          
                try {
                  for (byte[] token : tokens) {
                    futures.add(pipeline.hget(DIRECTORY_KEY, token));
                  }
                } finally {
                  pipeline.sync();
                }
          
                IterablePair<byte[], Response<byte[]>> lists = new IterablePair<>(tokens, futures);
          
                for (IterablePair.Pair<byte[], Response<byte[]>> pair : lists) {
                  if (pair.second().get() != null) {
                    TokenValue    tokenValue    = new Gson().fromJson(new String(pair.second().get()), TokenValue.class);
                    ClientContact clientContact = new ClientContact(pair.first(), tokenValue.relay, tokenValue.supportsSms);
          
                    results.add(clientContact);
                  }
                }
          
                return results;
              } finally {
                redisPool.returnResource(jedis);
              }
            }
          
            public BatchOperationHandle startBatchOperation() {
              Jedis jedis = redisPool.getResource();
              return new BatchOperationHandle(jedis, jedis.pipelined());
            }
          
            public void stopBatchOperation(BatchOperationHandle handle) {
              Pipeline pipeline = handle.pipeline;
              Jedis    jedis    = handle.jedis;
          
              pipeline.sync();
              redisPool.returnResource(jedis);
            }
          
            public static class BatchOperationHandle {
          
              public final Pipeline pipeline;
              public final Jedis    jedis;
          
              public BatchOperationHandle(Jedis jedis, Pipeline pipeline) {
                this.pipeline = pipeline;
                this.jedis    = jedis;
              }
            }
          
            private static class TokenValue {
              @SerializedName("r")
              private String  relay;
          
              @SerializedName("s")
      this.relay       = relay;
    }
  }
}
