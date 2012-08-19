/*
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.vcloud.director.v1_5.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.net.URI;

import org.jclouds.vcloud.director.v1_5.VCloudDirectorException;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.domain.Error;
import org.jclouds.vcloud.director.v1_5.domain.Link;
import org.jclouds.vcloud.director.v1_5.domain.Metadata;
import org.jclouds.vcloud.director.v1_5.domain.MetadataEntry;
import org.jclouds.vcloud.director.v1_5.domain.MetadataValue;
import org.jclouds.vcloud.director.v1_5.domain.Reference;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgList;
import org.jclouds.vcloud.director.v1_5.internal.VCloudDirectorAdminApiExpectTest;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Allows us to test the {@link OrgApi} via its side effects.
 * 
 * @author Adrian Cole
 */
@Test(groups = { "unit", "user" }, singleThreaded = true, testName = "OrgApiExpectTest")
public class OrgApiExpectTest extends VCloudDirectorAdminApiExpectTest {

   @Test
   public void testGetOrgList() {
      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse, 
            getStandardRequest("GET", "/org/"),
            getStandardPayloadResponse("/org/orglist.xml", VCloudDirectorMediaType.ORG_LIST));

      OrgList expected = OrgList.builder()
            .org(Reference.builder()
               .type("application/vnd.vmware.vcloud.org+xml")
               .name("JClouds")
               .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0"))
               .build())
            .build();

