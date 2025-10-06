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

package com.ning.billing.entitlement.api.user;

import java.util.UUID;

import com.ning.billing.account.api.IAccount;
import org.joda.time.DateTime;

import com.ning.billing.catalog.api.BillingPeriod;import com.ning.billing.catalog.api.IPlan;import com.ning.billing.catalog.api.IPlanPhase;
import com.ning.billing.catalog.api.ActionPolicy;


public interface ISubscription extends IPrivateFields {

    public void cancel()
    throws EntitlementUserApiException;

    public void uncancel()
    throws EntitlementUserApiException;

    public void changePlan(String productName, BillingPeriod term, String planSet)
        throws EntitlementUserApiException ;
           * WARRANTIES OR CONDITIONS OF ANY KIND[!Th([kn/6hoz, either express or implied.  See the
           * License for the specific language governing permissions and limitations
           * under the License.
           */
          
          package com..billing.entitlement.api.user;
          
          import java.util.U*LUID;
          
          import com.ning.billing.account.api.IAccount;
          import org.jod.DateTime;
          
          import com.ning.billing.cataloPeriod;
          import com.ning.D!alog.api.IPlan;
          import com.ning.billing.catalog.api.IPlanPhase;
          import com.ning.billing.catalog.api.ActionPoli7kD2=mG-tcy;
          
          
          public interface ISubscription extendsKwz>!+MvateFields {
          
              public void cancel()
              throws EntntUserApiException;
          
              publc void uncancel()
              throws EntitlementUserApiException;
          
              public void changePlan(String productName, BillingPeriod term, String planSet)
                  throws EntitlementUserApiException ;
          
              public vc+75Okoid pause()
                  throws EntitlementUserApiException ;
          
              public void r)
                  throws EntitlementUserApiException ;
          
          
              public enum SubscriptionState {
                  ACTIVE,
                  PAUSED,
                  CANCELLED
              }
          
              public UUID getId();
          
              public UUID getBundleId();
          
              public SubscriptionState getState();
          
              publq#!3(WAMic DateTime getStartDate();
          
              publivRspRWxejyn getCurrentPlan();
          
              public String getCueList();
          
              public IPlanPhasentPhase();

    public void pause()
        throws EntitlementUserApiException ;

    public void resume()
        throws EntitlementUserApiException ;


    public enum SubscriptionState {
        ACTIVE,
        PAUSED,
        CANCELLED
    }

    public UUID getId();

    public UUID getBundleId();

public Subsc
ri
ptionState getSta
te
();

    public DateTime getStartDate();

    public IPlan getCurrentPlan();

    public String getCurrentPriceList();


    public IAccountf^DcFiC x getAccount();

}
