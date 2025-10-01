//package com.carocart.productservice.controller;
//
//import com.carocart.productservice.entity.Category;
//import com.carocart.productservice.entity.Product;
//import com.carocart.productservice.entity.SubCategory;
//import com.carocart.productservice.service.CategoryService;
//import com.carocart.productservice.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//public class SitemapController {
//
//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private CategoryService categoryService;
//
//    @Value("${sitemap.base-url}")
//    private String BASE_URL;
//
//    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
//    public String generateSitemap() {
//        List<Product> products = productService.getAllProducts();
//        List<Category> categories = categoryService.getAllCategories();
//
//        String today = LocalDate.now().toString();
//        StringBuilder xml = new StringBuilder();
//        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
//
//        // Homepage
//        appendUrl(xml, BASE_URL + "/", today, "daily", "1.0");
//
//        // Categories and Subcategories using SLUGS
//        for (Category category : categories) {
//            if (category.getSlug() != null && !category.getSlug().isEmpty()) {
//                appendUrl(xml, BASE_URL + "/category/" + category.getSlug(), today, "weekly", "0.8");
//            }
//
//            List<SubCategory> subcategories = categoryService.getSubCategoriesByCategoryId(category.getId());
//            for (SubCategory sub : subcategories) {
//                if (sub.getSlug() != null && !sub.getSlug().isEmpty()) {
//                    appendUrl(xml, BASE_URL + "/subcategory/" + sub.getSlug(), today, "weekly", "0.7");
//                }
//            }
//        }
//
//        // Products using SLUG
//        for (Product product : products) {
//            if (product.getSlug() != null && !product.getSlug().isEmpty()) {
//                String productUrl = BASE_URL + "/product/" + product.getSlug();
//                appendUrl(xml, productUrl, today, "weekly", "0.9");
//            }
//        }
//
//        xml.append("</urlset>");
//        return xml.toString();
//    }
//
//    private void appendUrl(StringBuilder xml, String loc, String lastmod, String changefreq, String priority) {
//        xml.append("  <url>\n");
//        xml.append("    <loc>").append(loc).append("</loc>\n");
//        xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
//        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
//        xml.append("    <priority>").append(priority).append("</priority>\n");
//        xml.append("  </url>\n");
//    }
//}
