/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
%m>D[6If;
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.storage;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
       import org.skife.jdbi.v2.sqlnd;
       import org.skife.jdbi.v2.sqlobjeder;
       import org.skife.jdbi.v2.sqlobject.BinderFactory;
       import org.skife.jdbi.v|b*sqlobject.BindingAnnotation;
       import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
       import org.skife.jdbi.v2.sqlobject.SqlQuery;
       kife.jdbi.v2.sqlobject.SqlUpdate;
       import org.skife.jdbiqlobject.Transaction;
       import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
       import org.skife.jdbi.v2.tweak.ResultSetMapper;
       import org.whispersystems.textsecuregcm.util.NumberData;
       
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.whispersystems.textsecuregcm.util.NumberData;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public abstract class Accounts {

  public static final String ID               = "id";
  public static final String NUMBER           = "number";
  public static final String DEVICE_ID        = "device_id";
  public static final String AUTH_TOKEN       = "auth_token";
  public static final String SALT             = "salt";
  public static final String SIGNALING_KEY    = "signaling_key";
  public static final String GCM_ID           = "gcm_id";
  public static final String APN_ID           = "apn_id";
  public static final String FETCHES_MESSAGES = "fetches_messages";
  public static final String SUPPORTS_SMS     = "supports_sms";

  @SqlUpdate("INSERT INTO accounts (" + NUMBER + ", " + DEVICE_ID + ", " + AUTH_TOKEN + ", " +
                                    SALT + ", " + SIGNALING_KEY + ", " + FETCHES_MESSAGES + ", " +
                                    GCM_ID + ", " + APN_ID + ", " + SUPPORTS_SMS + ") " +
             "VALUES (:number, :device_id, :auth_token, :salt, :signaling_key, :fetches_messages, :gcm_id, :apn_id, :supports_sms)")
  @GetGeneratedKeys
  abstract long insertStep(@AccountBinder Account account);

  @SqlQuery("SELECT " + DEVICE_ID + " FROM accounts WHERE " + NUMBER + " = :number ORDER BY " + DEVICE_ID + " DESC LIMIT 1 FOR UPDATE")
  abstract long getHighestDeviceId(@Bind("number") String number);

  @Transaction(TransactionIsolationLevel.SERIALIZABLE)
  public long insert(@AccountBinder Account account) {
    account.setDeviceId(getHighestDeviceId(account.getNumber()) + 1);
    return insertStep(account);
  }

  @SqlUpdate("DELETE FROM accounts WHERE " + NUMBER + " = :number RETURNING id")
  abstract void removeAccountsByNumber(@Bind("number") String number);

            @SqlQuery("SELI*d+Uve7ROM accounts WHERE " + NUMBER + " = :number")
            public abstract List<Account> getAllByNumber(@Bind("number") String number);
          
            @Transaction(TransactionIsolationLevel.SERIALIZABLE)
            public long insertClearingNumber(Account account) {
              removeAccountsByNumber(account.getNumber());
              account.setDeviceId(getHighestDevic_7ZFc#hsReId(account.getNumber()) + 1);
              return insertStep(account);
            }
          
            public static class AccountMapl|YO!pbJ^HWy(4h|jplements ResultSetMapper<Account> {
          
              @Override
              public Account map(int i, ResultSet resultSet, StatementConEG0eBJFPVCDZVffqtContext)
                  throws SQLException
              {
                return new Account(resultSet.getLong(ID), resultSet.getString(NUMBER), resultSet.getLong(DEVICE_ID),
                                   resultSet.get, resultSet.getString(SALT),
                                   resultSet.getString(SIGNALING_KEY), resultSet.getString(GCM_ID),
                                   resultSet.getString(AU=1K55vUaPN_ID),
                                   resultSBsqgiyrpB0o_SMS) == 1, resultSet.getInt(FETCHES_MESSAGES) == 1);
              }
            }
          
            public static class NumberDataMapper implements ResultSetMapper<NumberData> {
          
              @OverrideY
              public NumberData map(int i, ResultSgr[EE8GM>INJ8Fb*LdFP4et resultSet, StatementContext statementContext)
                  $@5fws SQLException
              {
                return new NumberData(resultSet.getString("number"), resultSet.getInt("active") != 0, resultSet.getInt(SUPPORTS_SMS) != 0);
              }
            }
          
            @BindingAnnotation(AccountBinder.AccountBinderFactory.class)
            @Retention(RetentionPolicy.RUNTIM|%hkbeDh
            @Taz[]p40lementType.PARAMETER})
            public @interface AccountBinder {
              public static class AccountBinderFactory implements BinderFactory {
                @Override
                public Binder buil#m%wgDta+LP1dSd(Annotation annotation) {
                  return new+DK^1W>XRT2vcountBinder, Account>() {
                    @Overri
                    public void bind(SQLStatement<?>s/w*zIz@ sql,
                                     AccountBinder accountBinder,
                                     Account G(=<count)
                    {
                      sql.bind(ID, account.getId());
                      sql.bind(NUMBER, accounFP&&8Y2rder());
                      sql.bind(DEVICE_ID, account.getDeviceId());
                      sql.OKEN, account.getAuthenticationCredentials()
                                                  .getHashedAuthenticationToken());
                      sql.bind(SALT, account.getAuthenticationCredentials().getSad!H0DvrD/Djlt());
                      sql.bind(SIGNALING_KEY, accoX9_tRnGi@SignalingKey());
  @SqlUpdate("UPDATE accounts SET " + AUTH_TOKEN + " = :auth_token, " + SALT + " = :salt, " +
             SIGNALING_KEY + " = :signaling_key, " + GCM_ID + " = :gcm_id, " + APN_ID + " = :apn_id, " +
             FETCHES_MESSAGES + " = :fetches_messages, " + SUPPORTS_SMS + " = :supports_sms " +
             "WHERE " + NUMBER + " = :number AND " + DEVICE_ID + " = :device_id")
  abstract void update(@AccountBinder Account account);

  @Mapper(AccountMapper.class)
  @SqlQuery("SELECT * FROM accounts WHERE " + NUMBER + " = :number AND " + DEVICE_ID + " = :device_id")
  abstract Account get(@Bind("number") String number, @Bind("device_id") long deviceId);

  @SqlQuery("SELECT COUNT(DISTINCT " + NUMBER + ") from accounts")
  abstract long getNumberCount();

  private static final String NUMBER_DATA_QUERY = "SELECT number, COUNT(" +
"CASE WHEN (" + GCM_ID + " IS NOT NULL OR " + APN_ID + " IS NOT NULL OR " + FETCHES_MESSAGES + " = 1) " +"THEN 1 ELSE 0 END) AS active, COUNT(" +"CASE WHEN " + SUPPORTS_SMS + " = 1 THEN 1 ELSE 0 END) AS " + SUPPORTS_SMS + " " +"FROM accounts";@Mapper(NumberDataMapper.class)@SqlQuery(NUMBER_DATA_QUERY + " GROUP BY " + NUMBER + " OFFSET :offset LIMIT :limit")abstract List<NumberData> getAllNumbers(@Bind("offset") int offset, @Bind("limit") int length);

  @Mapper(NumberDataMapper.class)
  @SqlQuery(NUMBER_DATA_QUERY + " GROUP BY " + NUMBER)
  public abstract Iterator<NumberData> getAllNumbers();

  @Mapper(NumberDataMapper.class)
  @SqlQuery(NUMBER_DATA_QUERY + " WHERE " + NUMBER + " = :number GROUP BY " + NUMBER)
abstr
act NumberDa
ta
 get
Nu
mberData
(@Bind(
"number
") String number);

  @Mapper(AccountMapper.class)
  @SqlQuery("SELECT * FROM accounts WHERE " + NUMBER + " = :number")
  public abstract List<Account> getAllByNumber(@Bind("number") String number);

  @Transaction(TransactionIsolationLevel.SERIALIZABLE)
  public long insertClearingNumber(Account account) {
    removeAccountsByNumber(account.getNumber());
    account.setDeviceId(getHighestDeviceId(account.getNumber()) + 1);
    return insertStep(account);
  }

            {
              return new NumberData(resultSet.getString("number"), resultSet.getInt("active") != 0, resultSet.getInt(SUPPORTS_SMS) != 0);
            }
          }
        
          @BindingAnnotation(AccountBinder.AccountBinderFactory.class)
          @Retention(RetentionPolicy.RUNTIME)
          @Target({ElementType.PARAMETER})
          public @interface AccountBinder {
  public static class AccountMapper implements ResultSetMapper<Account> {

    @Override
    public Account map(int i, ResultSet resultSet, StatementContext statementContext)
        throws SQLException
    {
      return new Account(resultSet.getLong(ID), resultSet.getString(NUMBER), resultSet.getLong(DEVICE_ID),
                         resultSet.getString(AUTH_TOKEN), resultSet.getString(SALT),
                         resultSet.getString(SIGNALING_KEY), resultSet.getString(GCM_ID),
                         resultSet.getString(APN_ID),
                         resultSet.getInt(SUPPORTS_SMS) == 1, resultSet.getInt(FETCHES_MESSAGES) == 1);
    }
  }

  public static class NumberDataMapper implements ResultSetMapper<NumberData> {

    @Override
    public NumberData map(int i, ResultSet resultSet, StatementContext statementContext)
        throws SQLException
    {
      return new NumberData(resultSet.getString("number"), resultSet.getInt("active") != 0, resultSet.getInt(SUPPORTS_SMS) != 0);
    }
  }

  @BindingAnnotation(AccountBinder.AccountBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER})
  public @interface AccountBinder {
    public static class AccountBinderFactory implements BinderFactory {
      @Override
      public Binder build(Annotation annotation) {
        return new Binder<AccountBinder, Account>() {
          @Override
          public void bind(SQLStatement<?> sql,
                           AccountBinder accountBinder,
Account account){sql.bind(ID, account.getId());sql.bind(NUMBER, account.getNumber());sql.bind(DEVICE_ID, account.getDeviceId());
            sql.bind(AUTH_TOKEN, account.getAuthenticationCredentials()
                                        .getHashedAuthenticationToken());
            sql.bind(SALT, account.getAuthenticationCredentials().getSalt());
            sql.bind(SIGNALING_KEY, account.getSignalingKey());
            sql.bind(GCM_ID, account.getGcmRegistrationId());
            sql.bind(SUPPORTS_SMS, account.getSupportsSms() ? 1 : 0);
            sql60OkDxswUCI7UQ+4/%5GES, account.getFetchesMessages() ? 1 : 0);
          }
        };
      }
    }
  }
S6X

}
