# ALOHA [![Build Status](https://travis-ci.org/zalora/aloha.svg?branch=master)](https://travis-ci.org/zalora/aloha)

## What is aloha?

ALOHA == zALOra jmemcacHed infinispAn

It's a little bit far-fetched, but naming is one of the hardest task in IT these days...

> aloha aims to replace a traditional memcached instance with a clusterable, HA memory grid, which still speaks and behaves like memcached

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

aloha has 3 configuration files: 

- bootstrap.yml
- config/application.yml
- jgroups-jdbc.xml (Optional)
- default-jgroups-ec2.xml (Optional)

### bootstrap.yml

Currently only the app name is defined here. If you wanted to add support for a config server, that would be the place to go.
   
### config/application.yml

This is the heart of aloha, you can configure nearly every aspect of the app. You can override every
setting in the application.yml via command-line properties. If you don't like the cluster name (the default cluster name
comes from the House of Kamehameha, a dynasty of Hawaiian Kings), start the app like that:

`java -Dinfinispan.cluster.name=whyWouldYouDoThat -jar aloha.jar`

Just keep in mind not to override credentials via the command line.

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

We have three caches hardwired into the app:

|            | Primary Cache                 | Secondary Cache  | Read-Through Cache    |
|------------|-------------------------------|------------------|-----------------------|
| Usage      | Generic memcached replacement | Sessions         | Augment Primary Cache |
| Cache Mode | Distributed Async             | Distributed Sync | Distributed Async     |

This is the commented configuration of the primary cache:

```
infinispan:
  cache:
    primary:
      name: main # Changes here are reflected in the JMX path 

      # Infinispan clustering modes: http://infinispan.org/docs/stable/user_guide/user_guide.html#clustering
      mode: DIST_ASYNC  
      
      enabled: true
      
      # http://infinispan.org/docs/stable/user_guide/user_guide.html#l1_caching
      l1:
        enabled: true
        lifespan: 600 # lifespan in seconds

      # Evicted entries are written to disk rather than deleted
      # http://infinispan.org/docs/stable/user_guide/user_guide.html#cache-passivation
      passivation:
        enabled: true
        dataLocation: diskStore/primary/data
        expiredLocation: diskStore/primary/expired
        maxSize: 671_088_640 # If the data uses up more than 640MB, passivation will start
```

##### Passivation

Due to a bug in the implementation, we don't use soft-index-filestore anymore,
but replaced it with LevelDB. We are using passivation mainly as a failsave to prevent
OOM-Exceptions. The performance penalty when passivating 50% of the keys is between 2x-4x
slower than serving data directly from memory.

In the primary cache the passivation threshold is memory usage based, in the secondary cache it's 
entry based. We are doing that because we want to limit the consumed memory in the main cache
and prevent OOM. We're using the secondary cache as session storage, so the size is minimal, but
we only want a certain number of sessions in memory and passivate the sessions created by bots.

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

### jgroups-jdbc.xml

The included jgroups-jdbc.xml file is preconfigured to coordinate clustering via JDBC. 
To activate it, point a path to your file in the infinispan.cluster section of the application.yml.

You have to provide the following parameters, to make JDBC_PING work:

| Name                | Variable                              | Default               | Example                            |
|---------------------|---------------------------------------|-----------------------|------------------------------------|
| Connection URL      | `${jgroups.jdbc.connection_url}`      |                       | jdbc:mysql://localhost/bob_live_sg |
| Connection Username | `${jgroups.jdbc.connection_username}` |                       | root                               |
| Connection Password | `${jgroups.jdbc.connection_password}` |                       | secret                             |
| Connection Driver   | `${jgroups.jdbc.connection_driver}`   | com.mysql.jdbc.Driver | com.mysql.jdbc.Driver              |

To activate the JDBC jgroups profile, the variable `infinispan.cluster.jgroups.config` has to be set to `jgroups-jdbc.xml`

### default-jgroups-ec2.xml

The ec2 jgroups file was extracted from the infinispan-cloud package.
To activate the S3 jgroups profile, the variable `infinispan.cluster.jgroups.config` has to be set to `default-jgroups-ec2.xml`

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

Cache statistics are always enabled

## Docker

Feel free to play around with the Dockerfile (which is very basic). We tried S3 and TCP, which both work nicely.

### Run two docker instances

```
$ mvn clean package docker:build
$ docker run --rm --name aloha1 -p 11211:11211 -d aloha
$ docker run --rm --name aloha2 -p 11212:11211 --link aloha1 -d aloha
```
 