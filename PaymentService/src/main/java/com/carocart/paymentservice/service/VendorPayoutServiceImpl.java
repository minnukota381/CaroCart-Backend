package com.carocart.paymentservice.service; // or service package

import com.carocart.paymentservice.dto.CreateVendorPayoutRequest;
import com.carocart.paymentservice.dto.LastPayoutWindowDTO;
import com.carocart.paymentservice.dto.VendorPayoutDTO;
import com.carocart.paymentservice.entity.VendorPayout;
import com.carocart.paymentservice.repository.VendorPayoutRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class VendorPayoutServiceImpl implements VendorPayoutService {

  private final VendorPayoutRepository repo;

  public VendorPayoutServiceImpl(VendorPayoutRepository repo) {
    this.repo = repo;
  }

  @Override
  public VendorPayoutDTO createPayout(CreateVendorPayoutRequest req) {
    // Idempotency: same (vendorId, from, to) → same payoutId
    Optional<VendorPayout> existing = repo.findByVendorIdAndFromAtAndToAt(
        req.getVendorId(), req.getFrom(), req.getTo()
    );
    VendorPayout vp = existing.orElseGet(() -> {
      VendorPayout e = new VendorPayout();
      e.setPayoutId(generateId(req.getVendorId(), req.getTo()));
      e.setVendorId(req.getVendorId());
      e.setFromAt(req.getFrom());
      e.setToAt(req.getTo());
      e.setCount(req.getCount() == null ? 0 : req.getCount());
      e.setTotalAmount(req.getTotalAmount() == null ? BigDecimal.ZERO : req.getTotalAmount());
      e.setNote(req.getNote());
      e.setStatus(VendorPayout.PayoutStatus.CREATED);
      e.setWindowHash(req.getWindowHash());
      return repo.save(e);
    });

    return toDTO(vp);
  }

  @Override
  public List<VendorPayoutDTO> listPayouts(Long vendorId, int limit) {
    if (vendorId == null) {
      return repo.findAll(PageRequest.of(0, limit)).map(this::toDTO).toList();
    }
    return repo.findByVendorIdOrderByCreatedAtDesc(vendorId, PageRequest.of(0, limit))
               .stream().map(this::toDTO).toList();
  }

  @Override
  public LastPayoutWindowDTO getLastPayoutWindow(Long vendorId) {
    return repo.findTopByVendorIdOrderByToAtDesc(vendorId)
               .map(vp -> new LastPayoutWindowDTO(vp.getToAt()))
               .orElseGet(() -> new LastPayoutWindowDTO(null));
  }

  private VendorPayoutDTO toDTO(VendorPayout vp) {
    VendorPayoutDTO dto = new VendorPayoutDTO();
    dto.setPayoutId(vp.getPayoutId());
    dto.setVendorId(vp.getVendorId());
    dto.setFrom(vp.getFromAt());
    dto.setTo(vp.getToAt());
    dto.setCount(vp.getCount());
    dto.setTotalAmount(vp.getTotalAmount());
    dto.setStatus(vp.getStatus().name());
    return dto;
  }

  private String generateId(Long vendorId, Instant to) {
    String date = DateTimeFormatter.ofPattern("yyyyMMdd")
        .withZone(ZoneId.of("UTC")).format(to);
    // You can replace this with a DB sequence if you want monotonic IDs globally
    long seq = System.currentTimeMillis() % 1_000_000;
    return "VP_%s_%d_%06d".formatted(date, vendorId, seq);
  }
}
