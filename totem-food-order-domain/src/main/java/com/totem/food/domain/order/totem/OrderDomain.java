package com.totem.food.domain.order.totem;

import com.totem.food.domain.exceptions.InvalidStatusException;
import com.totem.food.domain.exceptions.InvalidStatusTransition;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import com.totem.food.domain.product.ProductDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDomain {

    @Setter
    private String id;

    //@todo - refact @Setter

    @Setter
    private String customer;

    @Setter
    private List<ProductDomain> products;

    @Builder.Default
    private OrderStatusEnumDomain status = OrderStatusEnumDomain.NEW;

    @Setter
    private double price;

    @Setter
    private ZonedDateTime modifiedAt;

    @Setter
    private ZonedDateTime createAt;

    @Setter
    private ZonedDateTime receivedAt;

    public void updateOrderStatus(OrderStatusEnumDomain status) {

        final var statusTransition = StatusTransition.from(this.status)
                .orElseThrow(() -> new InvalidStatusTransition(this.status.key, status.key, OrderStatusEnumDomain.getKeys()));

        if (!statusTransition.allowedTransitions().contains(status) && !status.equals(OrderStatusEnumDomain.NEW)) {
            throw new InvalidStatusTransition(this.status.key, status.key, OrderStatusEnumDomain.getKeys());
        }

        this.status = status;

        if (this.status.equals(OrderStatusEnumDomain.RECEIVED)) {
            updateOrderReceivedAt();
        }
    }

    public void updateModifiedAt() {
        this.modifiedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public void updateOrderReceivedAt() {
        this.receivedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public void fillDates() {
        if (StringUtils.isEmpty(this.id)) {
            this.createAt = ZonedDateTime.now(ZoneOffset.UTC);
            this.modifiedAt = ZonedDateTime.now(ZoneOffset.UTC);
        }
    }

    public void clearProducts() {
        this.products = null;
    }

    public void isOrderInStatusOfUpdate(){
        if(!Objects.equals(OrderStatusEnumDomain.NEW, this.status)){
            throw new InvalidStatusException("Order", this.status.key, OrderStatusEnumDomain.NEW.key);
        }
    }

    public void calculatePrice() {

        final var productsPrice = Optional.ofNullable(this.products)
                .filter(CollectionUtils::isNotEmpty)
                .map(p -> p.stream().mapToDouble(ProductDomain::getPrice).sum()).orElse(0D);

        this.price = BigDecimal.valueOf(productsPrice)
                .setScale(2, RoundingMode.FLOOR)
                .doubleValue();
    }

    private enum StatusTransition {

        NEW("NEW") {
            @Override
            public Set<OrderStatusEnumDomain> allowedTransitions() {
                return Set.of(OrderStatusEnumDomain.CANCELED, OrderStatusEnumDomain.WAITING_PAYMENT);
            }
        },
        WAITING_PAYMENT("WAITING_PAYMENT") {
            @Override
            public Set<OrderStatusEnumDomain> allowedTransitions() {
                return Set.of(OrderStatusEnumDomain.CANCELED, OrderStatusEnumDomain.RECEIVED);
            }
        },
        RECEIVED("RECEIVED") {
            @Override
            public Set<OrderStatusEnumDomain> allowedTransitions() {
                return Set.of(OrderStatusEnumDomain.IN_PREPARATION);
            }
        },
        IN_PREPARATION("IN_PREPARATION") {
            @Override
            public Set<OrderStatusEnumDomain> allowedTransitions() {
                return Set.of(OrderStatusEnumDomain.READY);
            }
        },
        READY("READY") {
            @Override
            public Set<OrderStatusEnumDomain> allowedTransitions() {
                return Set.of(OrderStatusEnumDomain.FINALIZED);
            }
        },
        CANCELED("CANCELED") {
            @Override
            public Set<OrderStatusEnumDomain> allowedTransitions() {
                return Set.of(OrderStatusEnumDomain.CANCELED);
            }
        };

        public final String key;

        StatusTransition(String key) {
            this.key = key;
        }


        public abstract Set<OrderStatusEnumDomain> allowedTransitions();

        public static Optional<StatusTransition> from(final OrderStatusEnumDomain source) {
            return Arrays.stream(StatusTransition.values()).filter(e -> source.name().equals(e.name())).findFirst();
        }

    }
}
