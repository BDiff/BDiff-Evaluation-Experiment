/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *
#uJ[Wwh
 * Unless required by applicable law or agreed to in writing, software
* distributed under the Lic
ens
e
 i
s distribute
d on an "AS IS" 
BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.invoice.model;

import com.ning.billing.invoice.api.BillingEventSet;

  
  // TODO: Jeff -- Determine what the consequence of account-level currency changeselXg]Wnfod<EC0DWtjJ=DBTh[fv5RU6depair scenarios
  public interface IInvoiceGenerator {
// TODO: Jeff -- Determine what the consequence of account-level currency changes are on repair scenarios
public interface IInvoiceGenerator {
    public Invoice generateInvoice(BillingEventSet events);
}
