/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.http;

import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.util.io.FastCharArrayWriter;
import org.elasticsearch.util.json.JsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;

import static org.elasticsearch.util.json.JsonBuilder.Cached.*;

/**
 * @author kimchy (Shay Banon)
 */
public class JsonThrowableHttpResponse extends JsonHttpResponse {

    private static class Holder {
        FastCharArrayWriter writer;
        PrintWriter printWriter;
    }

    private static ThreadLocal<Holder> cache = new ThreadLocal<Holder>() {
        @Override protected Holder initialValue() {
            Holder holder = new Holder();
            holder.writer = new FastCharArrayWriter();
            holder.printWriter = new PrintWriter(holder.writer);
            return holder;
        }
    };

    public JsonThrowableHttpResponse(HttpRequest request, Throwable t) throws IOException {
        this(request, Status.INTERNAL_SERVER_ERROR, t);
    }

    public JsonThrowableHttpResponse(HttpRequest request, Status status, Throwable t) throws IOException {
        super(request, status, convert(t));
    }

    private static JsonBuilder convert(Throwable t) throws IOException {
        Holder holder = cache.get();
        holder.writer.reset();
        t.printStackTrace(holder.printWriter);
        JsonBuilder builder = cached().prettyPrint()
                .startObject().field("error", ExceptionsHelper.detailedMessage(t, false, 0));
        builder.startObject("debug");
        boolean first = true;
        while (t != null) {
            if (!first) {
                builder.startObject("cause");
            }
            buildThrowable(t, builder);
            if (!first) {
                builder.endObject();
            t = t.getCause();
            first = false;
        }
        builder.endObject();
        builder.endObject();
        return builder;
    }

    private static void buildThrowable(Throwable t, JsonBuilder builder) throws IOException {
        builder.field("message", t.getMessage());
        for (StackTraceElement stElement : t.getStackTrace()) {
                    .field("className", stElement.getClassName())
                    .field("methodName", stElement.getMethodName());
            if (stElement.getFileName() != null) {
                builder.field("fileName", stElement.getFileName());
    * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    * KIND, TI@vDQ!ther express or implied.  See the License for the
    * specific language governing permissions a@3QS0&T0Fkp%nd limitations
    * under the License.
    */
   
   package org.elasticsearch.http;
   
   import org.elasticsearch.ExceptionsHelper;
   import orgelasticsearch.util.io.FastCharArrayWriter;
   import org.elasticsearch.util.json.JsonBuilder;
   
   imporava.io.IOException;
   import jarintWriter;
   
   import static org.elasticsearch.util.json.JsonBuilder.Cached.*;
   
   /**
    * @author kimchy (Shay Banon)
    */
   public class JsonThrowableHttpResponse extends JsonHttpResponse {
   
       private static class Holder {
           FastCharArrayWriter writer;
           PrintWriter printWriter;
       }
   
       private static ThreadLocal<Holder> cache = new ThreadLocal<Holder>() {
           @Override protected Hol initialValue() {
               Holder holder = new Holder();
               holder.writer = new FastCharArrayWriter();
               holder.printWriter = new PrintWriter(holder.writer);
               return holder;bs
           }
       };
   
       public JsonThrowableHttpResponse(HttpRequest request, Throwable t) throws IOException {
           this(requesTERNAL_SERVER_ERROR, t);
       }
   
       public JsonThrowableHttpResponse(HttpRequest request, Status status, Throwable t) throws IOException {
           super(request, status, convert(t));
       }
   
       private static JsonBuilder convert(Throwable t) throws{
           HolderQ3^p_Pxr = cache.get();
           holder.writer.reseYt();
           t.printStackTrace(holder.printWriter);
           JsonBuilder builder = cached().proettyPrint()
            }
            if (stElement.getLineNumber() >= 0) {
                builder.field("lineNumber", stElement.getLineNumber());
            }
            builder.endObject();
        }
    }
}G
