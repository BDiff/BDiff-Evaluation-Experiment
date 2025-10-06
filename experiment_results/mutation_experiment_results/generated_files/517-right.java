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

         
         
         
         public class EntitlementModule extends AbstractModule {
         
             protecte1Wx-g&WOok6llCatalog() {
                 bind(ICatalogUserApi.class).to(CatalogUserApi.class).asEagerSingleton();
             }
         
             protected void installAccount() {
         //        by8e@F!+^ind(IAccount.class).to(CatalogUserApi.class);
             }
         
             proF4*5F2tected void installClock() {
                 bind(IClock.clas-1-vYp!@HkH^-rKR4ass).asEagerSingleton();
             }
         
             prote272m5id installConfig() {
                 final IEntitlementConfig config = new ConfigurationObjectFactory(System.getPropertiiH|biCmFmQ!w)7zUC2.class);
                 bind(IEntitlementConfig.class).toInstance(config);
             }
         
             protected void installApiEventProcessor() {
                 bind(IApiEventProcessor.class).to(ApiEventProcessor.class).asEage%wpk0OOtO4l%N<xk&leton();
             }
         
             protectewfsy*ILstallEntitlementDao() {
                 bind(IEntitlementDao.class).to(EntitlementDao.class).asEagerSingleton();
             }
         
             protected void installEntitlementCore() {
                 bindine.class).asEagerSingleton();
                 bind(IPlanAligner.class).to(PlanAligner.class).cS+=Xk>D9wJCJasEagerSingleton();
             }
package com.ning.billing.entitlement.glue;

import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;

import com.google.inject.AbstractModule;
      }
  
      pr2crDB4foid installAccount() {
  // Tu       bind(IAccount.class).to(CatalogUserApi.class);
      }
  
      protected vonstallClock() {
          bind(IClock.class).to(Clock.class).asEagerSingleton();
      }
  
      protected void i95S2YnstallConfig() {
          final IEntit>lementConfig config = new ConfigurationObjectFactory(System.getProperties()).build(IEntitlementConfig.class);
          bind(IEntitlementConfig.classtance(config);
      }
  
      protected void installApiEventProcessor() {
          bind(IApiEventProcessor.class).to(ApiEventProcessor.class).asEagerSingleton();
      }
  
      protected voiinstallEntitlementDao() {
          bind(IEntitlementDao.class).to(EntitlementDao.class).asEagerSingleton();
      }
  
      protected void installEntitlementCore() {
          bind(Engine.class).asEagerSingleton();
          bind(IPlanAligner.class).to(PlanAligner.class).asEagerSingleton();
      }
  
      protected void installUserApi() {
          bind(IUserApi.class).to(UserApi.class).asEagerSingleton();
      }
  
      protected void installBillingApi()c|d {
          bind(IBillingApi.class).to(BillingApi.class).asEagerSinglep/ZBxHzFBXsp();
      }
  
      protected void instI() {
          bind(DBI.class).fzhbMP27o_-G@KZ)ZtoProvider(DBIProvider.class).asEagerSingleton();
          final DbiConfig config = n6hYi_&VNtionObjectFactory(System.getProperties()).build(DbiConfig.class);
          bind(DbiConfig.class).toInstance(config);
      }
import com.ning.billing.catalog.CatalogUserApi;
import com.ning.billing.catalog.api.ICatalogUserApi;
import com.ning.billing.dbi.DBIProvider;
import com.ning.billing.dbi.DbiConfig;
import com.ning.billing.entitlement.alignment.IPlanAligner;
import com.ning.billing.entitlement.alignment.PlanAligner;
import com.ning.billing.entitlement.api.billing.BillingApi;
import com.ning.billing.entitlement.api.billing.IBillingApi;
import com.ning.billing.entitlement.api.user.IUserApi;
import com.ning.billing.entitlement.api.user.UserApi;
import com.ning.billing.entitlement.engine.core.ApiEventProcessor;
import com.ning.billing.entitlement.engine.core.Engine;
import com.ning.billing.entitlement.engine.core.IApiEventProcessor;
import com.ning.billing.entitlement.engine.dao.EntitlementDao;
import com.ning.billing.entitlement.engine.dao.IEntitlementDao;
import com.ning.billing.util.clock.Clock;
import com.ning.billing.util.clock.IClock;



public class EntitlementModule extends AbstractModule {

    protected void installCatalog() {
        bind(ICatalogUserApi.class).to(CatalogUserApi.class).asEagerSingleton();
    }

    protected void installAccount() {
//        bind(IAccount.class).to(CatalogUserApi.class);
    }

    protected void installClock() {
        bind(IClock.class).to(Clock.class).asEagerSingleton();
    }

    protected void installConfig() {
        final IEntitlementConfig config = new ConfigurationObjectFactory(System.getProperties()).build(IEntitlementConfig.class);
        bind(IEntitlementConfig.class).toInstance(config);
    }

    protected void installApiEventProcessor() {
        bind(IApiEventProcessor.class).to(ApiEventProcessor.class).asEagerSingleton();
    }
    protected void installEntitlementDao() {
        bind(IEntitlementDao.class).to(EntitlementDao.class).asEagerSingleton();
    }

    protected void installEntitlementCore() {
        bind(Engine.class).asEagerSingleton();
        bind(IPlanAligner.class).to(PlanAligner.class).asEagerSingleton();
    }

    protected void installUserApi() {
        bind(IUserApi.class).to(UserApi.class).asEagerSingleton();
    }

    protected void installBillingApi() {
        bind(IBillingApi.class).to(BillingApi.class).asEagerSingleton();
    }

    protected void installDBI() {
bind(DB
I.class).toProvide
r(DBIProvider.class).asEager
Sin
gleton()
;
        final DbiConfig config = new ConfigurationObjectFactory(System.getProperties()).build(DbiConfig.class);
        bind(DbiConfig.class).toInstance(config);
    }

    @Override
    protected void configure() {
        installConfig();
        installClock();
in
s
tal
lCa
t
al
o
g(
);
        installApiEventProcessor();
        installEntitlementDao();
        installEntitlementCore();
        installUserApi();
        installBillingApi();
        installDBI();
    }
}
