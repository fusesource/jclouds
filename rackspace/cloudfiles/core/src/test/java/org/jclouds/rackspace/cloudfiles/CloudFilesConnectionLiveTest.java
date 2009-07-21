/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
 * ====================================================================
 */
package org.jclouds.rackspace.cloudfiles;

import static org.jclouds.rackspace.reference.RackspaceConstants.PROPERTY_RACKSPACE_KEY;
import static org.jclouds.rackspace.reference.RackspaceConstants.PROPERTY_RACKSPACE_USER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.jclouds.http.HttpResponseException;
import org.jclouds.http.HttpUtils;
import org.jclouds.http.options.GetOptions;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.rackspace.cloudfiles.domain.AccountMetadata;
import org.jclouds.rackspace.cloudfiles.domain.CFObject;
import org.jclouds.rackspace.cloudfiles.domain.ContainerMetadata;
import org.jclouds.rackspace.cloudfiles.options.ListContainerOptions;
import org.jclouds.rackspace.cloudfiles.reference.CloudFilesHeaders;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Tests behavior of {@code JaxrsAnnotationProcessor}
 * 
 * @author Adrian Cole
 */
@Test(groups = "live", testName = "cloudfiles.CloudFilesAuthenticationLiveTest")
public class CloudFilesConnectionLiveTest {

   protected static final String sysRackspaceUser = System.getProperty(PROPERTY_RACKSPACE_USER);
   protected static final String sysRackspaceKey = System.getProperty(PROPERTY_RACKSPACE_KEY);

   private String bucketPrefix = System.getProperty("user.name") + ".cfint";
   CloudFilesConnection connection;

   @BeforeGroups(groups = { "live" })
   public void setupConnection() {
      connection = CloudFilesContextBuilder.newBuilder(sysRackspaceUser, sysRackspaceKey)
               .withModule(new Log4JLoggingModule()).withJsonDebug().buildContext().getConnection();
   }

   @Test
   public void testListOwnedContainers() throws Exception {
      List<ContainerMetadata> response = connection.listOwnedContainers();
      assertNotNull(response);
      long initialContainerCount = response.size();
      assertTrue(initialContainerCount >= 0);

      // Create test containers
      String[] containerNames = new String[] { 
            bucketPrefix + ".testListOwnedContainers1",
            bucketPrefix + ".testListOwnedContainers2" };
      assertTrue(connection.putContainer(containerNames[0]));
      assertTrue(connection.putContainer(containerNames[1]));
      
      // Test default listing
      response = connection.listOwnedContainers();
      assertEquals(response.size(), initialContainerCount + 2);
      
      // Test listing with options
      response = connection.listOwnedContainers(ListContainerOptions.Builder
            .afterMarker(containerNames[0].substring(0, containerNames[0].length() - 1)) 
            .maxResults(1));
      assertEquals(response.size(), 1);
      assertEquals(response.get(0).getName(), containerNames[0]);

      response = connection.listOwnedContainers(ListContainerOptions.Builder
            .afterMarker(containerNames[0]).maxResults(1));
      assertEquals(response.size(), 1);
      assertEquals(response.get(0).getName(), containerNames[1]);

      // Cleanup and test containers have been removed
      assertTrue(connection.deleteContainerIfEmpty(containerNames[0]));
      assertTrue(connection.deleteContainerIfEmpty(containerNames[1]));
      response = connection.listOwnedContainers();
      assertEquals(response.size(), initialContainerCount);
   }

   @Test
   public void testHeadAccountMetadata() throws Exception {
      AccountMetadata metadata = connection.getAccountMetadata();
      assertNotNull(metadata);
      long initialContainerCount = metadata.getContainerCount();

      String containerName = bucketPrefix + ".testHeadAccountMetadata";
      assertTrue(connection.putContainer(containerName));

      metadata = connection.getAccountMetadata();
      assertNotNull(metadata);
      assertEquals(metadata.getContainerCount(), initialContainerCount + 1);

      assertTrue(connection.deleteContainerIfEmpty(containerName));
   }

   @Test
   public void testDeleteContainer() throws Exception {
      assertTrue(connection.deleteContainerIfEmpty("does-not-exist"));

      String containerName = bucketPrefix + ".testDeleteContainer";
      assertTrue(connection.putContainer(containerName));
      assertTrue(connection.deleteContainerIfEmpty(containerName));
   }

   @Test
   public void testPutContainers() throws Exception {
      String containerName1 = bucketPrefix + ".hello";
      assertTrue(connection.putContainer(containerName1));
      // List only the container just created, using a marker with the container name less 1 char
      List<ContainerMetadata> response = connection
               .listOwnedContainers(ListContainerOptions.Builder.afterMarker(
                        containerName1.substring(0, containerName1.length() - 1)).maxResults(1));
      assertNotNull(response);
      assertEquals(response.size(), 1);
      assertEquals(response.get(0).getName(), bucketPrefix + ".hello");

      // TODO: Contrary to the API documentation, a container can be created with '?' in the name.
      String containerName2 = bucketPrefix + "?should-be-illegal-question-char";
      connection.putContainer(containerName2);
      // List only the container just created, using a marker with the container name less 1 char
      response = connection.listOwnedContainers(ListContainerOptions.Builder.afterMarker(
               containerName2.substring(0, containerName2.length() - 1)).maxResults(1));
      assertEquals(response.size(), 1);

      // TODO: Should throw a specific exception, not UndeclaredThrowableException
      try {
         connection.putContainer(bucketPrefix + "/illegal-slash-char");
         fail("Should not be able to create container with illegal '/' character");
      } catch (Exception e) {
      }

      assertTrue(connection.deleteContainerIfEmpty(containerName1));
      assertTrue(connection.deleteContainerIfEmpty(containerName2));
   }

