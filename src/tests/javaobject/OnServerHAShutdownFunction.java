/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javaobject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionAdapter;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.internal.cache.GemFireCacheImpl;

public class OnServerHAShutdownFunction extends FunctionAdapter implements Declarable {

  public void execute(FunctionContext context) {
    ArrayList vals = new ArrayList();
    if (context.getArguments() instanceof Boolean) {
      if (context.isPossibleDuplicate()) {
        context.getResultSender().lastResult((Boolean)context.getArguments());
      }
      else {
        stopServer();
      }
    }
    else if (context.getArguments() instanceof String) {
      String key = (String)context.getArguments();
      if (context.isPossibleDuplicate()) {
        context.getResultSender().lastResult(key);
      }
      else {
        stopServer();
      }
    }
    else if (context.getArguments() instanceof Vector) {
      Cache c = null;
      try {
        c = CacheFactory.getAnyInstance();
      }
      catch (CacheClosedException ex) {
        vals.add("NoCacheResult");
        context.getResultSender().lastResult(vals);
      }
      Region region = c.getRegion("partition_region");
      Vector keys = (Vector)context.getArguments();
      System.out.println("Context.getArguments " + keys);
      Iterator itr = keys.iterator();
      while (itr.hasNext()) {
        Object k = itr.next();
        vals.add(region.get(k));
        System.out.println("vals " + vals);
      }
    }

    if (context.isPossibleDuplicate()) {
      context.getResultSender().lastResult(vals);
    }
    else {
      stopServer();
    }

  }

  private void stopServer() {
    CacheServer server = (CacheServer)GemFireCacheImpl.getInstance()
        .getCacheServers().get(0);
    server.stop();
  }

  public String getId() {
    return "OnServerHAShutdownFunction";
  }

  public boolean optimizeForWrite() {
    return false;
  }

  public void init(Properties arg0) {

  }

}
