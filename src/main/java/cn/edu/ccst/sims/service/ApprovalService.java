package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.vo.BookingApprovalVO;
import cn.edu.ccst.sims.vo.RentalApprovalVO;
import java.util.List;

public interface ApprovalService {
    List<BookingApprovalVO> getPendingBookings();
    List<RentalApprovalVO> getPendingRentals();

    // 审核预约: pass=true通过(1), false驳回(2)
    void auditBooking(Long id, boolean pass);

    // 审核借用: pass=true通过/使用中(2), false驳回(4)
    void auditRental(Long id, boolean pass);
}