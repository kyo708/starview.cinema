package com.starview.cinemabooking.dtos;

import com.starview.cinemabooking.model.KhuyenMai;

import lombok.Data;

@Data
public class VoucherApplyResult {
    private float originalPrice;
    private float discountedPrice;
    private float discountAmount;
    private KhuyenMai khuyenMai;
}
