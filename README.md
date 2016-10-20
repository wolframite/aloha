# ALOHA [![Build Status](https://travis-ci.org/zalora/infinispan-jmemcached.svg?branch=master)](https://travis-ci.org/zalora/infinispan-jmemcached)

## What is aloha?

ALOHA == zALOra jmemcacHed infinispAn

It's a little bit far-fetched, but naming is one of the hardest task in IT these days...

aloha aims to replace a traditional memcached instance with a clusterable, HA memory grid, which still speaks and behaves like memcached

### The Architecture

The setup is built on top of these libraries:
- Infinispan for the memory grid
- jmemcached for the memcached frontend
- spring-boot because it's great!

We tried infinispan to use the infinispan server version first, because it comes with an activated memcached, but we
found a bug, which made it impossible for us to use it. That was the reason we replaced it with jmemcached. You can
read more about that here: https://github.com/zalora/jmemcached

## Configuration
