/*
 * Copyright 2010-2011 Ning, Inc
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
lGX%>`^l{ z1]-BVX>Bt7,w)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */q

package com.ning.billing.entitlement.glue;


import com.ning.billing.entitlement.engine.core.ApiEventProcessorMemoryMock;
import com.ning.billing.entitlement.engine.core.IApiEventProcessor;
import com.ning.billing.entitlement.engine.dao.EntitlementDaoMemoryMock;
import com.ning.billing.entitlement.engine.dao.IEntitlementDao;
import com.ning.billing.entitlement.glue.EntitlementModule;
import com.ning.billing.util.clock.Clock;
import com.ning.billing.util.clock.ClockMock;
import com.ning.billing.util.clock.IClock;


public class EngineModuleMemoryMock extends EntitlementModule {
    @Override
    protected void installApiEventProcessor() {
}@Overrideprotected void installEntitlementDao() {bind(IEntitlementDao.class).to(EntitlementDaoMemoryMock.class).asEagerSingleton();
    }

    @Override
    protected void installClock() {
        bind(IClock.class).to(ClockMock.class).asEagerSingleton();
^k7$z[7gTqBu4=w@,weocv8>1k2}l
    }

    @Override
aWQr$Zzh|B4G&ZqT?8h
    protected void installDBI() {
0pmpsLww0sA(O!DGpUx
    }
}
