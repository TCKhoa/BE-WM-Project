package org.wp.wpproject.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wp.wpproject.service.ProductService;
import org.wp.wpproject.service.ExportReceiptService;

@Component
public class CleanupScheduler {

    @Autowired
    private ProductService productService;

    @Autowired
    private ExportReceiptService exportReceiptService;

    // Số ngày giữ lại sản phẩm hoặc phiếu xuất đã xóa mềm trước khi xóa vĩnh viễn
    private static final int DAYS_THRESHOLD = 30;

    /**
     * Tác vụ tự động chạy mỗi ngày lúc 2 giờ sáng
     *
     * Biểu thức cron: "0 0 2 * * ?"
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldDeletedData() {
        // Xóa sản phẩm đã xóa mềm quá hạn
        productService.permanentlyDeleteOldProducts(DAYS_THRESHOLD);
        System.out.println("Dọn dẹp sản phẩm bị xóa mềm cũ hơn " + DAYS_THRESHOLD + " ngày thành công!");

        // Xóa phiếu xuất đã xóa mềm quá hạn
        exportReceiptService.permanentlyDeleteOldExportReceipts(DAYS_THRESHOLD);
        System.out.println("Dọn dẹp phiếu xuất bị xóa mềm cũ hơn " + DAYS_THRESHOLD + " ngày thành công!");
    }
}
