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
package org.jclouds.cloudsigma2.sjc;

import java.net.URI;
import java.util.Properties;

import org.jclouds.cloudsigma2.CloudSigma2ApiMetadata;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;

import com.google.auto.service.AutoService;

/**
 * Implementation of {@link ProviderMetadata} for CloudSigma San Jose.
 */
@AutoService(ProviderMetadata.class)
public class CloudSigma2SanJoseProviderMetadata extends BaseProviderMetadata {

   public static Builder builder() {
      return new Builder();
   }

   @Override
   public Builder toBuilder() {
      return builder().fromProviderMetadata(this);
   }

   public CloudSigma2SanJoseProviderMetadata() {
      super(builder());
   }

   public CloudSigma2SanJoseProviderMetadata(Builder builder) {
      super(builder);
   }

   public static Properties defaultProperties() {
      Properties properties = new Properties();
      return properties;
   }

   public static class Builder
         extends
         BaseProviderMetadata.Builder {

      protected Builder() {
         id("cloudsigma2-sjc")
         .name("CloudSigma 2 San Jose")
         .apiMetadata(new CloudSigma2ApiMetadata())
         .homepage(URI.create("http://www.cloudsigma.com/en/our-cloud/features"))
         .console(URI.create("https://gui.sjc.cloudsigma.com/"))
         .iso3166Codes("US-CA")
         .endpoint("https://sjc.cloudsigma.com/api/2.0/")
         .defaultProperties(CloudSigma2SanJoseProviderMetadata.defaultProperties());
      }

      @Override
      public CloudSigma2SanJoseProviderMetadata build() {
         return new CloudSigma2SanJoseProviderMetadata(this);
      }

      @Override
      public Builder fromProviderMetadata(
            ProviderMetadata in) {
         super.fromProviderMetadata(in);
         return this;
      }

   }
}
