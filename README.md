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

aloha has 4 configuration files: 

- bootstrap.yml
- config/application.yml
- META-INF/persistence.xml
- jgroups.config.xml (Optional)

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
    jgroups.config: jgroups.xml
```

If you are using S3 to coordinate your cluster communication, the cluster name 
is used as a folder. So if you have multiple clusters sharing the same S3 bucket,
you won't have any interference. 

by default `jgroups.config` is empty, but you can let it point to a jgroups.config file to
configure e.g. the communication method of your cluster. aloha comes with a jgroups template to
connect to S3. 

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
        maxSize: 300000 # Maximum amount of entries before eviction to disk store starts
```

The read through cache tries to get cache-misses from the database via a JPA connection:

```
infinispan:
  cache:
    readthrough:
      name: readthrough
      mode: DIST_ASYNC
      enabled: false
      
      # The preloader reads all items of the table and fills the store during startup
      preload: false 
      
      # Chunk size to read data from the database  
      preloadPageSize: 5000
      
      # JPA entity
      entityClass: com.zalora.aloha.models.entities.Product
      
      # Name defined in the persistence.xml
      persistenceUnitName: org.infinispan.persistence.jpa

```

##### Passivation

Due to a bug in the implementation, we don't use soft-index-filestore anymore,
but replaced it with LevelDB. We are using passivation mainly as a failsave to prevent
OOM-Exceptions. The performance penalty when passivating 50% of the keys is between 2x-4x
slower than serving data directly from memory.

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
    readthrough: 11213
```

#### spring

Database settings:

```
spring:
  datasource:
    url: "jdbc:mysql://localhost:3306/bob_live_my?autoReconnect=true&useSSL=false"
    username: root
    password:
    driver-class-name: "com.mysql.jdbc.Driver"
    testWhileIdle: true
    validationQuery: SELECT 1

  jpa:
    show-sql: false
    
    # Important to set to validate for production, other settings might change the table schema and/or data
    hbm2ddl-auto: validate 

    properties.hibernate.dialect: org.hibernate.dialect.MySQL5InnoDBDialect
```

### persistence.xml

This file is already configured by the spring section of the application.yml

### jgroups.config.xml

The included jgroups.config.xml file is preconfigured to coordinate clustering via S3. 
To activate it, point a path to your file in the infinispan.cluster section of the application.yml.
In one of the next releases we will remove the jgroups.config.xml file and start using the config file
which comes with the infinispan-core package (/default-configs/default-jgroups-(ec2|google|tcp|udp).xml)

## Monitoring

All monitoring runs on the embedded untertow server, listening on `http://0.0.0.0:8090` by default.
You can change this port in the `application.yml` (`server.port`).

### Health

For now we only have the health overview provided by spring at `/health`, which shows you if the system is up or
not and the memory usage.

### Jolokia

For some more insight into the system and the cluster status, you can query jolokia at `/jolokia`

### Extended Statistics

To enable the global statistics, either modify the application.yml:

```
infinispan:
  cluster:
    name: Kamehameha
    statistics.enabled: true
```

or start aloha with the flag `-Dinfinispan.cluster.statistics.enabled=true`

On top of the global statistics, you can also turn on statistics per cache here:

```
infinispan:
  cache:
    primary:
      statistics.enabled: false
```

## Docker

Feel free to play around with the Dockerfile (which is very basic). We tried S3 and TCP, which both work nicely.

### Run two docker instances

```
$ mvn clean package docker:build
$ docker run --rm --name aloha1 -p 11211:11211 -d aloha
$ docker run --rm --name aloha2 -p 11212:11211 --link aloha1 -d aloha
```
 