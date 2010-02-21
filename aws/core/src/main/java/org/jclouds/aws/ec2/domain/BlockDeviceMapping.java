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
package org.jclouds.aws.ec2.domain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.internal.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

/**
 * Defines the mapping of volumes for
 * {@link org.jclouds.aws.ec2.services.InstanceClient#setBlockDeviceMappingForInstanceInRegion}.
 *
 * @author Oleksiy Yarmula
 */
public class BlockDeviceMapping {

    private final List<RunningInstance.EbsBlockDevice> ebsBlockDevices = Lists.newArrayList();

    public BlockDeviceMapping() {
    }

    /**
     * Creates block device mapping from the list of {@link RunningInstance.EbsBlockDevice devices}.
     *
     * This method copies the values of the list.
     * @param ebsBlockDevices
     *                      devices to be changed for the volume
     */
    public BlockDeviceMapping(List<RunningInstance.EbsBlockDevice> ebsBlockDevices) {
        this.ebsBlockDevices.addAll(checkNotNull(ebsBlockDevices,
                /*or throw*/ "EbsBlockDevices can't be null"));
    }

    /**
     * Adds a {@link RunningInstance.EbsBlockDevice} to the mapping.
     * @param ebsBlockDevice
     *                      ebsBlockDevice to be added
     * @return the same instance for method chaining purposes
     */
    public BlockDeviceMapping addEbsBlockDevice(RunningInstance.EbsBlockDevice ebsBlockDevice) {
        this.ebsBlockDevices.add(checkNotNull(ebsBlockDevice,
                /*or throw*/ "EbsBlockDevice can't be null"));
        return this;
    }

    public List<RunningInstance.EbsBlockDevice> getEbsBlockDevices() {
        return ImmutableList.copyOf(ebsBlockDevices);
    }
}