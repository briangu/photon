/*
 * Copyright 2010 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.viper.app.ws;


import java.util.Set;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import io.viper.net.common.HttpResponseLoggingHandler;
import io.viper.net.server.ws.WebSocketServerHandler;

import static org.jboss.netty.channel.Channels.pipeline;


/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class WebSocketServerPipelineFactory implements ChannelPipelineFactory
{

  Set<ChannelHandlerContext> _listeners;

  private final int maxContentLength;

  public WebSocketServerPipelineFactory(
    int maxContentLength,
    Set<ChannelHandlerContext> listeners)
  {
    _listeners = listeners;
    this.maxContentLength = maxContentLength;
  }

  public ChannelPipeline getPipeline()
    throws Exception
  {

    ChannelPipeline pipeline = pipeline();
    pipeline.addLast("log", new HttpResponseLoggingHandler());
    pipeline.addLast("decoder", new HttpRequestDecoder());
    pipeline.addLast("aggregator", new HttpChunkAggregator(maxContentLength));
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("handler", new WebSocketServerHandler(_listeners));

    return pipeline;
  }
}
