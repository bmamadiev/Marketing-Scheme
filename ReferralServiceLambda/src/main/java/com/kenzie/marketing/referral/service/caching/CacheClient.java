package com.kenzie.marketing.referral.service.caching;

import com.kenzie.marketing.referral.service.dependency.DaggerServiceComponent;

import redis.clients.jedis.Jedis;

import java.util.Optional;

public class CacheClient {

    public CacheClient() {}

    // Remember to check for null keys!
    public void setValue(String key, int seconds, String value) {
        // Check for non-null key
        // Set the value in the cache
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Jedis cache = DaggerServiceComponent.create().provideJedis();
        cache.setex(key, seconds, value);
        cache.close();
    }

    public Optional<String> getValue(String key) {
        // Check for non-null key
        // Retrieves the Optional values from the cache
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Jedis cache = DaggerServiceComponent.create().provideJedis();
        Optional<String> obj = Optional.ofNullable(cache.get(key));
        cache.close();

        return obj;
    }

    public void invalidate(String key) {
        // Check for non-null key
        // Delete the key
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Jedis cache = DaggerServiceComponent.create().provideJedis();
        cache.del(key);
        cache.close();
    }
    private void checkNonNullKey(String key) {
        // Ensure the key isn't null
        // What should you do if the key *is* null?
        if (key == null) {
            throw new IllegalArgumentException();
        }
    }

}
