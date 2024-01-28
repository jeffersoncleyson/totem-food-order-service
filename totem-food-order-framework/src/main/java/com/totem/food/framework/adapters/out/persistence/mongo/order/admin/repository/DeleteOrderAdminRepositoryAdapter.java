package com.totem.food.framework.adapters.out.persistence.mongo.order.admin.repository;

import com.totem.food.application.ports.out.persistence.commons.IRemoveRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.framework.adapters.out.persistence.mongo.commons.BaseRepository;
import com.totem.food.framework.adapters.out.persistence.mongo.order.totem.entity.OrderEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Component
public class DeleteOrderAdminRepositoryAdapter implements IRemoveRepositoryPort<OrderModel> {

	@Repository
	protected interface ProductRepositoryMongoDB extends BaseRepository<OrderEntity, String> {
	}

	private final ProductRepositoryMongoDB repository;

	@Override
	public void removeItem(String id) {
		repository.deleteById(id);
	}
}
