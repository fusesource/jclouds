/**
 *
 * Copyright (C) 2009 Adrian Cole <adrian@jclouds.org>
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
package org.jclouds.aws.s3;

import org.jclouds.aws.s3.domain.S3Error;
import org.jclouds.http.HttpFutureCommand;
import org.jclouds.http.HttpResponse;

public class S3ResponseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HttpFutureCommand<?> command;
    private final HttpResponse response;
    private S3Error error;

    public S3ResponseException(HttpFutureCommand<?> command,
	    HttpResponse response, S3Error error) {
	super(String.format(
		"command: %1s failed with response: %2s; error from s3: %3s",
		command, response, error));
	this.command = command;
	this.response = response;
	this.setError(error);

    }

    public S3ResponseException(HttpFutureCommand<?> command,
	    HttpResponse response) {
	super(String.format("command: %1s failed with response: %2s", command,
		response));
	this.command = command;
	this.response = response;
    }

    public void setError(S3Error error) {
	this.error = error;
    }

    public S3Error getError() {
	return error;
    }

    public HttpFutureCommand<?> getCommand() {
	return command;
    }

    public HttpResponse getResponse() {
	return response;
    }

}