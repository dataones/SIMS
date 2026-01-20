package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.entity.TbBooking;
import cn.edu.ccst.sims.entity.TbEquipmentRental;
import cn.edu.ccst.sims.entity.TbRefund;
import cn.edu.ccst.sims.mapper.TbBookingMapper;
import cn.edu.ccst.sims.mapper.TbEquipmentRentalMapper;
import cn.edu.ccst.sims.mapper.TbRefundMapper;
import cn.edu.ccst.sims.vo.BookingApprovalVO;
import cn.edu.ccst.sims.vo.RentalApprovalVO;
import cn.edu.ccst.sims.vo.RefundVO;
import cn.edu.ccst.sims.service.ApprovalService;
import cn.edu.ccst.sims.service.TbRefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private TbBookingMapper bookingMapper;
    @Autowired
    private TbEquipmentRentalMapper rentalMapper;
    @Autowired
    private TbRefundMapper refundMapper;
    @Autowired
    private TbRefundService refundService;

    @Override
    public List<BookingApprovalVO> getPendingBookings() {
        return bookingMapper.selectPendingBookings();
    }

    @Override
    public List<RentalApprovalVO> getPendingRentals() {
        return rentalMapper.selectPendingRentals();
    }

    @Override
    public List<RefundVO> getPendingRefunds() {
        return refundService.getPendingRefunds();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBooking(Long id, boolean pass) {
        TbBooking booking = new TbBooking();
        booking.setId(id);
        // 0-待审核, 1-已通过, 2-已驳回
        booking.setStatus(pass ? 1 : 2);
        bookingMapper.updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRental(Long id, boolean pass) {
        TbEquipmentRental rental = new TbEquipmentRental();
        rental.setId(id);
        // 1-申请中, 2-使用中(通过), 4-已驳回
        rental.setStatus(pass ? 2 : 4);
        rentalMapper.updateById(rental);

        // 注意：如果是驳回，理论上应该恢复库存（如果申请时已经扣减了库存）。
        // 这里假设申请时只是生成记录，通过时才真正去拿器材，或者库存逻辑另有处理。
        // 为简化逻辑，这里只改状态。
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(Long id, boolean pass) {
        // 获取当前审核人ID（这里简化处理，实际应该从SecurityContext获取）
        Long auditUserId = 1L; // 默认管理员ID

        // 调用退款服务进行审核
        refundService.auditRefund(id, auditUserId, pass, "");
    }
}