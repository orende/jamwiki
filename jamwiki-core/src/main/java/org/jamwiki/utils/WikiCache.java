/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

import javax.cache.CacheException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implement utility functions that interact with the cache and provide the
 * infrastructure for storing and retrieving items from the cache.
 */
public class WikiCache<K, V> {

    private record KeyValueClasses(Class keyClass, Class valueClass) {}

	private static final WikiLogger logger = WikiLogger.getLogger(WikiCache.class.getName());
	private static CacheManager CACHE_MANAGER = null;
	private static boolean INITIALIZED = false;
    private final static Map<String, KeyValueClasses> keyValueClassesMap = new HashMap<>();
	// track whether this instance was instantiated from an ehcache.xml file or using configured properties.
	private static final String EHCACHE_XML_CONFIG_FILENAME = "ehcache-jamwiki.xml";
	/** Directory for cache files. */
	private static final String CACHE_DIR = "cache";
	private final String cacheName;

	/**
	 * Initialize a new cache with the given name.
	 *
	 * @param cacheName The name of the cache being created.  This name should not
	 *  be re-used, otherwise unexpected results could be returned.
	 */
    public WikiCache(String cacheName, Class keyClass, Class valueClass) {
        this.cacheName = cacheName;
        keyValueClassesMap.put(cacheName, new KeyValueClasses(keyClass, valueClass));
    }

    /**
	 * Add an object to the cache.
	 *
	 * @param key A String, Integer, or other object to use as the key for
	 *  storing and retrieving this object from the cache.
	 * @param value The object that is being stored in the cache.
	 */
	public void addToCache(K key, V value) {
		this.getCache().put(key, value);
	}

	/**
	 * Internal method used to retrieve the Cache object created for this
	 * instance's cache name.  If no cache exists with the given name then
	 * a new cache will be created.
	 *
	 * @return The existing cache object, or a new cache if no existing cache
	 *  exists.
	 * @throws IllegalStateException if an attempt is made to retrieve a cache
	 *  using XML configuration and the cache is not configured.
	 */
	private Cache<K,V> getCache() throws CacheException {
		if (!WikiCache.INITIALIZED) {
			WikiCache.initialize();
		}
        KeyValueClasses kv = keyValueClassesMap.get(this.cacheName);
        Cache<K, V> cache = WikiCache.CACHE_MANAGER.getCache(this.cacheName, kv.keyClass, kv.valueClass);
        if (cache == null) {
			// all caches should be configured from ehcache.xml
			throw new IllegalStateException("No cache named " + this.cacheName + " is configured in the ehcache.xml file");
		}
		return (Cache<K, V>) cache;
	}

	/**
	 * Return the name of the cache that this instance was configured with.
	 */
	public String getCacheName() {
		return this.cacheName;
	}

	/**
	 * Initialize the cache, clearing any existing cache instances and loading
	 * a new cache instance.
	 */
	public static void initialize() {
        try {
            File file = ResourceUtil.getClassLoaderFile(EHCACHE_XML_CONFIG_FILENAME);
            XmlConfiguration xmlConfiguration = new XmlConfiguration(file.toURI().toURL());
            WikiCache.CACHE_MANAGER = CacheManagerBuilder.newCacheManager(xmlConfiguration);
            WikiCache.CACHE_MANAGER.init();
            WikiCache.INITIALIZED = true;
            logger.info("Initializing cache with disk store: " + System.getProperty("user.home") + File.pathSeparator + CACHE_DIR);
        } catch (IOException e) {
            logger.error("Failure while initializing cache", e);
            throw new RuntimeException(e);
        }
	}

	/**
	 * Return <code>true</code> if the key is in the specified cache, even
	 * if the value associated with that key is <code>null</code>.
	 */
	public boolean isKeyInCache(K key) {
		return this.getCache().containsKey(key);
	}

	/**
	 * Close the cache manager.
	 */
	public static void shutdown() {
		WikiCache.INITIALIZED = false;
		if (WikiCache.CACHE_MANAGER != null) {
			WikiCache.CACHE_MANAGER.close();
			WikiCache.CACHE_MANAGER = null;
		}
	}

	/**
	 * Remove all values from the cache.
	 */
	public void removeAllFromCache() {
		this.getCache().clear();
	}

	/**
	 * Remove a value from the cache with the given key.
	 *
	 * @param key The key for the record that is being removed from the cache.
	 */
	public void removeFromCache(K key) {
		this.getCache().remove(key);
	}

	/**
	 * Remove a key from the cache in a case-insensitive manner.  This method
	 * is significantly slower than removeFromCache and should only be used when
	 * the key values may not be exactly known.
	 */
	public void removeFromCacheCaseInsensitive(String key) {
        this.getCache().iterator().forEachRemaining(kvEntry -> {
            if (kvEntry.getKey() != null && kvEntry.getKey().toString().equalsIgnoreCase(key)) {
                this.getCache().remove(kvEntry.getKey());
            }
        });
    }

	/**
	 * Retrieve an object from the cache.  IMPORTANT: this method will return
	 * <code>null</code> if no matching element is cached OR if the cached
	 * object has a value of <code>null</code>.  Callers should call
	 * {@link #isKeyInCache} if a <code>null</code> value is returned to
	 * determine whether a <code>null</code> was cached or if the value does
	 * not exist in the cache.
	 *
	 * @param key The key for the record that is being retrieved from the
	 *  cache.
	 * @return The cached object if one is found, <code>null</code> otherwise.
	 */
	public V retrieveFromCache(K key) {
        return this.getCache().get(key);
	}
}
