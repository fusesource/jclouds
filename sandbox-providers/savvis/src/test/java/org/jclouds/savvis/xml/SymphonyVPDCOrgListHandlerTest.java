/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.savvis.xml;

import static org.testng.Assert.assertEquals;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.jclouds.http.functions.BaseHandlerTest;
import org.jclouds.vcloud.VCloudExpressMediaType;
import org.jclouds.vcloud.domain.ReferenceType;
import org.jclouds.vcloud.domain.internal.ReferenceTypeImpl;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Tests behavior of {@code OrgListHandler}
 * 
 * @author Adrian Cole
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during surefire
@Test(groups = "unit", testName = "OrgListHandlerTest")
public class SymphonyVPDCOrgListHandlerTest extends BaseHandlerTest {

   public void testApplyInputStream() {
      InputStream is = getClass().getResourceAsStream("/orglist.xml");

      Map<String, ReferenceType> result = factory.create(injector.getInstance(SymphonyVPDCOrgListHandler.class)).parse(is);
      assertEquals(result, ImmutableMap.of("SAVVISStation Integration Testing", new ReferenceTypeImpl("SAVVISStation Integration Testing",
            VCloudExpressMediaType.ORG_XML, URI.create("https://api.sandbox.symphonyvpdc.savvis.net/rest/api/v0.8/org/100000.0"))));
   }
}
