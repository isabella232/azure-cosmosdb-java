/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed;

import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosContainerSettings;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseRequestOptions;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * The interface that captures the APIs required to handle change feed processing logic.
 */
public interface ChangeFeedContextClient {
    /**
     * Reads the feed (sequence) of {@link PartitionKeyRange} for a database account from the Azure Cosmos DB service as an asynchronous operation.
     *
     * @param partitionKeyRangesOrCollectionLink the link of the resources to be read, or owner collection link, SelfLink or AltLink. E.g. /dbs/db_rid/colls/coll_rid/pkranges.
     * @param feedOptions the options for the request; it can be set as null.
     * @return an an {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRangeFeed(String partitionKeyRangesOrCollectionLink, FeedOptions feedOptions);

    /**
     * Method to create a change feed query for documents.
     *
     * @param collectionLink Specifies the collection to read documents from.
     * @param feedOptions The options for processing the query results feed.
     * @return an {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<CosmosItemProperties>> createDocumentChangeFeedQuery(CosmosContainer collectionLink, ChangeFeedOptions feedOptions);

    /**
     * Reads a database.
     *
     * @param database a reference to the database.
     * @param options the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the single cosmos database response with the read database or an error.
     */
    Mono<CosmosDatabaseResponse> readDatabase(CosmosDatabase database, CosmosDatabaseRequestOptions options);

    /**
     * Reads a {@link CosmosContainer}.
     *
     * @param containerLink   a reference to the container.
     * @param options         the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the single cosmos container response with the read container or an error.
     */
    Mono<CosmosContainerResponse> readContainer(CosmosContainer containerLink, CosmosContainerRequestOptions options);

    /**
     * Creates a {@link CosmosItem}.
     *
     * @param containerLink                the reference to the parent container.
     * @param document                     the document represented as a POJO or Document object.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Mono} containing the single resource response with the created cosmos item or an error.
     */
    Mono<CosmosItemResponse> createItem(CosmosContainer containerLink, Object document, CosmosItemRequestOptions options,
                                        boolean disableAutomaticIdGeneration);

    /**
     * DELETE a {@link CosmosItem}.
     *
     * @param itemLink  the item reference.
     * @param options   the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the deleted item or an error.
     */
    Mono<CosmosItemResponse> deleteItem(CosmosItem itemLink, CosmosItemRequestOptions options);

    /**
     * Replaces a {@link CosmosItem}.
     *
     * @param itemLink     the item reference.
     * @param document     the document represented as a POJO or Document object.
     * @param options      the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    Mono<CosmosItemResponse> replaceItem(CosmosItem itemLink, Object document, CosmosItemRequestOptions options);

    /**
     * Reads a {@link CosmosItem}
     *
     * @param itemLink     the item reference.
     * @param options      the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the read item or an error.
     */
    Mono<CosmosItemResponse> readItem(CosmosItem itemLink, CosmosItemRequestOptions options);

    /**
     * Query for items in a document container.
     *
     * @param containerLink  the reference to the parent container.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<CosmosItemProperties>> queryItems(CosmosContainer containerLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * @return the Cosmos client's service endpoint.
     */
    URI getServiceEndpoint();

    /**
     * Reads and returns the container settings.
     *
     * @param containerLink   a reference to the container.
     * @param options         the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the read container settings.
     */
    Mono<CosmosContainerSettings> readContainerSettings(CosmosContainer containerLink, CosmosContainerRequestOptions options);

    /**
     * @return the Cosmos container client.
     */
    CosmosContainer getContainerClient();

    /**
     * @return the Cosmos database client.
     */
    CosmosDatabase getDatabaseClient();

    /**
     * Closes the document client instance and cleans up the resources.
     */
    void close();
}
