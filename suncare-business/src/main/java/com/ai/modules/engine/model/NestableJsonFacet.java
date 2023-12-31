/**
 * NestableJsonFacet.java	  V1.0   2019年12月3日 下午3:18:03
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.HeatmapJsonFacet;
import org.apache.solr.common.util.NamedList;

public class NestableJsonFacet {
	private long domainCount;
	private final Map<String, NestableJsonFacet> queryFacetsByName;
	private final Map<String, BucketBasedJsonFacet> bucketBasedFacetByName;
	private final Map<String, Number> statFacetsByName;
	private final Map<String, HeatmapJsonFacet> heatmapFacetsByName;

	public NestableJsonFacet(NamedList<Object> facetNL) {
		queryFacetsByName = new HashMap<>();
		bucketBasedFacetByName = new HashMap<>();
		statFacetsByName = new HashMap<>();
		heatmapFacetsByName = new HashMap<>();

		for (Map.Entry<String, Object> entry : facetNL) {
			final String key = entry.getKey();
			if (getKeysToSkip().contains(key)) {
				continue;
			} else if ("count".equals(key)) {
				domainCount = (long) entry.getValue();
			} else if (entry.getValue() instanceof Number) { // Stat/agg facet value
				statFacetsByName.put(key, (Number) entry.getValue());
			} else if (entry.getValue() instanceof NamedList) { // Either heatmap/query/range/terms facet
				final NamedList<Object> facet = (NamedList<Object>) entry.getValue();
				final boolean isBucketBased = facet.get("buckets") != null;
				final boolean isHeatmap = HeatmapJsonFacet.isHeatmapFacet(facet);
				if (isBucketBased) {
					bucketBasedFacetByName.put(key, new BucketBasedJsonFacet(facet));
				} else if (isHeatmap) {
					heatmapFacetsByName.put(key, new HeatmapJsonFacet(facet));
				} else { // "query" facet
					queryFacetsByName.put(key, new NestableJsonFacet(facet));
				}
			}
		}
	}

	/**
	 * The number of records matching the domain of this facet.
	 */
	public long getCount() {
		return domainCount;
	}

	/**
	 * Retrieve a nested "query" facet by its name
	 */
	public NestableJsonFacet getQueryFacet(String name) {
		return queryFacetsByName.get(name);
	}

	/**
	 * @return the names of any "query" facets that are direct descendants of the
	 *         current facet
	 */
	public Set<String> getQueryFacetNames() {
		return queryFacetsByName.keySet();
	}

	/**
	 * Retrieve a nested "terms" or "range" facet by its name.
	 */
	public BucketBasedJsonFacet getBucketBasedFacets(String name) {
		return bucketBasedFacetByName.get(name);
	}

	/**
	 * @return the names of any "terms" or "range" facets that are direct
	 *         descendants of this facet
	 */
	public Set<String> getBucketBasedFacetNames() {
		return bucketBasedFacetByName.keySet();
	}

	/**
	 * Retrieve the value for a stat or agg facet with the provided name
	 */
	public Number getStatFacetValue(String name) {
		return statFacetsByName.get(name);
	}

	/**
	 * @return the names of any stat or agg facets that are direct descendants of
	 *         this facet
	 */
	public Set<String> getStatFacetNames() {
		return statFacetsByName.keySet();
	}

	/**
	 * Retrieve a "heatmap" facet by its name
	 */
	public HeatmapJsonFacet getHeatmapFacetByName(String name) {
		return heatmapFacetsByName.get(name);
	}

	/**
	 * @return the names of any heatmap facets that are direct descendants of this
	 *         facet
	 */
	public Set<String> getHeatmapFacetNames() {
		return heatmapFacetsByName.keySet();
	}

	/*
	 * Used by subclasses to control which keys are ignored during parsing.
	 */
	protected Set<String> getKeysToSkip() {
		final HashSet<String> keysToSkip = new HashSet<>();
		return keysToSkip;
	}
}
