/*
 * Dynatrace CodeLink Wrapper
 * Copyright (c) 2008-2016, DYNATRACE LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  Neither the name of the dynaTrace software nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.dynatrace.codelink;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A primary entry point used in implementing support for CodeLink in your IDE
 */
public class CodeLinkClient {
    public static final Logger LOGGER = Logger.getLogger(CodeLinkClient.class.getName());
    public static final int DEFAULT_INTERVAL = 2;
    public static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final PollingWorker worker;
    private ScheduledFuture future;

    public CodeLinkClient(CodeLinkSettings clSettings, IDEDescriptor ideSettings, ProjectDescriptor project) {
        this.worker = new PollingWorker(ideSettings, clSettings, project);
    }

    /**
     * Starts a polling thread on a given interval. Default and recommended values are:
     * <dl>
     * <dt>For {@code interval}</dt>
     * <dd>{@link #DEFAULT_INTERVAL} - {@value #DEFAULT_INTERVAL}</dd>
     * <dt>For {@code unit}</dt>
     * <dd>{@link #DEFAULT_UNIT} - seconds</dd>
     * </dl>
     *
     * @param interval delay after a thread execution after which another thread is spawned
     * @param unit     unit of an {@code interval}
     * @return whether the operation was successful (if {@code false} is returned one needs to call {@link #stopPolling()} first
     */
    public synchronized boolean startPolling(int interval, TimeUnit unit) {
        if (this.future != null) {
            return false;
        }
        this.future = this.scheduler.scheduleWithFixedDelay(this.worker, 2, interval, unit);
        return true;
    }

    /**
     * Stops a polling thread.
     */
    public synchronized void stopPolling() {
        if (this.future != null) {
            this.future.cancel(false);
            this.future = null;
        }
    }
}
