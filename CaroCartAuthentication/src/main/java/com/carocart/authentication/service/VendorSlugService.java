package com.carocart.authentication.service;


import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.util.Slugifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VendorSlugService {

    @Autowired private VendorRepository vendorRepository;
    @Autowired private Slugifier slugifier;

    /**
     * Idempotently ensures vendor.slug and vendor.citySlug are set.
     * - Called right after initial save (ID exists) and on updates only if missing.
     * - Slug is stable and globally unique.
     */
    public void ensureSlugs(Vendor v) {
        if (v == null) return;

        // Fill citySlug once (does NOT change on updates unless missing)
        if (isBlank(v.getCitySlug())) {
            v.setCitySlug(slugifier.toSlug(v.getCity()));
        }

        // Fill slug once (stable). Add a Swiggy-like suffix to guarantee uniqueness.
        if (isBlank(v.getSlug())) {
            String base = slugifier.toSlug(v.getCompanyName());
            String candidate = base + "-rest" + v.getId();
            String unique = candidate;
            int n = 0;
            while (vendorRepository.existsBySlug(unique)) {
                n++;
                unique = candidate + "-" + n;
            }
            v.setSlug(unique);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
