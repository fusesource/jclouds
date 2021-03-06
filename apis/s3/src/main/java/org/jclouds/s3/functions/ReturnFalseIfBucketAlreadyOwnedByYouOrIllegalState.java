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

package org.jclouds.s3.functions;

import static org.jclouds.util.Throwables2.propagateOrNull;

import javax.inject.Singleton;

import org.jclouds.aws.AWSResponseException;
import org.jclouds.util.Throwables2;

import com.google.common.base.Function;

/**
 * 
 * @author Adrian Cole
 */
@Singleton
public class ReturnFalseIfBucketAlreadyOwnedByYouOrIllegalState implements Function<Exception, Boolean> {

   public Boolean apply(Exception from) {
      AWSResponseException exception = Throwables2.getFirstThrowableOfType(from, AWSResponseException.class);
      if (exception != null && exception.getError() != null
            && exception.getError().getCode().equals("BucketAlreadyOwnedByYou")) {
         return false;
      } else if (Throwables2.getFirstThrowableOfType(from, IllegalStateException.class) != null) {
         return false;
      }
      return Boolean.class.cast(propagateOrNull(from));
   }
}
