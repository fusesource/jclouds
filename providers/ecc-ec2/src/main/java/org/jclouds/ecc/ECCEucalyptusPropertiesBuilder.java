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

package org.jclouds.ecc;

import static org.jclouds.Constants.PROPERTY_ENDPOINT;

import java.util.Properties;

import org.jclouds.eucalyptus.EucalyptusPropertiesBuilder;

/**
 * Builds properties used in ECCEucalyptus Clients
 * 
 * @author Adrian Cole
 */
public class ECCEucalyptusPropertiesBuilder extends EucalyptusPropertiesBuilder {
   @Override
   protected Properties defaultProperties() {
      Properties properties = super.defaultProperties();
      properties.setProperty(PROPERTY_ENDPOINT, "http://173.205.188.130:8773/services/Eucalyptus");
      return properties;
   }

   public ECCEucalyptusPropertiesBuilder(Properties properties) {
      super(properties);
   }

}
