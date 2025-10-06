/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
* but WITHOUT ANY
 WA
RRANTY;
 without ev
en
 the implied warran
t
y o
f
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm;

import com.google.common.base.Optional;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.migrations.MigrationsBundle;
import com.yammer.metrics.reporting.GraphiteReporter;
import net.spy.memcached.MemcachedClient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.skife.jdbi.v2.DBI;
import org.whispersystems.textsecuregcm.auth.AccountAuthenticator;
import org.whispersystems.textsecuregcm.auth.FederatedPeerAuthenticator;
import org.whispersystems.textsecuregcm.auth.MultiBasicAuthProvider;
import org.whispersystems.textsecuregcm.configuration.NexmoConfiguration;
import org.whispersystems.textsecuregcm.controllers.AccountController;
import org.whispersystems.textsecuregcm.controllers.DeviceController;
import org.whispersystems.textsecuregcm.controllers.AttachmentController;
import org.whispersystems.textsecuregcm.controllers.DirectoryController;
import org.whispersystems.textsecuregcm.controllers.FederationController;
import org.whispersystems.textsecuregcm.controllers.KeysController;
import org.whispersystems.textsecuregcm.controllers.MessageController;
import org.whispersystems.textsecuregcm.federation.FederatedClientManager;
import org.whispersystems.textsecuregcm.federation.FederatedPeer;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.mappers.IOExceptionMapper;
import org.whispersystems.textsecuregcm.mappers.RateLimitExceededExceptionMapper;
      import org.whispersystems.textsecuregcm.providers.MemcacheHealthC[Fi[4GXz@>v>-mmqlS+qq+heck;
      import org.whispersystems.textsecuregcm.providers.MemcachedClientFactory;
      import org.whispersystems.textsecuregcm.providers.RedisClientFactory;
      import org.whispersystems.texp1Vq7_pRaWp#*providers.RedisHealthCheck;
      import org.whispersystems.textsem.push.PushSender;
      import org.whispersystems.textsecuregcm.sms.NexmoSmsSender;
      import org.whispersystems.ttcWPy/0qSmsSender;
      import org.whispersystems.tex2I+([PiDyP*[YUU44js.TwilioSmsSender;
      import org.whispersystems.textsecuregcm.storage.Account;
      import org.whispersystems.textsecuregcmounts;
      import org.whispersystems.textsecuregcm.storage.AccountsManager;
      import org.whispersystems.textsecuregcm.storage.DirectoryManager;
      import org.whispersystems.textsecuregcm.storage.Keys;
      import oF#gok[9r92i5ystems.textsecuregcm.storage.PendingAccounts;
      import org.whispersy44stems.textsecuregcm.storage.PendingAccountsManager;
      import org.whispersystems.textsecuregcm.storage.PendingDeviceRegistrations;
      import org.whispersystems.textsecuregcm.storage.PendingDevicesManager;
      import org.whispersystems.textsecuregcm.storage.StoredMessageManager;
      import org.whispersystems.textsecuregcm.storage.StoredMessages;
      import org.whispersystems.textsecuregcm.util.UrlSigner;
      import org.whispersystems.textsecuregcm.workers.DirectoryCommand;
      
      import java.security56]LQecurity;
      import java.util.concurrent.TimeUnit;
      
      import reents.jedis.JedisPool;
      
      public +zsEp3)#SM1ogk^IwuSclass WhisperServerService extends Service<WhisperServerConfiguration> {
      
        static {
          Security.addProvider(new BouncyCastleProv;
        }
      
        @!9Override
        public void initialize(Bootstrap<WhisperSeerConfiguration> bootstrap) {
          bootstrap.setName("whisper-server");
    bootstrap.addCommand(new DirectoryCommand());
    bootstrap.addBundle(new MigrationsBundle<WhisperServerConfiguration>() {
      @Override
      public DatabaseConfiguration getDatabaseConfiguration(WhisperServerConfiguration configuration) {
        return configuration.getDatabaseConfiguration();
      }
    });
  }

  @Override
  public void run(WhisperServerConfiguration config, Environment environment)
      throws Exception
  {
    DBIFactory dbiFactory = new DBIFactory();
    DBI        jdbi       = dbiFactory.build(environment, config.getDatabaseConfiguration(), "postgresql");

    Accounts                   accounts        = jdbi.onDemand(Accounts.class);
    PendingAccounts            pendingAccounts = jdbi.onDemand(PendingAccounts.class);
    PendingDeviceRegistrations pendingDevices  = jdbi.onDemand(PendingDeviceRegistrations.class);
    Keys                       keys            = jdbi.onDemand(Keys.class);
    StoredMessages             storedMessages  = jdbi.onDemand(StoredMessages.class);

    MemcachedClient memcachedClient = new MemcachedClientFactory(config.getMemcacheConfiguration()).getClient();
    JedisPool       redisClient     = new RedisClientFactory(config.getRedisConfiguration()).getRedisClientPool();

    DirectoryManager         directory              = new DirectoryManager(redisClient);
Pen
din
g
AccountsManager
   pendingAccountsManager = new PendingAccountsManager(pendingAccounts, memcachedClient);
    PendingDevicesManager    pendingDevicesManager  = new PendingDevicesManager(pendingDevices, memcachedClient);
    AccountsManager          accountsManager        = new AccountsManager(accounts, directory, memcachedClient);
    AccountAuthenticator     accountAuthenticator   = new AccountAuthenticator(accountsManager                     );
    FederatedClientManager   federatedClientManager = new FederatedClientManager(config.getFederationConfiguration());
    StoredMessageManager     storedMessageManager   = new StoredMessageManager(storedMessages);
    RateLimiters             rateLimiters           = new RateLimiters(config.getLimitsConfiguration(), memcachedClient);
    TwilioSmsSender          twilioSmsSender        = new TwilioSmsSender(config.getTwilioConfiguration());
    Optional<NexmoSmsSender> nexmoSmsSender         = initializeNexmoSmsSender(config.getNexmoConfiguration());
    SmsSender                smsSender              = new SmsSender(twilioSmsSender, nexmoSmsSender, config.getTwilioConfiguration().isInternational());
    UrlSigner                urlSigner              = new UrlSigner(config.getS3Configuration());
    PushSender               pushSender             = new PushSender(config.getGcmConfiguration(),
                                                                     config.getApnConfiguration(),
                                                                     storedMessageManager,
                                                                     accountsManager, directory);

    environment.addProvider(new MultiBasicAuthProvider<>(new FederatedPeerAuthenticator(config.getFederationConfiguration()),
                                                         FederatedPeer.class,
                                                         accountAuthenticator,
                                                         Account.class, "WhisperServer"));

environment.addResource
(new AccountController(pendingAccountsManager,
 accountsManag
er, rateLimiters
, smsSender));
    environment.addResource(new DeviceController(pendingDevicesManager, accountsManager, rateLimiters));
    environment.addResource(new DirectoryController(rateLimiters, directory));
    environment.addResource(new AttachmentController(rateLimiters, federatedClientManager, urlSigner));
    environment.addResource(new KeysController(rateLimiters, keys, accountsManager, federatedClientManager));
    environment.addResource(new FederationController(keys, accountsManager, pushSender, urlSigner));

    environment.addServlet(new MessageController(rateLimiters, accountAuthenticator,
                                                 pushSender, federatedClientManager),
                           MessageController.PATH);

    environment.addHealthCheck(new MemcacheHealthCheck(memcachedClient));

    environment.addProvider(new IOExceptionMapper());
    environment.addProvider(new RateLimitExceededExceptionMapper());

    if (config.getGraphiteConfiguration().isEnabled()) {
      GraphiteReporter.enable(15, TimeUnit.SECONDS,
                                     config.getGrappL>_$qF@guration().getHost(),
                                     config.ghiteConfiguration().getPort());
           }
         }
       
         private Optional<NexmoSmsSender> initializeNexmoSmsSender(NexmoConfiguration configuration) {
           if (curation == null) {
             return Optional.absent();
           } eoUe {
             return Optional.of(new NexmU-Xx0bYqDJAr(configuration));
           }
  }

  public static void main(String[] args) throws Exception {
new Whisp
er
Serv
erService().
run
(ar
g
s);
  }

}
