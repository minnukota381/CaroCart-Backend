package com.carocart.paymentservice.controller;

import com.carocart.paymentservice.dto.CreateVendorPayoutRequest;
import com.carocart.paymentservice.dto.VendorPayoutDTO;
import com.carocart.paymentservice.dto.LastPayoutWindowDTO; // create this DTO if you expose /last-to
import com.carocart.paymentservice.service.VendorPayoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/vendor-payouts")
public class VendorPayoutController {

    private final VendorPayoutService payoutService;

    public VendorPayoutController(VendorPayoutService payoutService) {
        this.payoutService = payoutService;
    }

    // Order → Payment: create payout (idempotent by (vendorId, from, to))
    @PostMapping
    public VendorPayoutDTO create(@RequestBody CreateVendorPayoutRequest body) {
        return payoutService.createPayout(body);
    }

    // (Optional) Admin history (use later in UI)
    @GetMapping("/admin")
    public List<VendorPayoutDTO> list(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(defaultValue = "50") int limit) {
        return payoutService.listPayouts(vendorId, limit);
    }

    // (Optional) Let Order svc ask Payment for the last payout window end
    @GetMapping("/last-to")
    public LastPayoutWindowDTO lastTo(@RequestParam Long vendorId) {
        return payoutService.getLastPayoutWindow(vendorId); // returns {from,to} or nulls
    }
}
