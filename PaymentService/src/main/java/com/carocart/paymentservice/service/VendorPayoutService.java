package com.carocart.paymentservice.service; // or service package if you prefer

import com.carocart.paymentservice.dto.CreateVendorPayoutRequest;
import com.carocart.paymentservice.dto.LastPayoutWindowDTO;
import com.carocart.paymentservice.dto.VendorPayoutDTO;

import java.util.List;

public interface VendorPayoutService {
  VendorPayoutDTO createPayout(CreateVendorPayoutRequest req);     // idempotent
  List<VendorPayoutDTO> listPayouts(Long vendorId, int limit);     // optional
  LastPayoutWindowDTO getLastPayoutWindow(Long vendorId);          // optional
}
