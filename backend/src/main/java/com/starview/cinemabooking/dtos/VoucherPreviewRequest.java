package com.starview.cinemabooking.dtos;

import lombok.Data;

@Data
public class VoucherPreviewRequest {
    private String voucherCode;
    private float originalPrice;
}