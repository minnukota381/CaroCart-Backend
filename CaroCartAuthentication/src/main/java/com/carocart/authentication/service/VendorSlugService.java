package com.carocart.authentication.service;

import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.util.Slugifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VendorSlugService {

    @Autowired private VendorRepository vendorRepository;
    @Autowired private Slugifier slugifier;

    /**
     * Ensure vendor.slug and vendor.citySlug are set for NEW or EXISTING vendor.
     * - For new vendors (no ID yet), slug is based on companyName + citySlug.
     * - For persisted vendors (ID exists), we still allow base from company+city but
     *   keep it unique. (You can switch to "-rest{id}" style if you prefer.)
     * - Only creates values if missing.
     */
    public void ensureSlugs(Vendor v) {
        if (v == null) return;

        // citySlug
        if (isBlank(v.getCitySlug()) && StringUtils.hasText(v.getCity())) {
            v.setCitySlug(toSlug(v.getCity()));
        }

        // slug
        if (isBlank(v.getSlug()) && StringUtils.hasText(v.getCompanyName())) {
            String base = toSlug(v.getCompanyName());
            // include city for nicer SEO + lower collision chance
            if (StringUtils.hasText(v.getCitySlug())) {
                base = base + "-" + v.getCitySlug();
            }
            v.setSlug(makeUnique(base, v.getId()));
        }
    }

    /**
     * Recompute slugs on update if companyName or city changed.
     * - If either changed: recompute citySlug from new city; recompute slug from new company+city.
     * - Keeps uniqueness and avoids changing slugs unnecessarily.
     */
    public void ensureSlugsOnUpdate(Vendor before, Vendor after) {
        if (before == null || after == null) return;

        boolean nameChanged = !safeEq(before.getCompanyName(), after.getCompanyName());
        boolean cityChanged = !safeEq(before.getCity(), after.getCity());

        // If city changed, always recompute citySlug
        if (cityChanged && StringUtils.hasText(after.getCity())) {
            after.setCitySlug(toSlug(after.getCity()));
        } else if (isBlank(after.getCitySlug()) && StringUtils.hasText(after.getCity())) {
            // backfill if missing
            after.setCitySlug(toSlug(after.getCity()));
        }

        // Recompute slug only if name or city changed OR slug is blank
        if (nameChanged || cityChanged || isBlank(after.getSlug())) {
            if (StringUtils.hasText(after.getCompanyName())) {
                String base = toSlug(after.getCompanyName());
                if (StringUtils.hasText(after.getCitySlug())) {
                    base = base + "-" + after.getCitySlug();
                }
                after.setSlug(makeUnique(base, after.getId()));
            }
        }
    }

    // ----------------- helpers -----------------

    private String makeUnique(String base, Long currentId) {
        String candidate = base;
        int n = 0;
        while (existsSlugForOther(candidate, currentId)) {
            n++;
            candidate = base + "-" + n;
        }
        return candidate;
    }

    private boolean existsSlugForOther(String slug, Long currentId) {
        // If repository has a method to fetch by slug and compare IDs, use that.
        var exists = vendorRepository.findBySlug(slug);
        return exists.isPresent() && (currentId == null || !exists.get().getId().equals(currentId));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean safeEq(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    private String toSlug(String s) {
        if (!StringUtils.hasText(s)) return "";
        String normalized = s.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
        return normalized.replaceAll("^-|-$", "");
    }
}
