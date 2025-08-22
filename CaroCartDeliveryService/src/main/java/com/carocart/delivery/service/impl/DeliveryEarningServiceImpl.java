package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.DeliveryEarningCreateRequest;
import com.carocart.delivery.dto.EarningsSummaryDTO;
import com.carocart.delivery.entity.DeliveryEarning;
import com.carocart.delivery.repository.DeliveryEarningRepository;
import com.carocart.delivery.service.DeliveryEarningService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DeliveryEarningServiceImpl implements DeliveryEarningService {

    private final DeliveryEarningRepository repo;

    public DeliveryEarningServiceImpl(DeliveryEarningRepository repo) {
        this.repo = repo;
    }

    @Override
    public DeliveryEarning createInternal(DeliveryEarningCreateRequest req) {
        if (repo.existsByOrderItemId(req.getOrderItemId())) {
            // idempotent
            return repo.findByDeliveryBoyIdOrderByDeliveredAtDesc(req.getDeliveryBoyId()).stream()
                    .filter(e -> e.getOrderItemId().equals(req.getOrderItemId()))
                    .findFirst().orElse(null);
        }
        DeliveryEarning e = new DeliveryEarning();
        e.setOrderItemId(req.getOrderItemId());
        e.setDeliveryBoyId(req.getDeliveryBoyId());
        e.setFee(nullSafe(req.getFee()));
        e.setTip(nullSafe(req.getTip()));
        e.setPenalty(nullSafe(req.getPenalty()));
        e.setNetAmount(e.getFee() + e.getTip() - e.getPenalty());
        e.setDeliveredAt(req.getDeliveredAt() != null ? req.getDeliveredAt() : LocalDateTime.now());
        e.setStatus("PENDING");
        return repo.save(e);
    }

    @Override
    public List<DeliveryEarning> listMine(Long deliveryBoyId, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return repo.findByDeliveryBoyIdOrderByDeliveredAtDesc(deliveryBoyId);
        }
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime t = to != null ? to.atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);
        return repo.findByDeliveryBoyIdAndDeliveredAtBetweenOrderByDeliveredAtDesc(deliveryBoyId, f, t);
        }

    @Override
    public EarningsSummaryDTO summary(Long deliveryBoyId, String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;
        LocalDateTime to = now;

        switch (period == null ? "" : period.toLowerCase()) {
            case "today":
                from = now.toLocalDate().atStartOfDay(); break;
            case "week":
                from = now.minusDays(7).toLocalDate().atStartOfDay(); break;
            case "month":
                from = now.minusDays(30).toLocalDate().atStartOfDay(); break;
            default:
                from = now.minusDays(7).toLocalDate().atStartOfDay(); // default week
        }

        List<DeliveryEarning> rows = repo.findByDeliveryBoyIdAndDeliveredAtBetweenOrderByDeliveredAtDesc(
                deliveryBoyId, from, to);

        double fee = rows.stream().mapToDouble(DeliveryEarning::getFee).sum();
        double tip = rows.stream().mapToDouble(DeliveryEarning::getTip).sum();
        double pen = rows.stream().mapToDouble(DeliveryEarning::getPenalty).sum();
        double net = rows.stream().mapToDouble(DeliveryEarning::getNetAmount).sum();

        return new EarningsSummaryDTO(rows.size(), fee, tip, pen, net);
    }

    private double nullSafe(Double v) { return v == null ? 0.0 : v; }
}
