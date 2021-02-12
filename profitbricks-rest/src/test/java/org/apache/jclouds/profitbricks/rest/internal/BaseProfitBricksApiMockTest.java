/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jclouds.profitbricks.rest.internal;

import static com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.jclouds.profitbricks.rest.ProfitBricksApi;
import org.apache.jclouds.profitbricks.rest.ProfitBricksProviderMetadata;
import org.jclouds.ContextBuilder;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.http.filters.BasicAuthentication;
import org.jclouds.rest.ApiContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.google.gson.JsonParser;
import com.google.inject.Module;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class BaseProfitBricksApiMockTest {

   protected static final String authHeader = BasicAuthentication.basic("username", "password");
   private static final String DEFAULT_ENDPOINT = new ProfitBricksProviderMetadata().getEndpoint();

   private final Set<Module> modules = ImmutableSet.<Module>of(new ExecutorServiceModule(newDirectExecutorService()));

   protected MockWebServer server;
   protected ProfitBricksApi api;

   // So that we can ignore formatting.
   private final JsonParser parser = new JsonParser();

   @BeforeMethod
   public void start() throws IOException {
      server = new MockWebServer();
      server.start();
      ApiContext<ProfitBricksApi> ctx = ContextBuilder.newBuilder("profitbricks-rest")
	      .credentials("username", "password")
	      .endpoint(url(""))
	      .modules(modules)
	      .overrides(overrides())
	      .build();
      api = ctx.getApi();
   }

   @AfterMethod(alwaysRun = true)
   public void stop() throws IOException {
      server.shutdown();
      api.close();
   }

   protected Properties overrides() {
      return new Properties();
   }

   protected String url(String path) {
      return server.url(path).toString();
   }
   
   protected MockResponse response204() {
      return new MockResponse().setStatus("HTTP/1.1 204 No Content");
   }
   
   protected MockResponse response404() {
      return new MockResponse().setStatus("HTTP/1.1 404 Not Found");
   }
   
   protected String stringFromResource(String resourceName) {
      try {
         return Resources.toString(getClass().getResource(resourceName), Charsets.UTF_8)
            .replace(DEFAULT_ENDPOINT, url(""));
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

   protected RecordedRequest assertSent(MockWebServer server, String method, String path) throws InterruptedException {
      
      RecordedRequest request = server.takeRequest();
      
      assertEquals(request.getMethod(), method);
      assertEquals(request.getPath(), path);
      assertEquals(request.getHeader("Authorization"), authHeader);
      return request;
   }

   protected RecordedRequest assertSent(MockWebServer server, String method, String path, String json)
	   throws InterruptedException {
      RecordedRequest request = assertSent(server, method, path);
      
      String expectedContentType = "application/json";
      
      if (request.getMethod().equals("PATCH"))
         expectedContentType = "application/json";
      
      assertEquals(request.getHeader("Content-Type"), expectedContentType);
      assertEquals(parser.parse(request.getBody().readUtf8()), parser.parse(json));
      return request;
   }
}
