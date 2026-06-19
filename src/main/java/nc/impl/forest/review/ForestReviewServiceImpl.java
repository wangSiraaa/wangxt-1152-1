package nc.impl.forest.review;

import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.EventDispatcher;
import nc.bs.businessevent.IEventType;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.itf.forest.review.IForestReviewService;
import nc.jdbc.framework.SQLParameter;
import nc.rule.forest.ForestBusinessValidator;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.forest.review.ForestReviewVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

import java.util.Collection;

public class ForestReviewServiceImpl implements IForestReviewService {

    public static final String SOURCE_ID = "FOREST_REVIEW";

    private BaseDAO dao = new BaseDAO();

    @Override
    public ForestReviewVO[] queryReviewsByRecord(String pkTrapRecord, String pkOrg) throws BusinessException {
        String condition = "pk_trap_record = ? and pk_org = ? and dr = 0 order by review_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        param.addParam(pkOrg);
        Collection<ForestReviewVO> reviews = dao.retrieveByClause(ForestReviewVO.class, condition, param);
        return reviews != null ? reviews.toArray(new ForestReviewVO[0]) : new ForestReviewVO[0];
    }

    @Override
    public ForestReviewVO[] queryReviewsByQuarantine(String pkQuarantine, String pkOrg) throws BusinessException {
        String condition = "pk_quarantine = ? and pk_org = ? and dr = 0 order by review_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkQuarantine);
        param.addParam(pkOrg);
        Collection<ForestReviewVO> reviews = dao.retrieveByClause(ForestReviewVO.class, condition, param);
        return reviews != null ? reviews.toArray(new ForestReviewVO[0]) : new ForestReviewVO[0];
    }

    @Override
    public ForestReviewVO saveReview(ForestReviewVO reviewVO) throws BusinessException {
        if (reviewVO.getPk_trap_record() == null) {
            ExceptionUtils.wrapBusinessException("诱捕记录不能为空");
        }
        if (reviewVO.getReview_date() == null) {
            ExceptionUtils.wrapBusinessException("复核日期不能为空");
        }
        if (reviewVO.getRisk_level() == null) {
            ExceptionUtils.wrapBusinessException("风险等级不能为空");
        }

        ForestBusinessValidator.validateTrapRecordStatus(
                reviewVO.getPk_trap_record(),
                TrapRecordVO.STATUS_PENDING_REVIEW
        );

        fillAuditFields(reviewVO, true);
        reviewVO.setStatus(VOStatus.NEW);
        dao.insertVO(reviewVO);

        updateTrapRecordStatus(reviewVO.getPk_trap_record(), reviewVO.getRisk_level(), reviewVO.getIs_allow_disposal());

        fireEvent(reviewVO, IEventType.TYPE_INSERT_AFTER);

        return reviewVO;
    }

    @Override
    public ForestReviewVO updateReview(ForestReviewVO reviewVO) throws BusinessException {
        if (reviewVO.getPk_forest_review() == null) {
            ExceptionUtils.wrapBusinessException("复核记录主键不能为空");
        }

        fillAuditFields(reviewVO, false);
        reviewVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(reviewVO);

        fireEvent(reviewVO, IEventType.TYPE_UPDATE_AFTER);

        return reviewVO;
    }

    @Override
    public void deleteReview(String pkReview) throws BusinessException {
        if (pkReview == null) {
            ExceptionUtils.wrapBusinessException("复核记录主键不能为空");
        }
        String sql = "update forest_review set dr = 1, modifier = ?, modifiedtime = ?, ts = ? where pk_forest_review = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkReview);
        dao.executeUpdate(sql, param);
    }

    @Override
    public ForestReviewVO queryReviewById(String pkReview) throws BusinessException {
        String condition = "pk_forest_review = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkReview);
        Collection<ForestReviewVO> reviews = dao.retrieveByClause(ForestReviewVO.class, condition, param);
        if (reviews != null && reviews.size() > 0) {
            return reviews.iterator().next();
        }
        return null;
    }

    @Override
    public ForestReviewVO queryLatestReview(String pkTrapRecord) throws BusinessException {
        String sql = "select * from forest_review where pk_trap_record = ? and dr = 0 order by creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        return (ForestReviewVO) dao.executeQuery(sql, param, new nc.jdbc.framework.processor.BeanProcessor(ForestReviewVO.class));
    }

    private void updateTrapRecordStatus(String pkTrapRecord, Integer riskLevel, Integer allowDisposal) throws BusinessException {
        String sql = "update forest_trap_record set record_status = ?, risk_level = ?, modifier = ?, modifiedtime = ?, ts = ? "
                + "where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(TrapRecordVO.STATUS_REVIEWED);
        param.addParam(riskLevel);
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkTrapRecord);
        dao.executeUpdate(sql, param);
    }

    private void fillAuditFields(ForestReviewVO vo, boolean isNew) {
        String userId = InvocationInfoProxy.getInstance().getUserId();
        String pkGroup = InvocationInfoProxy.getInstance().getGroupId();
        String pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        UFDateTime now = new UFDateTime();

        if (vo.getPk_group() == null) {
            vo.setPk_group(pkGroup);
        }
        if (vo.getPk_org() == null) {
            vo.setPk_org(pkOrg);
        }
        if (vo.getPk_quarantine() == null) {
            vo.setPk_quarantine(userId);
        }
        if (vo.getIs_quarantine() == null) {
            vo.setIs_quarantine(ForestReviewVO.NOT_QUARANTINE);
        }
        if (vo.getIs_allow_disposal() == null) {
            vo.setIs_allow_disposal(ForestReviewVO.NOT_ALLOW_DISPOSAL);
        }

        if (isNew) {
            vo.setCreator(userId);
            vo.setCreationtime(now);
        }
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }

    private void fireEvent(ForestReviewVO reviewVO, String eventType) {
        try {
            BusinessEvent event = new BusinessEvent(SOURCE_ID, eventType, reviewVO);
            EventDispatcher.fireEvent(event);
        } catch (Exception e) {
        }
    }
}
