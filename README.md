# ALOHA

## What's with the name?

Pretty obvious, duh!

ALOHA == z**ALO**ra jmemcac**H**ed infinisp**A**n

> aloha replaces your traditional memcached setup with a clusterable, HA memory grid, which
still speaks and behaves like memcached

## tl;dr &ndash; Give me Memcached!

`mvn clean package && java -jar target/aloha-*.jar`

This gives you a single local aloha instance with one memcached container listening on 11211

## The Architecture

The setup is built on top of these libraries:
- Infinispan for the memory grid
- spring-boot because it's great!

## Configuration

aloha has 2 configuration files: 

- bootstrap.yml
- config/application.yml

### bootstrap.yml

Currently only the app name is defined here. If you wanted to add support for a config server,
that would be the place to go.
   
### config/application.yml

This is the heart of aloha, you can configure nearly every aspect of the app. You can override every
setting in the application.yml via command-line properties, e.g. turn on the socket server:

`java -Dsocket.enabled=true -jar aloha.jar`

#### infinispan

```
infinispan:
  cluster:
    jgroups:
    name: Kamehameha

  cache:
    name: memcachedCache
    mode: LOCAL
    numOwners: 3 # Amount of machines in the cluster storing the key
    stateTransferChunkSize: 128
    lock:
      timeout: 30 # Lock acquisition timeout in seconds
      concurrency: 1024
```

By default Aloha is configured to provide a single local cache which can be accessed with the Memcached protocol.
Internally infinispan uses a `Cache<String, byte[]>` type to store the data.

If you let `jgroups` point to a jgroups file (Aloha comes with two:
`/default-configs/default-jgroups-(tcp|udp).xml`), you can use the clustering features of
infinispan, which is documented here: http://infinispan.org/docs/stable/user_guide/user_guide.html#clustering

To start a `REPL_SYNC` cluster, you can run `./start-node1` and `./start-node2`.

#### memcached

Since Infinispan 9.1.1, we can use the Memcached server, which comes with Infinispan again, because a bug we
reported was fixed in this version.

```
memcached:
  enabled: true
  host: 0.0.0.0
  port: 11211
  idleTime: -1
  recvBufSize: 0
  sendBufSize: 0
  tcpNoDelay: true
```

In theory you can configure the name of the cache to use, but unless it's set to "memcachedCache", the server fails
to boot up. The configuration parameters in the application.yml represent the defaults of configuration-builder.

#### hotrod

By default the hotrod server is disabled, it can be enabled via a cli parameter or by changing the config file
directly. We increased the timeouts, because it solved problems we had on shitty networks. Like on AWS.

```
hotrod:
  enabled: false
  host: 0.0.0.0
  topologyLockTimeout: 30000 # ms
  topologyReplTimeout: 30000 # ms
```

#### socket

We added a netty-based unix domain socket server to eliminate latency when the server is running on the same
machine than the client.

I'm sure there's plenty of potential for optimization, so PRs are welcome! The socket is read-only and it supports
get and multi-get. Quick test on the cli using openbsd's netcat:

**Single Get:**

`echo mykey | nc -U /tmp/sockythesock.sock`

The result of a single get is only the value.

**Multi Get:**

`echo "mykey1 mykey2" | nc -U /tmp/sockythesock.sock`

The result of a multi-get is a bit more verbose:

```
mykey1
myvalue1
mykey2
myvalue2
```

Configuration is self-explaning:

```
socket:
  enabled: false
  path: /tmp/sockythesock.sock
```

### Health

For now we only have the health overview provided by spring at `/health`, which shows you if the system is up or
not.

### Jolokia

For some more insight into the system and the cluster status, you can query jolokia at `/jolokia`