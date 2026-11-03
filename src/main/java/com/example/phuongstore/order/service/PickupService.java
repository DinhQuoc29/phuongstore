package com.example.phuongstore.order.service;

import com.example.phuongstore.order.dto.PickupAllocationItem;
import com.example.phuongstore.order.dto.PickupProductCard;
import com.example.phuongstore.order.entity.OrderItem;
import com.example.phuongstore.order.repository.OrderItemRepository;
import com.example.phuongstore.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickupService {

    private final OrderItemRepository orderItemRepository;

    public List<PickupProductCard> getPickupChecklist() {
        List<OrderItem> pendingItems = orderItemRepository.findAllPendingPickupItemsWithDetails();

        Map<Long, List<OrderItem>> groupedByProduct = new LinkedHashMap<>();
        for (OrderItem item : pendingItems) {
            Long productId = item.getProduct().getId();
            groupedByProduct.computeIfAbsent(productId, k -> new ArrayList<>()).add(item);
        }

        List<PickupProductCard> cards = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : groupedByProduct.entrySet()) {
            List<OrderItem> items = entry.getValue();
            Product product = items.getFirst().getProduct();

            int totalNeeded = items.stream().mapToInt(OrderItem::getQuantity).sum();
            int totalPicked = items.stream().mapToInt(OrderItem::getPickedQuantity).sum();

            List<PickupAllocationItem> allocations = items.stream()
                .map(item -> new PickupAllocationItem(
                    item.getId(),
                    item.getOrder().getId(),
                    item.getOrder().getCustomer().getId(),
                    item.getOrder().getCustomer().getFullName(),
                    item.getOrder().getCustomer().getPhoneNumber(),
                    item.getColorCode(),
                    item.getQuantity(),
                    item.getPickedQuantity()
                ))
                .toList();

            cards.add(new PickupProductCard(
                product.getId(),
                product.getProductCode(),
                product.getProductName(),
                product.getImageUrl(),
                totalNeeded,
                totalPicked,
                allocations
            ));
        }

        return cards;
    }
}
