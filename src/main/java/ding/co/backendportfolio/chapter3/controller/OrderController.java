package ding.co.backendportfolio.chapter3.controller;

import ding.co.backendportfolio.chapter3.dto.OrderDetailResponse;
import ding.co.backendportfolio.chapter3.dto.OrderResponse;
import ding.co.backendportfolio.chapter3.dto.OrderStatisticsResponse;
import ding.co.backendportfolio.chapter3.entity.OrderStatus;
import ding.co.backendportfolio.chapter3.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/chapter3/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Retrieves a paginated list of all orders
     * @param pageable pagination parameters
     * @return paginated order response list
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.findOrders(pageable));
    }

    /**
     * Retrieves detailed information of a specific order by ID
     * @param id order ID
     * @return order detail response including order items
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    /**
     * Retrieves a paginated list of orders filtered by status
     * @param status order status (e.g., PENDING, PROCESSING, COMPLETED, CANCELLED)
     * @param pageable pagination parameters
     * @return paginated order response list matching the status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findOrdersByStatus(status, pageable));
    }

    /**
     * Retrieves orders within a specified date range
     * @param startDate start date of the period (inclusive)
     * @param endDate end date of the period (inclusive)
     * @param pageable pagination parameters
     * @return paginated order response list within the date range
     */
    @GetMapping("/period")
    public ResponseEntity<Page<OrderResponse>> getOrdersByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findOrdersByPeriod(startDate, endDate, pageable));
    }

    /**
     * Performs complex search with multiple filters
     * @param startDate orders created after this date
     * @param status order status filter
     * @param minAmount minimum order amount filter
     * @param pageable pagination parameters
     * @return paginated order response list matching all criteria
     */
    @GetMapping("/complex-search")
    public ResponseEntity<Page<OrderResponse>> complexSearch(
            @RequestParam LocalDateTime startDate,
            @RequestParam OrderStatus status,
            @RequestParam int minAmount,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.searchOrders(startDate, status, minAmount, pageable));
    }

    /**
     * Retrieves order statistics grouped by member
     * @param minAmount optional minimum total amount filter
     * @param pageable pagination parameters
     * @return paginated order statistics including total orders, amount, and average
     */
    @GetMapping("/stats")
    public ResponseEntity<Page<OrderStatisticsResponse>> getOrderStatistics(
            @RequestParam(required = false) Long minAmount,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrderStatistics(minAmount, pageable));
    }

    /**
     * Searches for a specific order by order number
     * @param orderNumber unique order number
     * @return order detail response
     */
    @GetMapping("/search")
    public ResponseEntity<OrderDetailResponse> searchByOrderNumber(
            @RequestParam String orderNumber) {
        return ResponseEntity.ok(orderService.findByOrderNumber(orderNumber));
    }
} 