      assertEquals(api.getOrgApi().getOrgList(), expected);
   }

   @Test
   public void testGetOrgFromOrgListReference() {
      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse, 
            getStandardRequest("GET", "/org/"),
            getStandardPayloadResponse("/org/orglist.xml", VCloudDirectorMediaType.ORG_LIST));

      Reference org = Iterables.getOnlyElement(api.getOrgApi().getOrgList());
      
      api = requestsSendResponses(loginRequest, sessionResponse, 
            getStandardRequest("GET", org.getHref()),
            getStandardPayloadResponse("/org/org.xml", VCloudDirectorMediaType.ORG));

      Org expected = org();

      assertEquals(api.getOrgApi().getOrg(org.getHref()), expected);
   }

   @Test
   public void testGetOrg() {
      URI orgUri = URI.create(endpoint + "/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0");

      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse, 
            getStandardRequest("GET", "/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0"),
            getStandardPayloadResponse("/org/org.xml", VCloudDirectorMediaType.ORG));
      
      Org expected = org();

      assertEquals(api.getOrgApi().getOrg(orgUri), expected);
   }

   @Test
   public void testGetOrgFailOnInvalidOrgId() {
      URI orgUri = URI.create(endpoint + "/org/NOTAUUID");

      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse,
            getStandardRequest("GET", "/org/NOTAUUID"),
            getStandardPayloadResponse(400, "/org/error400.xml", VCloudDirectorMediaType.ERROR));

      Error expected = Error.builder()
            .message("validation error on field 'id': String value has invalid format or length")
            .majorErrorCode(400)
            .minorErrorCode("BAD_REQUEST")
            .build();

      try {
         api.getOrgApi().getOrg(orgUri);
         fail("Should give HTTP 400 error");
      } catch (VCloudDirectorException vde) {
         assertEquals(vde.getError(), expected);
      } catch (Exception e) {
         fail("Should have thrown a VCloudDirectorException");
      }
   }

   @Test
   public void testGetOrgFailOnWrongOrgId() {
      URI orgUri = URI.create(endpoint + "/org/9e08c2f6-077a-42ce-bece-d5332e2ebb5c");

      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse,
            getStandardRequest("GET", "/org/9e08c2f6-077a-42ce-bece-d5332e2ebb5c"),
            getStandardPayloadResponse(403, "/org/error403-catalog.xml", VCloudDirectorMediaType.ERROR));

      assertNull(api.getOrgApi().getOrg(orgUri));
   }

   @Test
   public void testGetOrgFailOnFakeOrgId() {
      URI orgUri = URI.create(endpoint + "/org/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse,
            getStandardRequest("GET", "/org/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
            getStandardPayloadResponse(403, "/org/error403-fake.xml", VCloudDirectorMediaType.ERROR));

      assertNull(api.getOrgApi().getOrg(orgUri));
   }
   
   @Test
   public void testGetOrgMetadata() {
      URI orgUri = URI.create(endpoint + "/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0");
      
      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse, 
            new VcloudHttpRequestPrimer()
               .apiCommand("GET", "/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata")
               .acceptAnyMedia()
               .httpRequestBuilder().build(), 
            new VcloudHttpResponsePrimer()
               .xmlFilePayload("/org/orgMetadata.xml", VCloudDirectorMediaType.METADATA)
               .httpResponseBuilder().build());
      
      Metadata expected = Metadata.builder()
            .type("application/vnd.vmware.vcloud.metadata+xml")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata"))
            .link(Link.builder()
                  .rel("up")
                  .type("application/vnd.vmware.vcloud.org+xml")
                  .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0"))
                  .build())
            .entries(ImmutableSet.of(metadataEntry()))
            .build();
 
       assertEquals(api.getOrgApi().getMetadataApi().getMetadata(orgUri), expected);
   }
   
   @Test
   public void testGetOrgMetadataValue() {
      URI orgUri = URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0");
      
      VCloudDirectorApi api = requestsSendResponses(loginRequest, sessionResponse,
            new VcloudHttpRequestPrimer()
               .apiCommand("GET", "/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata/KEY")
               .acceptAnyMedia()
               .httpRequestBuilder().build(), 
            new VcloudHttpResponsePrimer()
               .xmlFilePayload("/org/orgMetadataValue.xml", VCloudDirectorMediaType.METADATA_VALUE)
               .httpResponseBuilder().build());
      
      MetadataValue expected = metadataValue();

      assertEquals(api.getOrgApi().getMetadataApi().getMetadataValue(orgUri, "KEY"), expected);
   }

   public static Org org() {
      return Org.builder()
         .name("JClouds")
         .description("")
         .fullName("JClouds")
         .id("urn:vcloud:org:6f312e42-cd2b-488d-a2bb-97519cd57ed0")
         .type("application/vnd.vmware.vcloud.org+xml")
         .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0"))
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.vdc+xml")
            .name("Cluster01-JClouds")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/vdc/d16d333b-e3c0-4176-845d-a5ee6392df07"))
            .build())
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.tasksList+xml")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/tasksList/6f312e42-cd2b-488d-a2bb-97519cd57ed0"))
            .build())
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.catalog+xml")
            .name("Public")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/catalog/7212e451-76e1-4631-b2de-ba1dfd8080e4"))
            .build())
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.controlAccess+xml")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/catalog/9e08c2f6-077a-42ce-bece-d5332e2ebb5c/controlAccess/"))
            .build())
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.orgNetwork+xml")
            .name("ilsolation01-Jclouds")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/network/f3ba8256-6f48-4512-aad6-600e85b4dc38"))
            .build())
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.orgNetwork+xml")
            .name("internet01-Jclouds")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/network/55a677cf-ab3f-48ae-b880-fab90421980c"))
            .build())
         .link(Link.builder()
            .rel("down")
            .type("application/vnd.vmware.vcloud.metadata+xml")
            .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata"))
            .build())
         .build();
   }
   
   public static MetadataEntry metadataEntry() {
      return MetadataEntry.builder()
            .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata/KEY"))
            .link(Link.builder()
                  .rel("up")
                  .type("application/vnd.vmware.vcloud.metadata+xml")
                  .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata"))
                  .build())
            .entry("KEY", "VALUE")
            .build();
   }
   
   public static MetadataValue metadataValue() {
      return MetadataValue.builder()
            .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata/KEY"))
            .link(Link.builder()
                  .rel("up")
                  .type("application/vnd.vmware.vcloud.metadata+xml")
                  .href(URI.create("https://vcloudbeta.bluelock.com/api/org/6f312e42-cd2b-488d-a2bb-97519cd57ed0/metadata"))
                  .build())
            .value("VALUE")
            .build();
   }
}
