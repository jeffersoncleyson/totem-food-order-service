package com.totem.food.framework.adapters.in.rest.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {

    //######## VERSIONS
    public static final String API_VERSION_1 = "/v1";

    //######## CATEGORY
    public static final String ADM_CATEGORY = "/administrative/category";
    public static final String CATEGORY_ID = "/{categoryId}";

    //######## ORDER
    public static final String ADM_ORDER = "/administrative/orders";
    public static final String TOTEM_ORDER = "/totem/order";
    public static final String ORDER_ID = "/{orderId}";
    public static final String ORDER_ID_AND_STATUS = "/{orderId}/status/{statusName}";

    //######## PRODUCT
    public static final String ADM_PRODUCT = "/administrative/product";
    public static final String PRODUCT_ID = "/{productId}";

}
