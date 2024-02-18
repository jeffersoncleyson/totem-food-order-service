package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.exceptions.InvalidInput;
import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.dtos.payment.PaymentFilterDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.out.dtos.EmailNotificationDto;
import com.totem.food.application.ports.out.dtos.PaymentNotificationDto;
import com.totem.food.application.ports.out.event.ISendEventPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.IUpdateRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.application.usecases.annotations.UseCase;
import com.totem.food.application.usecases.commons.IUpdateStatusUseCase;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@AllArgsConstructor
@UseCase
public class UpdateStatusOrderUseCase implements IUpdateStatusUseCase<OrderDto> {

    private final IOrderMapper iOrderMapper;
    private final ISearchUniqueRepositoryPort<Optional<OrderModel>> iSearchUniqueRepositoryPort;
    private final IUpdateRepositoryPort<OrderModel> iProductRepositoryPort;
    private final ISendEventPort<EmailNotificationDto, Boolean> iSendEmailEventPort;
    private final ISendRequestPort<PaymentFilterDto, Boolean> iSendRequestPaymentPort;
    private final ISendRequestPort<String, Optional<CustomerResponse>> iSearchUniqueCustomerRepositoryPort;
    private final ISendEventPort<PaymentNotificationDto, Boolean> sendEventPort;

    @Override
    public OrderDto updateStatus(String id, String status, boolean isEvent) {

        final var orderModelOptional = iSearchUniqueRepositoryPort.findById(id);

        final var model = orderModelOptional.orElseThrow(() -> new ElementNotFoundException(String.format("Order [%s] not found", id)));

        if (model.getStatus().equals(OrderStatusEnumDomain.from(status))) {
            return iOrderMapper.toDto(model);
        }

        final var domain = iOrderMapper.toDomain(model);

        domain.updateOrderStatus(OrderStatusEnumDomain.from(status));
        domain.updateModifiedAt();

        if (domain.getStatus().equals(OrderStatusEnumDomain.RECEIVED) && !isEvent) {
            throw new InvalidInput(String.format("Order [%s] needs a payment request or Payment is PENDING", id));
        }

        if (List.of(OrderStatusEnumDomain.READY, OrderStatusEnumDomain.RECEIVED).contains(domain.getStatus())) {
            iSearchUniqueCustomerRepositoryPort.sendRequest(domain.getCustomer())
                    .map(CustomerResponse::getEmail).ifPresent(sendEmail(id, domain.getStatus()));
        }

        final var domainValidated = iOrderMapper.toModel(domain);
        final var domainSaved = iProductRepositoryPort.updateItem(domainValidated);

        if(OrderStatusEnumDomain.WAITING_PAYMENT.key.equals(domain.getStatus().key)){
            sendEventPort.sendMessage(PaymentNotificationDto.builder()
                    .order(domainSaved)
                    .build());
        }

        return iOrderMapper.toDto(domainSaved);
    }

    private Consumer<String> sendEmail(String id, OrderStatusEnumDomain status) {
        return email -> {
            final var subject = String.format("[%s] Pedido %s", "Totem Food Service", id);
            var message = "";
            if(status.equals(OrderStatusEnumDomain.READY))
                message = String.format("Pedido %s acabou de ser finalizado pela cozinha, em instantes o atendente ira chama-lo!", id);
            if(status.equals(OrderStatusEnumDomain.RECEIVED))
                message = String.format("Pedido %s recebido pela cozinha, em instantes nosso chefes irão prepara-lo!", id);
            iSendEmailEventPort.sendMessage(new EmailNotificationDto(email, subject, message));
        };
    }

}