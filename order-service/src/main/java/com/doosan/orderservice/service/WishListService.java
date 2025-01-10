package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.orderservice.dto.*;
import com.doosan.orderservice.entity.Order;
import com.doosan.orderservice.entity.OrderItem;
import com.doosan.orderservice.entity.WishList;
import com.doosan.orderservice.repository.WishListRepository;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class WishListService {
    private final WishListRepository wishListRepository;
    private final OrderService orderService;
    private final ProductService productService;

    public Mono<ResponseEntity<ResponseDto<List<WishListDto>>>> getWishList(int userId) {
        return Mono.fromCallable(() -> {
            List<WishList> wishListItems = wishListRepository.findByUserIdAndIsDeletedFalse(userId);
            List<WishListDto> wishListDtos = wishListItems.stream()
                    .map(this::convertToDto)
                    .toList();

            return ResponseEntity.ok(
                    ResponseDto.<List<WishListDto>>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("위시리스트 조회 성공")
                            .data(wishListDtos)
                            .build()
            );
        })
        .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<List<WishListDto>>builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .resultMessage("위시리스트 조회 실패")
                        .build()
                )))
        .subscribeOn(Schedulers.boundedElastic());
    }

    private WishListDto convertToDto(WishList item) {
        WishListDto dto = new WishListDto();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());

        ResponseDto<ProductResponse> productResponse = productService.getProduct(item.getProductId()).getBody();
        if (productResponse != null && productResponse.getData() != null) {
            ProductResponse product = productResponse.getData();
            dto.setProductName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setDescription(product.getDescription());
        }
        return dto;
    }

    public Mono<ResponseEntity<ResponseDto<Void>>> addToWishList(int userId, Long productId, int quantity) {
        return Mono.fromCallable(() -> {
            Optional<WishList> existingWishList = wishListRepository.findByUserIdAndProductIdAndIsDeletedFalse(userId, productId);
            if (existingWishList.isPresent()) {
                throw new BusinessRuntimeException("이미 위시리스트에 존재하는 상품입니다.");
            }

            WishList wishList = WishList.builder()
                    .userId(userId)
                    .productId(productId)
                    .quantity(quantity)
                    .build();

            wishListRepository.save(wishList);

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("위시리스트에 상품이 추가되었습니다.")
                            .build()
            );
        })
        .onErrorResume(BusinessRuntimeException.class, e ->
            Mono.just(ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    ))
        )
        .onErrorResume(Exception.class, e ->
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage("위시리스트 추가 실패")
                            .build()
                    ))
        )
        .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ResponseEntity<ResponseDto<Void>>> updateWishListItem(int userId, Long productId, int quantity) {
        return Mono.fromCallable(() -> {
            WishList wishList = wishListRepository.findByUserIdAndProductIdAndIsDeletedFalse(userId, productId)
                    .orElseThrow(() -> new BusinessRuntimeException("위시리스트에 존재하지 않는 상품입니다."));

            wishList.setQuantity(quantity);
            wishListRepository.save(wishList);

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("위시리스트 상품 수량이 수정되었습니다.")
                            .build()
            );
        })
        .onErrorResume(BusinessRuntimeException.class, e ->
            Mono.just(ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    ))
        )
        .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ResponseEntity<ResponseDto<Void>>> removeFromWishList(int userId, Long productId) {
        return Mono.fromCallable(() -> {
            WishList wishList = wishListRepository.findByUserIdAndProductIdAndIsDeletedFalse(userId, productId)
                    .orElseThrow(() -> new BusinessRuntimeException("위시리스트에 존재하지 않는 상품입니다."));

            wishList.setDeleted(true);
            wishListRepository.save(wishList);

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("위시리스트에서 상품이 제거되었습니다.")
                            .build()
            );
        })
        .onErrorResume(BusinessRuntimeException.class, e ->
            Mono.just(ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    ))
        )
        .onErrorResume(Exception.class, e ->
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage("위시리스트 제거 실패")
                            .build()
                    ))
        )
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public ResponseEntity<ResponseDto<WishListOrderResponseDto>> orderFromWishList(int userId, List<Long> productIds) {
        try {
            log.info("위시리스트 주문 시작 - userId: {}, productIds: {}", userId, productIds);
            
            // 위시리스트 아이템 조회
            List<WishList> wishListItems = wishListRepository.findByUserIdAndIsDeletedFalse(userId);
            log.info("조회된 위시리스트 아이템: {}", wishListItems.size());
            
            // productId를 Long으로 변환하여 비교
            List<WishList> filteredItems = wishListItems.stream()
                    .peek(item -> log.info("위시리스트 아이템 - productId: {}", item.getProductId()))
                    .filter(item -> {
                        boolean contains = productIds.contains(item.getProductId());
                        log.info("상품 ID {} 포함 여부: {}", item.getProductId(), contains);
                        return contains;
                    })
                    .toList();
            log.info("필터링된 위시리스트 아이템: {}", filteredItems.size());

            if (filteredItems.isEmpty()) {
                throw new BusinessRuntimeException("주문할 상품이 위시리스트에 없습니다.");
            }

            // 주문 생성을 위한 요청 아이템 리스트 생성
            List<CreateOrderReqDto> orderItemRequests = filteredItems.stream()
                    .map(item -> {
                        log.info("주문 아이템 변환 - productId: {}, quantity: {}", item.getProductId(), item.getQuantity());
                        return CreateOrderReqDto.builder()
                                .productId(Long.valueOf(item.getProductId()))
                                .quantity((long) item.getQuantity())
                                .build();
                    })
                    .toList();

            // 주문 생성
            CreateOrderResDto orderResult = orderService.createOrder(userId, orderItemRequests);
            log.info("주문 생성 완료 - orderId: {}", orderResult.getOrderId());
            
            // 주문 성공 시 위시리스트 아이템 삭제 처리
            filteredItems.forEach(item -> item.setDeleted(true));
            wishListRepository.saveAll(filteredItems);

            // 응답 데이터 생성
            WishListOrderResponseDto responseData = WishListOrderResponseDto.builder()
                    .orderId(orderResult.getOrderId())
                    .userId(orderResult.getUserId())
                    .orderDate(orderResult.getOrderDate())
                    .totalPrice(orderResult.getTotalPrice())
                    .items(orderItemRequests.stream()
                            .map(item -> {
                                ResponseDto<ProductResponse> productResponse = productService.getProduct(item.getProductId()).getBody();
                                if (productResponse != null && productResponse.getData() != null) {
                                    ProductResponse product = productResponse.getData();
                                    return OrderItemDto.builder()
                                            .productId(item.getProductId())
                                            .productName(product.getName())
                                            .quantity(Math.toIntExact(item.getQuantity()))
                                            .price(product.getPrice())
                                            .build();
                                }
                                throw new BusinessRuntimeException("상품 정보를 조회할 수 없습니다.");
                            })
                            .toList())
                    .build();

            return ResponseEntity.ok(
                    ResponseDto.<WishListOrderResponseDto>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("위시리스트 상품 주문이 완료되었습니다.")
                            .data(responseData)
                            .build()
            );

        } catch (BusinessRuntimeException e) {
            log.error("위시리스트 주문 실패 - BusinessRuntimeException: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResponseDto.<WishListOrderResponseDto>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            log.error("위시리스트 주문 실패 - Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<WishListOrderResponseDto>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage("위시리스트 주문 처리 중 오류가 발생했습니다.")
                            .build()
                    );
        }
    }

    private OrderItemDto convertToOrderItemDto(OrderItem item) {
        ResponseDto<ProductResponse> productResponse = productService.getProduct(Long.valueOf(item.getProductId())).getBody();
        if (productResponse != null && productResponse.getData() != null) {
            ProductResponse product = productResponse.getData();
            return OrderItemDto.builder()
                    .productId(Long.valueOf(item.getProductId()))
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();
        }
        throw new BusinessRuntimeException("상품 정보를 조회할 수 없습니다.");
    }
}
