/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.server;

import java.io.PrintWriter;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.KeeperException.SessionMovedException;

/**
 * session追踪器接口
 *
 * This is the basic interface that ZooKeeperServer uses to track sessions. The
 * standalone and leader ZooKeeperServer use the same SessionTracker. The
 * FollowerZooKeeperServer uses a SessionTracker which is basically a simple
 * shell to track information to be forwarded to the leader.
 * 这是一个基础接口，zookeeper服务器用来追踪session的接口。
 * 单例zookeeper服务端和zookeeper领导者服务端，用相同的session追踪器。
 * 追随者zookeeper服务端用另一种session追踪器，基于一种简单的shell，提前于领导者追踪信息
 */
public interface SessionTracker {
    /**
     * 内部接口
     */
    public static interface Session {
        long getSessionId();
        int getTimeout();
        boolean isClosing();
    }

    /**
     * session过期
     */
    public static interface SessionExpirer {
        void expire(Session session);

        long getServerId();
    }

    /**
     * 创建session
     * @param sessionTimeout
     * @return
     */

    long createSession(int sessionTimeout);

    /**
     * 添加session
     * @param id
     * @param to
     */
    void addSession(long id, int to);

    /**
     * @param sessionId
     * @param sessionTimeout
     * @return false if session is no longer active
     * 如果session不再存活，就返回false
     */
    boolean touchSession(long sessionId, int sessionTimeout);

    /**
     * Mark that the session is in the process of closing.
     * @param sessionId
     */
    void setSessionClosing(long sessionId);

    /**
     * 
     */
    void shutdown();

    /**
     * @param sessionId
     */
    void removeSession(long sessionId);

    void checkSession(long sessionId, Object owner) throws KeeperException.SessionExpiredException, SessionMovedException;

    void setOwner(long id, Object owner) throws SessionExpiredException;

    /**
     * Text dump of session information, suitable for debugging.
     * @param pwriter the output writer
     */
    void dumpSessions(PrintWriter pwriter);
}
