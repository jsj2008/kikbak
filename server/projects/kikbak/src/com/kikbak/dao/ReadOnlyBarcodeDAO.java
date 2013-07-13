package com.kikbak.dao;

import com.kikbak.dto.Barcode;


public interface ReadOnlyBarcodeDAO {

    public Barcode findById(Long id);
    public Barcode findByCode(String code);
    public Barcode findByUserIdAndAllocatedGift(final Long userId, final Long allocatedGiftId);
    
}