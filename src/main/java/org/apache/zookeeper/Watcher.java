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

package org.apache.zookeeper;

import org.apache.yetus.audience.InterfaceAudience;

/**
 * 备用知识
 * InterfaceAudience 类包含三个注解类型，用来被说明被他们注解的类型的潜在的使用范围（audience）。
 * @InterfaceAudience.Public： 对所有工程和应用可用
 * @InterfaceAudience.LimitedPrivate： 仅限于某些本项目的衍生外围项目
 * @InterfaceAudience.Private： 仅限于本项目自身
 * 这个注解知识在逻辑层面上说明，这个类可以被使用的范围，不会带有强制性，随便加，也不会报错。
 *
 * Watcher优势 通过watcher，可以避免主动轮询导致的额外负担，更加实时和有效率。
 * This interface specifies the public interface an event handler class must
 * implement. A ZooKeeper client will get various events from the ZooKeeper
 * server it connects to. An application using such a client handles these
 * events by registering a callback object with the client. The callback object
 * is expected to be an instance of a class that implements Watcher interface.
 *
 * @Reader liantengda
 * 这个借口明确规定一个事件处理类一定要实现这个接口。
 * 一个zookeeper 客户端 将会从与他连接的服务端捕捉到各种各样的事件。 一个使用这种客户端的应用，处理这些事件
 * 通过客户端注册的一个回调对象。这个回调对象应该是一个实现了这个观察者接口的类的实例。
 * 回调对象
 */
@InterfaceAudience.Public
public interface Watcher {

    /**
     * 这个接口定义了一个事件可能出现的状态
     * This interface defines the possible states an Event may represent
     */
    @InterfaceAudience.Public
    public interface Event {
        /**
         * Enumeration of states the ZooKeeper may be at the event
         *
         * KeeperState代表ZooKeeper的连接状态
         */
        @InterfaceAudience.Public
        public enum KeeperState {
            /** Unused, this state is never generated by the server */
            @Deprecated
            Unknown (-1),

            /** The client is in the disconnected state - it is not connected
             * to any server in the ensemble. */
            Disconnected (0),

            /** Unused, this state is never generated by the server */
            @Deprecated
            NoSyncConnected (1),

            /** The client is in the connected state - it is connected
             * to a server in the ensemble (one of the servers specified
             * in the host connection parameter during ZooKeeper client
             * creation). */
            SyncConnected (3),

            /**
             * Auth failed state
             */
            AuthFailed (4),

            /**
             * The client is connected to a read-only server, that is the
             * server which is not currently connected to the majority.
             * The only operations allowed after receiving this state is
             * read operations.
             * This state is generated for read-only clients only since
             * read/write clients aren't allowed to connect to r/o servers.
             */
            ConnectedReadOnly (5),

            /**
              * SaslAuthenticated: used to notify clients that they are SASL-authenticated,
              * so that they can perform Zookeeper actions with their SASL-authorized permissions.
              */
            SaslAuthenticated(6),

            /** The serving cluster has expired this session. The ZooKeeper
             * client connection (the session) is no longer valid. You must
             * create a new client connection (instantiate a new ZooKeeper
             * instance) if you with to access the ensemble. */
            Expired (-112);

            private final int intValue;     // Integer representation of value
                                            // for sending over wire

            KeeperState(int intValue) {
                this.intValue = intValue;
            }

            public int getIntValue() {
                return intValue;
            }

            public static KeeperState fromInt(int intValue) {
                switch(intValue) {
                    case   -1: return KeeperState.Unknown;
                    case    0: return KeeperState.Disconnected;
                    case    1: return KeeperState.NoSyncConnected;
                    case    3: return KeeperState.SyncConnected;
                    case    4: return KeeperState.AuthFailed;
                    case    5: return KeeperState.ConnectedReadOnly;
                    case    6: return KeeperState.SaslAuthenticated;
                    case -112: return KeeperState.Expired;

                    default:
                        throw new RuntimeException("Invalid integer value for conversion to KeeperState");
                }
            }
        }

        /**
         * Enumeration of types of events that may occur on the ZooKeeper
         *
         * EventType代表node的状态变更
         */
        @InterfaceAudience.Public
        public enum EventType {
            None (-1),
            NodeCreated (1),
            NodeDeleted (2),
            NodeDataChanged (3),
            NodeChildrenChanged (4);

            private final int intValue;     // Integer representation of value
                                            // for sending over wire

            EventType(int intValue) {
                this.intValue = intValue;
            }

            public int getIntValue() {
                return intValue;
            }

            public static EventType fromInt(int intValue) {
                switch(intValue) {
                    case -1: return EventType.None;
                    case  1: return EventType.NodeCreated;
                    case  2: return EventType.NodeDeleted;
                    case  3: return EventType.NodeDataChanged;
                    case  4: return EventType.NodeChildrenChanged;

                    default:
                        throw new RuntimeException("Invalid integer value for conversion to EventType");
                }
            }           
        }
    }

    /**
     * 当ZNode发生事件变化时，通过process(WatchedEvent event)方法调用DataMonitor的
     * process(WatchedEvent event)方法。
     * @param event
     */
    abstract public void process(WatchedEvent event);
}
