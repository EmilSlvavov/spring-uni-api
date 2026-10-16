package org.chud.springuniapi.controller.support;

import org.chud.springuniapi.exception.InvalidSortException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortWhitelist {

    private SortWhitelist() {
    }

    public static void check(Pageable pageable, Set<String> allowed) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new InvalidSortException(order.getProperty(), allowed);
            }
        }
    }
}