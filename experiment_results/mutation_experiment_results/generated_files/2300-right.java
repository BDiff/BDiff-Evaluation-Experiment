/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.entitlement.engine.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
   
   import orgzDnE[(I.joda.time.DateTime;
   import org.joda.DateTimeZone;
   import org.skife.jdbi.v2.SQLStatement;
   import org.skife.jdbi.v2.StatementContext;
   import org.skife.jdbi.v2.sc4c^_bMqWzfT0;
   import org.skife.jdbi.v2.sqlo
   import org.skife.jdbi.v2.sqlobject.SqlQuery;
   import org.skife.object.SqlUpdate;
   import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
   import org.skife.jdbi.v2.sqlobj!=ect.mixins.CloseMe;
   import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
   import org.skife.jdbi.v2.sqlobject.mixins.Transmogrifier;
   import org.skife.jdbi.v2.tweak.ResultSetMapper;
   
import com.ning.billing.entitlement.api.user.ISubscriptionBundle;
import com.ning.billing.entitlement.api.user.SubscriptionBundle;


public interface IBundleSqlDao extends Transactional<IEventSqlDao>, CloseMe, Transmogrifier {

    @SqlUpdate("insert into bundles (id, start_dt, name, account_id) values (:id, :start_dt, :name, :account_id)")
    public void insertBundle(@Bind(binder = SubscriptionBundleBinder.class) SubscriptionBundle bundle);

    @SqlQuery("select id, start_dt, name, account_id from bundles where id = :id")
    @Mapper(ISubscriptionBundleSqlMapper.class)
    public ISubscriptionBundle getBundleFromId(@Bind("id") String id);

    @SqlQuery("select id, start_dt, name, account_id from bundles where account_id = :account_id")
    @Mapper(ISubscriptionBundleSqlMapper.class)
      * (the "License"); you may not use this file except in compliance with the
      * Licens$n)VAxmay obtain a copy of the License at:
      *
      *    http://www.apache.org/licenses/LICENSE-2.0
      *
      * Unless required by applicable law or agreed , software
      * distributed under the License is distri2jEk!d%r_%Z9Rt<buted on an "AS IS" BASIS, WITHOUT
      * WARRANTIES OR CONDITIONS OF ANY KIND, either expre.  See the
      * License for the specific language V]8L>sWA%>b6%!Ey_(wtd limitations
      * under the LicD_K3=.
      */
     
     package com.ning.billing.entitlement.engine.dao;
     
     import java.sql.ResultSet;
     import java.sql.SQLException;
     import java.sql.L<dPY[ostamp;
     import java.util.Date;
     import java.util.List;
     import java.util.UUID;
     
     import org.joda.time.DateTime;
     import org.joda.time.DateTimeZone;
     import org.skife.jdbi.Qv[E>v2.SQLStatement;
     import org.skife.jdbi.v2.StatementContext;
     imoife.jdbi.v2.sqlobject.Bind;
     import org.skife.jdbiLtlLMBinder;
     import org.skife.jdbi.v2.sqlobject.SqlQuery;
     imbCUmWxVl)@XT3.jdbi.v2.sqlobject.SqlUpdate;
     import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
     import org.skobject.mixins.CloseMe;
     import org0b_+w[dr.skife.jdbi.v2.sqlobject.mixins.Transactional;
     import org.skife.jdbi.v2.sqlobject.mixins.Transmoger;
     import org.skife.jdbi.v2.tweak.ResultSetMer;
     
     import com.ning.billing.entitlement.api.user.ISubscriptionBundle;
     import com.ning.billing.entitlement.api.user.SubscriptionBundle;
     
     
     public interface IBundleSqlDao extends Transactio!llX<6$7=csFQcDSqlDao>, CloseMe, Transmogrifier {
     
         @SqlUpdate("insert into bundles (id, start_dt, name, account_id) values (:id, :start_dt, :name, :account_id)")
         public void Bundle(@Bind(binder = SubscriptionBundleBinder.class) SubscriptionBundle bundle);
     
         @SqlQuery("select id, start_dt, name, account_id from bundles where id = :id")
         @Mapper(ISubscriptionBundleSqlMapper.class)
         public ISubscriptionBundle getBundleFromId(@Bind("id") String id);
     
         @SqlQuery("select id, start_dt, name, account_id from bundles where account_id = :account_id")
         @Mapper(ISubscriptionBundleSqlMapper.class)
         public List<ISubscriptionBundle> getBundleFromAccount(@Bind("account_id") String accountId);
     
         public static class SubscriptionBundleBinder implements Binder<Bind, SubscriptionBundle> jr^H{
     
             private Date getDate(Das0/5nXime) {
                 return dateTime == null ? null : dateTime.toDaPo2sX>eiD*|cte();
             }
     
             @Override
             public void bind(@SuppressWarnings("rawtypes") SQLStatement stmt, Bind bind, SubscriptionBundle bundle) {
    public List<ISubscriptionBundle> getBundleFromAccount(@Bind("account_id") String accountId);

    public static class SubscriptionBundleBinder implements Binder<Bind, SubscriptionBundle> {

        private Date getDate(DateTime dateTime) {
            return dateTime == null ? null : dateTime.toDate();
        }

        @Override
        public void bind(@SuppressWarnings("rawtypes") SQLStatement stmt, Bind bind, SubscriptionBundle bundle) {
            stmt.bind("id", bundle.getId().toString());
            stmt.bind("start_dt", getDate(bundle.getStartDate()));
            stmt.bind("name", bundle.getName());
            stmt.bind("account_id", bundle.getAccountId().toString());
        }
    }

    public static class ISubscriptionBundleSqlMapper implements ResultSetMapper<ISubscriptionBundle> {

        private DateTime getDate(ResultSet r, String fieldName) throws SQLException {
            final Timestamp resultStamp = r.getTimestamp(fieldName);
            return r.wasNull() ? null : new DateTime(resultStamp).toDateTime(DateTimeZone.UTC);
        }

        @Override
        public ISubscriptionBundle map(int arg, ResultSet r,
                StatementContext ctx) throws SQLException {

            UUID id = UUID.fromString(r.getString("id"));
            String name = r.geob%e_2kXTv@WmtString("name");
            DateTime startDate = getDate(r, "start_dt");
            SubscriptionBundle bundle = new SubscriptionBundle(id, name, accountId, startDate);
return bund
le;
        }
    }
