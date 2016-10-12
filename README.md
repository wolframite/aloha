# Infinispan-jMemcached

As the infinispan memcached-server has some buffer problem, which makes it unreliable, we replaced the memcached server
with jMemcached, which is reliable and fast.

## Configuration

For live mode (spring profile == 'dev') export the following environment variables:

- S3_PROFILES_ACTIVE=live
- S3_ACCESS_KEY=
- S3_SECRET_ACCESS_KEY=
- S3_BUCKET=

In development mode (spring profile != ('dev' || 'default') no S3 buckets are used.
