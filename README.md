# Infinispan-jMemcached [![Build Status](https://travis-ci.org/zalora/infinispan-jmemcached.svg?branch=master)](https://travis-ci.org/zalora/infinispan-jmemcached)

As the infinispan memcached-server has some buffer problem, which makes it unreliable, we replaced the memcached server
with jMemcached, which is reliable and fast.

## Configuration

For live mode (spring profile == 'dev') export the following environment variable:

`SPRING_PROFILES_ACTIVE=live` or append `--spring.profiles.active=live`

### CLI Parameters

| Parameter                      | Description                                                               | Default Value |
|--------------------------------|---------------------------------------------------------------------------|---------------|
| -Djgroups.bind_addr            | bind address for infinispan sync                                          | localhost     |
| -Djgroups.s3.access_key        | S3 Access Key                                                             | -             |
| -Djgroups.s3.secret_access_key | S3 Password                                                               | -             |
| -Djgroups.s3.bucket            | S3 Bucket Name                                                            | -             |
| -Dinfinispan.cluster.name      | Cluster name groups nodes and defines the S3 folder name                  | -             |
| -Dinfinispan.cache.name        | Cache name reflects in the exported MBeans                                | -             |
| -Dinfinispan.cache.mode        | Available sync modes are: REPL_SYNC, REPL_ASYNC, DIST_SYNC and DIST_ASYNC | DIST_ASYNC    |
| -Dlogging.level.root           | Available log levels: ERROR, WARN, INFO, DEBUG, TRACE                     | INFO          |

In development mode, you don't have to pass in any parameters
