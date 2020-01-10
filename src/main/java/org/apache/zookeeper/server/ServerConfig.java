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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.yetus.audience.InterfaceAudience;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

/**
 * Server configuration storage.
 *
 * We use this instead of Properties as it's typed.
 *  服务端注册文件存储类
 *  因为这个配置是定制的所以不用properties。
 */
@InterfaceAudience.Public
public class ServerConfig {
    //// 如果你要更新这个配置文件参数，务必要更新下配置中的这 四个 单词
    //// If you update the configuration parameters be sure
    //// to update the "conf" 4letter word
    /*
    备用知识
    InetAddress:类的主要作用是封装IP及DNS，因为这个类没有构造器，所以我们要用他的一些方法来获得对象常用的有
    1、使用getLocalHost方法为InetAddress创建对象；
    2、根据域名得到InetAddress对象
    3、根据ip得到InetAddress对象
    InetSocketAddress：类主要作用是封装端口 他是在在InetAddress基础上加端口，但它是有构造器的。
    * */

    protected InetSocketAddress clientPortAddress;
    protected String dataDir;
    protected String dataLogDir;
    protected int tickTime = ZooKeeperServer.DEFAULT_TICK_TIME;
    protected int maxClientCnxns;
    /** defaults to -1 if not set explicitly */
    protected int minSessionTimeout = -1;
    /** defaults to -1 if not set explicitly */
    protected int maxSessionTimeout = -1;

    public static void main(String[] args) {
        /*-----------------InetAddress---------------------------------*/
        try {
            //获得本地InetAddress对象
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println(localHost);
            //根据域名获得InetAddress对象
            InetAddress byDomainName = InetAddress.getByName("github.com");
            System.out.println(byDomainName);
            //根据ip获得InetAddress对象
            InetAddress byIp = InetAddress.getByName("111.13.100.91");
            System.out.println(byIp.getHostName());////如果ip地址存在，并且DNS给你解析就会输出
            System.out.println(byIp.getHostAddress());
            System.out.println(byIp.getCanonicalHostName());
        } catch (UnknownHostException e) {
            //未知主机
            e.printStackTrace();
        }
        /*--------------------------InetSocketAddress------------------*/
        InetSocketAddress inetSocketAddress = new InetSocketAddress("192.168.1.155", 8080);
        System.out.println(inetSocketAddress.getHostName());
        System.out.println(inetSocketAddress.getPort());
    }
    /**
     * Parse arguments for server configuration
     * @param args clientPort dataDir and optional tickTime and maxClientCnxns
     * @return ServerConfig configured wrt arguments
     * @throws IllegalArgumentException on invalid usage
     */
    public void parse(String[] args) {
        if (args.length < 2 || args.length > 4) {
            throw new IllegalArgumentException("Invalid number of arguments:" + Arrays.toString(args));
        }

        clientPortAddress = new InetSocketAddress(Integer.parseInt(args[0]));
        System.out.println(clientPortAddress.getAddress()+":"+clientPortAddress.getPort());
        dataDir = args[1];
        dataLogDir = dataDir;
        if (args.length >= 3) {
            tickTime = Integer.parseInt(args[2]);
        }
        if (args.length == 4) {
            maxClientCnxns = Integer.parseInt(args[3]);
        }
    }

    /**
     * Parse a ZooKeeper configuration file
     * 解析一个zookeeper配置文件
     * @param path the patch of the configuration file
     * @return ServerConfig configured wrt arguments
     * @throws ConfigException error processing configuration
     */
    public void parse(String path) throws ConfigException {
        QuorumPeerConfig config = new QuorumPeerConfig();
        config.parse(path);

        // let qpconfig parse the file and then pull the stuff we are
        // interested in
        readFrom(config);
    }

    /**
     * Read attributes from a QuorumPeerConfig.
     * @param config
     */
    public void readFrom(QuorumPeerConfig config) {
      clientPortAddress = config.getClientPortAddress();
      dataDir = config.getDataDir();
      dataLogDir = config.getDataLogDir();
      tickTime = config.getTickTime();
      maxClientCnxns = config.getMaxClientCnxns();
      minSessionTimeout = config.getMinSessionTimeout();
      maxSessionTimeout = config.getMaxSessionTimeout();
    }

    public InetSocketAddress getClientPortAddress() {
        return clientPortAddress;
    }
    public String getDataDir() { return dataDir; }
    public String getDataLogDir() { return dataLogDir; }
    public int getTickTime() { return tickTime; }
    public int getMaxClientCnxns() { return maxClientCnxns; }
    /** minimum session timeout in milliseconds, -1 if unset */
    public int getMinSessionTimeout() { return minSessionTimeout; }
    /** maximum session timeout in milliseconds, -1 if unset */
    public int getMaxSessionTimeout() { return maxSessionTimeout; }
}
