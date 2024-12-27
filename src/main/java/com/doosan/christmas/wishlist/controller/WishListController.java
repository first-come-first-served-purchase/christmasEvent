package com.doosan.christmas.wishlist.controller;

import com.doosan.christmas.member.domain.UserDetailsImpl;
import com.doosan.christmas.wishlist.dto.requestDto.WishListRequestDto;
import com.doosan.christmas.wishlist.dto.responseDto.WishListResponseDto;
import com.doosan.christmas.wishlist.service.WishListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 위시 리스트 API 컨트롤러
 * <p>
 * 위시 리스트 관련 CRUD 기능을 제공 하는 API를 관리 합니다.
 */
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "위시리스트 API", description = "위시리스트 관련 API")
public class WishListController {

    private final WishListService wishListService;

    /**
     * 위시 리스트 에 상품 추가
     *
     * @param request     추가할 상품의 정보가 담긴 요청 객체
     * @param userDetails 현재 인증된 사용자 정보
     * @return 추가된 위시 리스트 항목 정보
     */
    @PostMapping
    @Operation(summary = "위시리스트 추가", description = "상품을 위시리스트에 추가합니다.")
    public ResponseEntity<WishListResponseDto> addToWishList(@RequestBody @Valid WishListRequestDto request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(wishListService.addToWishList(userDetails.getMember().getId(), request)
        );
    }

    /**
     * 위시 리스트 목록 조회
     *
     * @param pageable    페이징 처리를 위한 객체
     * @param userDetails 현재 인증된 사용자 정보
     * @return 위시 리스트 항목 목록 (페이징 처리됨)
     */
    @GetMapping
    @Operation(summary = "위시리스트 조회", description = "위시리스트 목록을 조회합니다.")
    public ResponseEntity<Page<WishListResponseDto>> getWishList(@PageableDefault(size = 10) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(wishListService.getWishList(userDetails.getMember().getId(), pageable)
        );
    }

    /**
     * 위시 리스트 상품의 수량 수정
     *
     * @param productId   수정할 상품의 ID
     * @param quantity    수정할 수량
     * @param userDetails 현재 인증된 사용자 정보
     * @return 수정된 위시 리스트 항목 정보
     */
    @PutMapping("/{productId}")
    @Operation(summary = "위시리스트 수량 수정", description = "위시리스트 상품의 수량을 수정합니다.")
    public ResponseEntity<WishListResponseDto> updateWishListQuantity(@PathVariable Long productId, @RequestParam Long quantity, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(wishListService.updateWishListQuantity(userDetails.getMember().getId(), productId, quantity)
        );
    }

    /**
     * 위시 리스트 에서 상품 삭제
     *
     * @param productId   삭제할 상품의 ID
     * @param userDetails 현재 인증된 사용자 정보
     * @return 삭제 처리 결과
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "위시리스트 삭제", description = "위시리스트에서 상품을 삭제합니다.")
    public ResponseEntity<Void> removeFromWishList(@PathVariable Long productId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        wishListService.removeFromWishList(userDetails.getMember().getId(), productId);
        return ResponseEntity.ok().build();
    }
}
