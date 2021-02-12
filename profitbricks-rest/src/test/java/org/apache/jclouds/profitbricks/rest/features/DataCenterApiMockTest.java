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
package org.apache.jclouds.profitbricks.rest.features;

import okhttp3.mockwebserver.MockResponse;

import java.net.URI;
import java.util.List;
import org.apache.jclouds.profitbricks.rest.domain.DataCenter;
import org.apache.jclouds.profitbricks.rest.domain.Location;
import org.apache.jclouds.profitbricks.rest.domain.options.DepthOptions;
import org.apache.jclouds.profitbricks.rest.internal.BaseProfitBricksApiMockTest;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "DataCenterApiMockTest", singleThreaded = true)
public class DataCenterApiMockTest extends BaseProfitBricksApiMockTest {

   @Test
   public void testGetList() throws InterruptedException {
      server.enqueue(
              new MockResponse().setBody(stringFromResource("/datacenter/list.json"))
      );

      List<DataCenter> list = dataCenterApi().list();

      assertNotNull(list);
      assertEquals(list.size(), 3);
      assertEquals(list.get(0).properties().name(), "vea");

      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "GET", "/datacenters");
   }

   @Test
   public void testGetListWith404() throws InterruptedException {
      server.enqueue(response404());
      List<DataCenter> list = dataCenterApi().list(new DepthOptions().depth(1));
      assertTrue(list.isEmpty());
      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "GET", "/datacenters?depth=1");
   }

   @Test
   public void testGetDataCenter() throws InterruptedException {
      MockResponse response = new MockResponse();
      response.setBody(stringFromResource("/datacenter/get.json"));
      response.setHeader("Content-Type", "application/json");

      server.enqueue(response);

      DataCenter dataCenter = dataCenterApi().getDataCenter("some-id");

      assertNotNull(dataCenter);
      assertEquals(dataCenter.properties().name(), "docker");
      assertEquals(dataCenter.properties().location(), Location.US_LAS);

      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "GET", "/datacenters/some-id");
   }

   public void testGetDataCenterWith404() throws InterruptedException {
      server.enqueue(response404());

      DataCenter dataCenter = dataCenterApi().getDataCenter("some-id");

      assertNull(dataCenter);

      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "GET", "/datacenters/some-id");
   }

   @Test
   public void testCreate() throws InterruptedException {
      server.enqueue(
              new MockResponse().setBody(stringFromResource("/datacenter/get.json"))
                 .addHeader("Location", "http://foo/bar")
      );

      DataCenter dataCenter = dataCenterApi().create("test-data-center", "example description", Location.US_LAS.getId());

      assertNotNull(dataCenter);
      assertNotNull(dataCenter.id());
      assertEquals(dataCenter.requestStatusUri().get(), URI.create("http://foo/bar"));

      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "POST", "/datacenters",
              "{\"properties\": {\"name\": \"test-data-center\", \"description\": \"example description\",\"location\": \"us/las\"}}"
      );
   }

   @Test
   public void testUpdate() throws InterruptedException {
      server.enqueue(
            new MockResponse().setBody(stringFromResource("/datacenter/get.json"))
      );

      dataCenterApi().update("some-id", "new name");

      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "PATCH", "/datacenters/some-id", "{\"name\": \"new name\"}");
   }

   @Test
   public void testDelete() throws InterruptedException {
      server.enqueue(response204().addHeader("Location", "http://foo/bar"));

      URI statusURI = dataCenterApi().delete("some-id");
      
      assertEquals(statusURI, URI.create("http://foo/bar"));
      assertEquals(server.getRequestCount(), 1);
      assertSent(server, "DELETE", "/datacenters/some-id");
   }

   @Test
   public void testDepth() throws InterruptedException {

      for (int i = 1; i <= 5; ++i) {
         server.enqueue(
                 new MockResponse().setBody(
                         stringFromResource(String.format("/datacenter/get-depth-%d.json", i))
                 )
         );
         DataCenter dataCenter = dataCenterApi().getDataCenter("some-id", new DepthOptions().depth(i));
         assertNotNull(dataCenter);
         assertEquals(server.getRequestCount(), i);
         assertSent(server, "GET", "/datacenters/some-id?depth=" + i);
      }
   }

   private DataCenterApi dataCenterApi() {
      return api.dataCenterApi();
   }

}