   @Test
   public void testObjectOperations() throws Exception {
      String containerName = bucketPrefix + ".testObjectOperations";
      String data = "Here is my data";

      assertTrue(connection.putContainer(containerName));

      // Test PUT with string data, ETag hash, and a piece of metadata
      CFObject object = new CFObject("object", data);
      object.setContentLength(data.length());
      object.generateETag();
      object.getMetadata().setContentType("text/plain");
      object.getMetadata().getUserMetadata().put(
               CloudFilesHeaders.USER_METADATA_PREFIX + "Metadata", "metadata-value");
      byte[] md5 = connection.putObject(containerName, object).get(10, TimeUnit.SECONDS);
      assertEquals(HttpUtils.toHexString(md5), 
            HttpUtils.toHexString(object.getMetadata().getETag()));
      
      // Test HEAD of missing object
      CFObject.Metadata metadata = connection.headObject(containerName, "non-existent-object");
      assertEquals(metadata, CFObject.Metadata.NOT_FOUND);
      
      // Test HEAD of object
      metadata = connection.headObject(containerName, object.getKey());
      assertEquals(metadata.getKey(), object.getKey());
      assertEquals(metadata.getSize(), data.length());
      assertEquals(metadata.getContentType(), "text/plain");
      assertEquals(metadata.getETag(), object.getMetadata().getETag());
      assertEquals(metadata.getUserMetadata().entries().size(), 1);
      // Notice the quirk where CF changes the case of returned metadata names      
      assertEquals(Iterables.getLast(metadata.getUserMetadata().get(
            (CloudFilesHeaders.USER_METADATA_PREFIX + "Metadata").toLowerCase())), 
            "metadata-value");

      // Test POST to update object's metadata
      Multimap<String, String> userMetadata = HashMultimap.create();
      userMetadata.put(CloudFilesHeaders.USER_METADATA_PREFIX + "New-Metadata-1", "value-1");
      userMetadata.put(CloudFilesHeaders.USER_METADATA_PREFIX + "New-Metadata-2", "value-2");
      assertTrue(connection.setObjectMetadata(containerName, object.getKey(), userMetadata));
      
      // Test GET of missing object
      CFObject getObject = connection.getObject(containerName, "non-existent-object")
            .get(10, TimeUnit.SECONDS);
      assertEquals(getObject, CFObject.NOT_FOUND);
      
      // Test GET of object (including updated metadata)
      getObject = connection.getObject(containerName, object.getKey()).get(120, TimeUnit.SECONDS);
      assertEquals(IOUtils.toString((InputStream)getObject.getData()), data);
      assertEquals(getObject.getKey(), object.getKey());
      assertEquals(getObject.getContentLength(), data.length());
      assertEquals(getObject.getMetadata().getContentType(), "text/plain");
      assertEquals(getObject.getMetadata().getETag(), object.getMetadata().getETag());
      assertEquals(getObject.getMetadata().getUserMetadata().entries().size(), 2);
      // Notice the quirk where CF changes the case of returned metadata names      
      assertEquals(Iterables.getLast(getObject.getMetadata().getUserMetadata().get(
            (CloudFilesHeaders.USER_METADATA_PREFIX + "New-Metadata-1").toLowerCase())), "value-1");
      assertEquals(Iterables.getLast(getObject.getMetadata().getUserMetadata().get(
            (CloudFilesHeaders.USER_METADATA_PREFIX + "New-Metadata-2").toLowerCase())), "value-2");
      
      // Test PUT with invalid ETag (as if object's data was corrupted in transit)
      String correctEtag = HttpUtils.toHexString(object.getMetadata().getETag());
      String incorrectEtag = "0" + correctEtag.substring(1);
      object.getMetadata().setETag(HttpUtils.fromHexString(incorrectEtag));
      try {
         connection.putObject(containerName, object).get(10, TimeUnit.SECONDS);
      } catch (Throwable e) {
         assertEquals(e.getCause().getClass(), HttpResponseException.class);
         assertEquals(((HttpResponseException) e.getCause()).getResponse().getStatusCode(), 422);
      }

      // Test PUT chunked/streamed upload with data of "unknown" length
      ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes("UTF-8"));
      object = new CFObject("chunked-object", bais);
      md5 = connection.putObject(containerName, object).get(10, TimeUnit.SECONDS);
      assertEquals(HttpUtils.toHexString(md5), correctEtag);
      
      // Test GET with options
      // Non-matching ETag
      try {
         connection.getObject(containerName, object.getKey(),
               GetOptions.Builder.ifETagDoesntMatch(md5)).get(120, TimeUnit.SECONDS);
      } catch (Exception e) {
         assertEquals(e.getCause().getClass(), HttpResponseException.class);
         assertEquals(((HttpResponseException) e.getCause()).getResponse().getStatusCode(), 304);         
      }
      // Matching ETag
      getObject = connection.getObject(containerName, object.getKey(),
            GetOptions.Builder.ifETagMatches(md5)).get(120, TimeUnit.SECONDS);
      assertEquals(getObject.getMetadata().getETag(), md5);
      // Range
      getObject = connection.getObject(containerName, object.getKey(),
            GetOptions.Builder.startAt(8)).get(120, TimeUnit.SECONDS);
      assertEquals(IOUtils.toString((InputStream)getObject.getData()), data.substring(8));

      assertTrue(connection.deleteObject(containerName, "object"));
      assertTrue(connection.deleteObject(containerName, "chunked-object"));
      assertTrue(connection.deleteContainerIfEmpty(containerName));
   }

}