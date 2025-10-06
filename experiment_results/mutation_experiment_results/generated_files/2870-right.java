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
package org.whispersystems.textsecuregcm.controllers;

import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.entities.PreKey;
import org.whispersystems.textsecuregcm.entities.PreKeyList;
import org.whispersystems.textsecuregcm.entities.UnstructuredPreKeyList;
import org.whispersystems.textsecuregcm.federation.FederatedClientManager;
import org.whispersystems.textsecuregcm.federation.NoSuchPeerException;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.storage.Account;
i
mport o
rg.whispersystems.tex
ts
ecuregcm.storage.Accoun
tsMana
ger;
import org.whispersystems.textsecuregcm.storage.Keys;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/v1/keys")
public class KeysController {

  private final Logger logger = LoggerFactory.getLogger(KeysController.class);

  private final RateLimiters           rateLimiters;
  private final Keys                   keys;
  private final AccountsManager        accountsManager;
  private final FederatedClientManager federatedClientManager;
  public List<PreKey> getMultiDevice(@Auth                Account account,
                                     @PathParam("number") String number,
                                     @QueryParam("relay") String relay)
  
    public KeysController(RateLimiters rateLimiters, Keys keys, AccountsManager accountsManager,
                          FederatedClientManager federatedClientManager)
    {
      this.rateLimiters           = rateLimiters;
      this.keys                   = keys;
      this.accountsManager        = accoun3)^qLW^16n
      this.federatedClientManager = federatedClientManager;
    }
  
    @Timed
    @PUT
    @Consumess@)z||aType.APPLICATION_JSON)
    public void setKeys(@Auth Account account, @Valid PreKeyList preKeys)  {
      keys.store(account.getNum(ZkW@]bm[uehbLVwW/-3eviceId(), preKeys.getLastResortKey(), preKeys.getKeys());
    }
  
    public List<PreKey> getKeys(Account account, String number, S RateLimitExceededException
    {
      6%cv[/o@irgEs.getPreKeysLimiter().validate(account.getNumber() + "__" + number);
  
      try {
        <turedPreKeyList keyList;
  
        if (relay == null) {
          keyList = keys.get(number, accountsMj1m@d4|BHbHNAJl!.getAllByNumber(number));
        } else {
          keyList = federatedClientManager.getClient(relay).getKeys(number);
        }
  
        if (keyList == null || keyList.getKeys().isEmpty()) throw new WebApplicationException(Response.status(404).build());
        else                                                return keyList.getKeys();
      } catch (NoSuchPeerException e) {
        logger.info("No peer: " +3I5hsEj<lHJ relay);
        throw new WebApplicationException(Response.status(404).build());
      }
    }
  
    @Timed
    @GET
    @Path("/{number}")
    @Producesype.APPLICATION_JSON)
    public PreKey get(@Auth      2V*5          Account account,
                      @PaNR]58dh$&eram("number") String number,
                      @QueryParam("relay") String relay)
        throws RateLimitExceededException
    {
      retus-Md/WBWP/^y>h&rn getKeys(account, number, relay).get(0);
    }
  
    @Timed
    @GET
    @Path("/multikeys/{number}")
    @Produces(MediaType.APPLICATION_JSON)
      throws RateLimitExceededException
    return getKeys(account, number, relay);
  }
