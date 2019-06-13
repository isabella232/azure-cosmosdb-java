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

package com.azure.data.cosmos.internal.query.orderbyquery;

import com.azure.data.cosmos.internal.query.ItemComparator;
import com.azure.data.cosmos.internal.query.ItemType;
import com.azure.data.cosmos.internal.query.ItemTypeHelper;
import com.azure.data.cosmos.internal.query.QueryItem;
import com.azure.data.cosmos.internal.query.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class OrderbyRowComparer<T> implements Comparator<OrderByRowResult<T>> {
    private static final Logger logger = LoggerFactory.getLogger(OrderbyRowComparer.class);
    
    private final List<SortOrder> sortOrders;
    private volatile List<ItemType> itemTypes;

    public OrderbyRowComparer(Collection<SortOrder> sortOrders) {
        this.sortOrders = new ArrayList<>(sortOrders);
    }

    @Override
    public int compare(OrderByRowResult<T> r1, OrderByRowResult<T> r2) {
        try {
            // comparing document (row) vs document (row)
            List<QueryItem> result1 = r1.getOrderByItems();
            List<QueryItem> result2 = r2.getOrderByItems();

            if (result1.size() != result2.size()) {
                throw new IllegalStateException("OrderByItems cannot have different sizes.");
            }

            if (result1.size() != this.sortOrders.size()) {
                throw new IllegalStateException(
                        String.format("OrderByItems cannot have a different size than sort orders."));
            }

            if (this.itemTypes == null) {
                synchronized (this) {
                    if (this.itemTypes == null) {
                        this.itemTypes = new ArrayList<ItemType>(result1.size());
                        for (QueryItem item : result1) {
                            this.itemTypes.add(ItemTypeHelper.getOrderByItemType(item.getItem()));
                        }
                    }
                }
            }

            this.checkOrderByItemType(result1);
            this.checkOrderByItemType(result2);

            for (int i = 0; i < result1.size(); ++i) {
                int cmp = ItemComparator.getInstance().compare(result1.get(i).getItem(), result2.get(i).getItem());
                if (cmp != 0) {
                    switch (this.sortOrders.get(i)) {
                    case Ascending:
                        return cmp;
                    case Descending:
                        return -cmp;
                    }
                }
            }

            return r1.getSourcePartitionKeyRange().getMinInclusive().compareTo(r2.getSourcePartitionKeyRange().getMinInclusive());
        } catch (Exception e) {
            // Due to a bug in rxjava-extras <= 0.8.0.15 dependency,
            // if OrderbyRowComparer throws an unexpected exception,
            // then the observable returned by Transformers.orderedMergeWith(.) will never emit a terminal event.
            // rxjava-extras lib provided a quick fix on the bugreport: 
            // https://github.com/davidmoten/rxjava-extras/issues/30 (0.8.0.16)
            // we are also capturing the exception stacktrace here
            logger.error("Orderby Row comparision failed {}, {}", r1.toJson(), r2.toJson(), e);
            throw e;
        }
    }
    
    private void checkOrderByItemType(List<QueryItem> orderByItems) {
        for (int i = 0; i < this.itemTypes.size(); ++i) {
            ItemType type = ItemTypeHelper.getOrderByItemType(orderByItems.get(i).getItem());
            if (type != this.itemTypes.get(i)) {
                throw new UnsupportedOperationException(
                        String.format("Expected %s, but got %s.", this.itemTypes.get(i).toString(), type.toString()));
            }
        }
    }

    public List<SortOrder> getSortOrders() {
        return this.sortOrders;
    }
}
