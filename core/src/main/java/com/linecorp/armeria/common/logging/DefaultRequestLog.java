/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.common.logging;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;

import com.linecorp.armeria.common.Scheme;
import com.linecorp.armeria.common.SerializationFormat;
import com.linecorp.armeria.common.SessionProtocol;

import io.netty.channel.Channel;
import io.netty.util.Attribute;

/**
 * Default {@link RequestLog} implementation.
 */
public final class DefaultRequestLog
        extends AbstractMessageLog<RequestLog> implements RequestLog, RequestLogBuilder {

    private long startTimeMillis;
    private Channel channel;
    private SessionProtocol sessionProtocol;
    private SerializationFormat serializationFormat = SerializationFormat.NONE;
    private String host;
    private String method;
    private String path;

    @Override
    public void start(Channel channel, SessionProtocol sessionProtocol,
                      String host, String method, String path) {

        requireNonNull(channel, "channel");
        requireNonNull(sessionProtocol, "sessionProtocol");
        requireNonNull(host, "host");
        requireNonNull(method, "method");
        requireNonNull(path, "path");

        if (!start0()) {
            return;
        }

        this.channel = channel;
        this.sessionProtocol = sessionProtocol;
        this.host = host;
        this.method = method;
        this.path = path;
    }

    @Override
    protected boolean setStartTime() {
        boolean isSet = super.setStartTime();
        if (isSet) {
            startTimeMillis = System.currentTimeMillis();
        }
        return isSet;
    }

    @Override
    public long startTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public void serializationFormat(SerializationFormat serializationFormat) {
        requireNonNull(serializationFormat, "serializationFormat");
        if (isDone()) {
            return;
        }
        this.serializationFormat = serializationFormat;
    }

    @Override
    public Scheme scheme() {
        return Scheme.of(serializationFormat, sessionProtocol);
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    protected void appendProperties(StringBuilder buf) {
        buf.append(", scheme=")
           .append(serializationFormat != null ? serializationFormat.uriText() : null)
           .append('+')
           .append(sessionProtocol != null ? sessionProtocol.uriText() : null)
           .append(", host=")
           .append(host)
           .append(", method=")
           .append(method)
           .append(", path=")
           .append(path);
    }

    @Override
    CompletableFuture<?> parentLogFuture() {
        return null;
    }

    @Override
    boolean includeAttr(Attribute<?> attr) {
        return attr.key() != RAW_RPC_REQUEST;
    }
}
