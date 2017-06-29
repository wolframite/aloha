# ALOHA

## What's with the name?

Pretty obvious, duh!

ALOHA == z**ALO**ra jmemcac**H**ed infinisp**A**n

> aloha replaces your traditional memcached setup (data and sessions) with a clusterable,
HA memory grid, which still speaks and behaves like memcached

## tl;dr &ndash; Give me Memcached!

`mvn clean package && java -jar target/aloha-*.jar`

This gives you a single local aloha instance with one memcached container listening on 11211

## The Architecture

The setup is built on top of these libraries:
- Infinispan for the memory grid
- jmemcached for the memcached frontend
- spring-boot because it's great!

We tried infinispan to use the infinispan server version first, because it comes with an activated memcached, but we
found a bug, which made it impossible for us to use it. That was the reason we replaced it with jmemcached. You can
read more about that here: https://github.com/zalora/jmemcached

## Configuration

aloha has 2 configuration files: 

- bootstrap.yml
- config/application.yml

### bootstrap.yml

Currently only the app name is defined here. If you wanted to add support for a config server, that would be the place to go.
   
### config/application.yml

This is the heart of aloha, you can configure nearly every aspect of the app. You can override every
setting in the application.yml via command-line properties. If you don't like the cluster name (the default cluster name
comes from the House of Kamehameha, a dynasty of Hawaiian Kings), start the app like that:

`java -Dinfinispan.cluster.name=whyWouldYouDoThat -jar aloha.jar`

#### infinispan

```
infinispan:
  cluster:
    name: Kamehameha
    jgroups.config: 
```

By default `jgroups.config` is empty, but you can let it point to one of the jgroups.config 
files, which come with infinispan: `/default-configs/default-jgroups-(tcp|udp|ec2|google|kubernetes).xml`

If you want to use JDBC for coordination, check out the `jgroups-jdbc.xml` section. 

We have two caches hardwired into the app:

|            | Primary Cache                 | Secondary Cache  |
|------------|-------------------------------|------------------|
| Usage      | Generic memcached replacement | Sessions         |
| Cache Mode | Distributed Async             | Distributed Sync |

This is the commented configuration of the primary cache:

```
infinispan:
  cache:
    primary:
      name: main
      
      # Infinispan clustering modes: http://infinispan.org/docs/stable/user_guide/user_guide.html#clustering
      mode: DIST_ASYNC
      enabled: true
    
      compressor: com.zalora.aloha.compressor.Lz4
```

Aloha supports all cache modes infinispan offers, if you want to read more about that, have
a look at the official documentation: http://infinispan.org/docs/stable/user_guide/user_guide.html#clustering

Aloha comes with transparent LZ4-compression, the storage layer compresses the data field of the MemcachedItem
before it's put in the cache. Before returning it to the memcached part of the application, the data is uncompressed,
so the user doesn't have to do anything.

I will add a HotRod server endpoint soon, then the compression will be done in the client, so you have compressed
transfer over the wire and everything is still transparent for the users.

#### memcached

Configures the memcached frontend for every cache defined in the infinispan section

```
memcached:
  host: 0.0.0.0 # Host to listen on
  idleTime: 2000 # Connection timeout
  verbose: false # Prints additional information to the console

  # Port settings for the caches
  port:
    primary : 11211
    secondary: 11212
```

## Monitoring

All monitoring runs on the embedded untertow server, listening on `http://0.0.0.0:8090` by default.
You can change this port in the `application.yml` (`server.port`).

### Health

For now we only have the health overview provided by spring at `/health`, which shows you if the system is up or
not and the memory usage.

### Jolokia

For some more insight into the system and the cluster status, you can query jolokia at `/jolokia`

### Statistics

To enable the global statistics, either modify the application.yml:

```
infinispan:
  cluster:
    name: Kamehameha
    statistics.enabled: true
```

or start aloha with the flag `-Dinfinispan.cluster.statistics.enabled=true`

Cache statistics are always enabled.
