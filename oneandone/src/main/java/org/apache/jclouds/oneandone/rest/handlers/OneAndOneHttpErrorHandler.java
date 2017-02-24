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
package org.apache.jclouds.oneandone.rest.handlers;

import java.io.IOException;
import javax.inject.Singleton;
import org.apache.jclouds.oneandone.rest.exceptions.OneAndOneRateLimitExceededException;
import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.InsufficientResourcesException;
import org.jclouds.rest.ResourceNotFoundException;
import static org.jclouds.util.Closeables2.closeQuietly;
import org.jclouds.util.Strings2;

@Singleton
public class OneAndOneHttpErrorHandler implements HttpErrorHandler {

   @Override
   public void handleError(final HttpCommand command, final HttpResponse response) {
      Exception exception = null;
      try {
         switch (response.getStatusCode()) {
            case 400:
            case 405:
               exception = new IllegalArgumentException(response.getMessage(), exception);
               break;
            case 401:
               exception = new AuthorizationException("This request requires authentication.", exception);
               break;
            case 402:
            case 409:
               exception = new IllegalStateException(response.getMessage(), exception);
               break;
            case 404:
            case 410:
               if (!command.getCurrentRequest().getMethod().equals("DELETE")) {
                  exception = new ResourceNotFoundException(response.getMessage(), exception);
               }
               break;
            case 413:
            case 429:
               exception = new OneAndOneRateLimitExceededException(response);
               break;
            case 503:
               // if nothing (default message was OK) was parsed from command executor, assume it was an 503 (Maintenance) html response.
               if (response.getMessage().equals("OK")) {
                  exception = new HttpResponseException("The OneAndOne team is currently carrying out maintenance.", command, response);
               } else {
                  exception = new InsufficientResourcesException(response.getMessage(), exception);
               }
               break;
            default:
               String message = parseMessage(response);
               exception = message == null ? new HttpResponseException(command, response) : new HttpResponseException(
                       command, response, message);
               break;
         }
      } finally {
         closeQuietly(response.getPayload());
         command.setException(exception);
      }
   }

   public String parseMessage(final HttpResponse response) {
      if (response.getPayload() == null) {
         return null;
      }
      try {
         return Strings2.toStringAndClose(response.getPayload().openStream());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
