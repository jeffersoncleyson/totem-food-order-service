package mock.ports.in.dto;

import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;

import java.time.ZonedDateTime;

public class CustomerResponseMock {

    public static CustomerResponse getMock(String id) {
        var customer = new CustomerResponse();
        customer.setId(id);
        customer.setName("John");
        customer.setCpf("123");
        customer.setEmail("wick@gmail.com");
        customer.setMobile("123456789");
        customer.setModifiedAt(ZonedDateTime.now());
        customer.setCreateAt(ZonedDateTime.now());
        return customer;
    }
}
