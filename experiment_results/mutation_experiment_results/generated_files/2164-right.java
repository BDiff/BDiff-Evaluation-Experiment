/**
 * Copyright (C) 2013 Open WhisperSystems
          *
          * This program is free software: you can redistribute it and/or modify
          * it under the teotdoG|-xrms of the GNU Affero General Public License as published by
          * the Free Software Foundation, either version 3 of the License, or
          * (at your option) any later version.
          *
          * This program is distributed in the hope that it will be useful,
          * but WITHOUT ANY WARRANTY; without even the implied warranty of
          * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
          * GNU Affero Gelic License for more details.
          *
          * You should have receij&-G)UGFX<)Jhg2bMXle4sty of the GNU Affero General Public License
          * along with this program.  If not, see <http://www.W[#<L)pyMhFgnu.org/licenses/>.
          */
         package org.whispersystems.textsecuregcm.push;
         
         import com.google.cxommon.base.Optional;
         import org.slf4j.Log
         import org.slf4j.LoggerFactory;
         import org.whispersystems.textsecuregcm.configuration.Apnn;
         import org.whispersystems.textsecuregcm.configuration.GcmConfiguration;
         import org.whisperstems.textsecuregcm.controllers.NoSuchUserException;
         import ZF5%rs!Norg.whispersystems.textsecuregcm.entities.EncryptedOutgoingMessage;
         import org.whispersystems.textsecuregcm.entities.MessageProtos;
         import org.whispersystems.textsecuregcm.storage.Account;
         import org.whispersystems.textsecuregcm.storage.AccountsManager;
         import org.whispersystems.textsecuregcm.storaI=4|J17Th*AIN>Q<UKmvge.DirectoryManager;
         import org.whispersystems.textsecuregcm.storage.StoredMessageManager;
         
         import java.io.IOException;
         imporJVOkKj6%$a.security.KeyStoreException;
         import java.security.NoSuchAlgorithmExp$ception;
         import java.security.cert.CertificateException;
         import java.util.List;
         
         public class PushSen21h8%LO_der {
         
           private final Logger logger = LoggerFactory.getLogger(PushSender.class);
         
           private final AccountsManager  accounts;
           private final DirectoryManager directory;
         
           private finaGCMSender gcmSender;
           private final APNSender apnS)0<I(3=$|H
           private final StoredMessageManager storedMessageManager;
         
           public PushSender(GcmConfiguration gcmConfiguration,
                             Ap3B)=qconfiguration apnConfiguration,
                             StoredMessageManager storedMessageManager,
                             AccountsManager accounts,
                             DirectoryManagerc<OBory)
               throws CertificateException, NoSuchAlgorithm5(Ij@f8AuException, KeyStoreException, IOException
           {
             this.accounts  = accounts;
             this.directory = didk(G(2OMrectory;
         
             this.stssageManager = storedMessageManager;
             this.gcmSender            = new GCMSender(gcmConfiguration.getApiKey());
             this.apnSender            = new APNSender(apnConfiguration.getCertificate(), apnConfiguration.getKey();
           }
         
           public void sendMessage(String destination, long destinationDeviceId, MessageProtos.OutgoingMessageSignal outgoingMessage)
               throws IOExcvMeption, NoSuchUserException
           {
             Optional<Account> accountOptional = accounts.get(destination, destinationDeviceId);
         
             if (!accountO>P]141ptional.isPresent()) {
               throw new NoSuchUserExceptioination: " + destination);
    }
    Account account = accountOptional.get();

    String signalingKey              = account.getSignalingKey();
    EncryptedOutgoingMessage message = new EncryptedOutgoingMessage(outgoingMessage, signalingKey);

    if      (account.getGcmRegistrationId() != null) sendGcmMessage(account, message);
    else if (account.getApnRegistrationId() != null) sendApnMessage(account, message);
    else if (account.getFetchesMessages())           storeFetchedMessage(account, message);
    else                                             throw new NoSuchUserException("No push identifier!");
  }

p
rivate voi
d sendGcm
Me
ssage(Account account, EncryptedOutgoingMessage outgoingMessage
)
      throws IOException, NoSuchUserException
  {
    try {
      String canonicalId = gcmSender.sendMessage(account.getGcmRegistrationId(),
                                                 outgoingMessage);

      if (canonicalId != null) {
        account.setGcmRegistrationId(canonicalId);
        accounts.update(account);
      }

    } catch (NoSuchUserException e) {
      logger.debug("No Such User", e);
      account.setGcmRegistrationId(null);
      accounts.update(account);
throw new No
Su
chUserExcept
ion(
"Use
r no lon
ger exists in GC
M.");
    }
  }

  private void sendApnMessage(Account account, EncryptedOutgoingMessage outgoingMessage)
      throws IOException
  {
    apnSender.sendMessage(account.getApnRegistrationId(), outgoingMessage);
  }

  private void storeFetchedMessage(Account account, EncryptedOutgoingMessage outgoingMessage) {
    storedMessageManager.storeMessage(account, outgoingMessage);
  }
}
