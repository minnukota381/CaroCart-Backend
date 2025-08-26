package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.AdminEarningsPreviewDTO;
import com.carocart.delivery.dto.DeliveryEarningCreateRequest;
import com.carocart.delivery.dto.DeliveryPayoutDTO;
import com.carocart.delivery.dto.EarningsSummaryDTO;
import com.carocart.delivery.entity.DeliveryEarning;
import com.carocart.delivery.entity.DeliveryPayout;
import com.carocart.delivery.repository.DeliveryEarningRepository;
import com.carocart.delivery.repository.DeliveryPayoutRepository;
import com.carocart.delivery.service.DeliveryEarningService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class DeliveryEarningServiceImpl implements DeliveryEarningService {

    private final DeliveryEarningRepository repo;
    private final DeliveryPayoutRepository payoutRepo;

    // ✅ Safe baseline for MySQL DATETIME (instead of LocalDateTime.MIN)
    private static final LocalDateTime BASELINE =
            LocalDateTime.of(1970, 1, 1, 0, 0);

    public DeliveryEarningServiceImpl(DeliveryEarningRepository repo, DeliveryPayoutRepository payoutRepo) {
        this.repo = repo;
        this.payoutRepo = payoutRepo;
    }

    @Override
    @Transactional
    public DeliveryEarning createInternal(DeliveryEarningCreateRequest req) {
        if (repo.existsByOrderItemId(req.getOrderItemId())) {
            // Idempotent: return existing row
            return repo.findByDeliveryBoyIdOrderByDeliveredAtDesc(req.getDeliveryBoyId())
                    .stream()
                    .filter(e -> e.getOrderItemId().equals(req.getOrderItemId()))
                    .findFirst()
                    .orElse(null);
        }
        DeliveryEarning e = new DeliveryEarning();
        e.setOrderItemId(req.getOrderItemId());
        e.setDeliveryBoyId(req.getDeliveryBoyId());
        e.setFee(nz(req.getFee()));
        e.setTip(nz(req.getTip()));
        e.setPenalty(nz(req.getPenalty()));
        e.setNetAmount(e.getFee() + e.getTip() - e.getPenalty());
        e.setDeliveredAt(req.getDeliveredAt() != null ? req.getDeliveredAt() : LocalDateTime.now());
        e.setStatus("PENDING");
        return repo.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryEarning> listMine(Long deliveryBoyId, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return repo.findByDeliveryBoyIdOrderByDeliveredAtDesc(deliveryBoyId);
        }
        LocalDateTime f = from != null ? from.atStartOfDay() : BASELINE;
        LocalDateTime t = to != null ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        return repo.findByDeliveryBoyIdAndDeliveredAtBetweenOrderByDeliveredAtDesc(deliveryBoyId, f, t);
    }

    @Override
    @Transactional(readOnly = true)
    public EarningsSummaryDTO summary(Long deliveryBoyId, String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;
        LocalDateTime to = now;

        switch (period == null ? "" : period.toLowerCase()) {
            case "today": from = now.toLocalDate().atStartOfDay(); break;
            case "week":  from = now.minusDays(7).toLocalDate().atStartOfDay(); break;
            case "month": from = now.minusDays(30).toLocalDate().atStartOfDay(); break;
            default:      from = now.minusDays(7).toLocalDate().atStartOfDay();
        }

        List<DeliveryEarning> rows =
                repo.findByDeliveryBoyIdAndDeliveredAtBetweenOrderByDeliveredAtDesc(deliveryBoyId, from, to);

        double fee = rows.stream().mapToDouble(DeliveryEarning::getFee).sum();
        double tip = rows.stream().mapToDouble(DeliveryEarning::getTip).sum();
        double pen = rows.stream().mapToDouble(DeliveryEarning::getPenalty).sum();
        double net = rows.stream().mapToDouble(DeliveryEarning::getNetAmount).sum();

        return new EarningsSummaryDTO(rows.size(), fee, tip, pen, net);
    }

    // ---------- Admin flow ----------

    @Override
    @Transactional(readOnly = true)
    public AdminEarningsPreviewDTO previewUnpaid(Long deliveryBoyId) {
        LocalDateTime last = repo.findLastPayoutAt(deliveryBoyId);
        LocalDateTime since = (last != null) ? last : BASELINE;
        var items = repo.findUnpaidSince(deliveryBoyId, since);
        double total = items.stream().mapToDouble(DeliveryEarning::getNetAmount).sum();
        return new AdminEarningsPreviewDTO(
                deliveryBoyId, since, LocalDateTime.now(), items.size(), total, items
        );
    }

    @Override
    @Transactional
    public DeliveryPayoutDTO markPaid(Long deliveryBoyId, Long adminIdOrNull, String notes) {
        LocalDateTime last = repo.findLastPayoutAt(deliveryBoyId);
        LocalDateTime since = (last != null) ? last : BASELINE;
        var items = repo.findUnpaidSince(deliveryBoyId, since);
        if (items.isEmpty()) {
            throw new RuntimeException("Nothing to pay for this delivery boy.");
        }

        double total = items.stream().mapToDouble(DeliveryEarning::getNetAmount).sum();
        var payout = new DeliveryPayout();
        payout.setDeliveryBoyId(deliveryBoyId);
        payout.setTotalAmount(total);
        payout.setItems(items.size());
        payout.setNotes(notes);
        payout.setMarkedByAdminId(adminIdOrNull);
        payout.setWindowFrom(since);
        payout.setWindowTo(LocalDateTime.now());
        payout = payoutRepo.save(payout);

        LocalDateTime ts = payout.getCreatedAt();
        Long payoutId = payout.getId();
        for (DeliveryEarning e : items) {
            e.setStatus("PAID");
            e.setPayoutId(payoutId);
            e.setPaidAt(ts);
        }
        repo.saveAll(items);

        return new DeliveryPayoutDTO(
                payout.getId(), payout.getDeliveryBoyId(), payout.getTotalAmount(),
                payout.getItems(), payout.getCreatedAt(), payout.getNotes()
        );
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
}
