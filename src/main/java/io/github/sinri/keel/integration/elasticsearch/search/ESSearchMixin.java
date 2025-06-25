package io.github.sinri.keel.integration.elasticsearch.search;

import io.github.sinri.keel.integration.elasticsearch.ESApiMixin;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface ESSearchMixin extends ESApiMixin {
    /**
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.9/search-search.html">Search API</a>
     */
    default Future<ESSearchResponse> searchSync(String indexName, ESApiQueries queries, JsonObject requestBody) {
        return callPost("/" + indexName + "/_search", queries, requestBody)
                .compose(resp -> Future.succeededFuture(new ESSearchResponse(resp)));
    }
}
