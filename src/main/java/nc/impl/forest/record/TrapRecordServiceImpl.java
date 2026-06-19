package nc.impl.forest.record;

import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.EventDispatcher;
import nc.bs.businessevent.IEventType;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.itf.forest.record.ITrapRecordService;
import nc.jdbc.framework.SQLParameter;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

import java.util.Collection;

public class TrapRecordServiceImpl implements ITrapRecordService {

    public static final String SOURCE_ID = "FOREST_TRAP_RECORD";

    private BaseDAO dao = new BaseDAO();

    @Override
    public TrapRecordVO[] queryRecordsByTrap(String pkTrap, String pkOrg) throws BusinessException {
        String condition = "pk_forest_trap = ? and pk_org = ? and dr = 0 order by record_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrap);
        param.addParam(pkOrg);
        Collection<TrapRecordVO> records = dao.retrieveByClause(TrapRecordVO.class, condition, param);
        return records != null ? records.toArray(new TrapRecordVO[0]) : new TrapRecordVO[0];
    }

    @Override
    public TrapRecordVO[] queryPendingReviewRecords(String pkOrg) throws BusinessException {
        String condition = "record_status = ? and pk_org = ? and dr = 0 order by record_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(TrapRecordVO.STATUS_PENDING_REVIEW);
        param.addParam(pkOrg);
        Collection<TrapRecordVO> records = dao.retrieveByClause(TrapRecordVO.class, condition, param);
        return records != null ? records.toArray(new TrapRecordVO[0]) : new TrapRecordVO[0];
    }

    @Override
    public TrapRecordVO[] queryRecordsByRanger(String pkRanger, String pkOrg) throws BusinessException {
        String condition = "pk_ranger = ? and pk_org = ? and dr = 0 order by record_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkRanger);
        param.addParam(pkOrg);
        Collection<TrapRecordVO> records = dao.retrieveByClause(TrapRecordVO.class, condition, param);
        return records != null ? records.toArray(new TrapRecordVO[0]) : new TrapRecordVO[0];
    }

    @Override
    public TrapRecordVO saveRecord(TrapRecordVO recordVO) throws BusinessException {
        if (recordVO.getPk_forest_trap() == null) {
            ExceptionUtils.wrapBusinessException("诱捕器不能为空");
        }
        if (recordVO.getRecord_date() == null) {
            ExceptionUtils.wrapBusinessException("记录日期不能为空");
        }
        if (recordVO.getInsect_type() == null || recordVO.getInsect_type().trim().isEmpty()) {
            ExceptionUtils.wrapBusinessException("虫类不能为空");
        }
        if (recordVO.getInsect_count() == null || recordVO.getInsect_count() < 0) {
            ExceptionUtils.wrapBusinessException("虫情数量不能为空且不能为负数");
        }

        fillAuditFields(recordVO, true);
        recordVO.setRecord_status(TrapRecordVO.STATUS_PENDING_REVIEW);
        recordVO.setStatus(VOStatus.NEW);
        dao.insertVO(recordVO);

        fireEvent(recordVO, IEventType.TYPE_INSERT_AFTER);

        return recordVO;
    }

    @Override
    public TrapRecordVO updateRecord(TrapRecordVO recordVO) throws BusinessException {
        if (recordVO.getPk_trap_record() == null) {
            ExceptionUtils.wrapBusinessException("记录主键不能为空");
        }

        fillAuditFields(recordVO, false);
        recordVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(recordVO);

        fireEvent(recordVO, IEventType.TYPE_UPDATE_AFTER);

        return recordVO;
    }

    @Override
    public void deleteRecord(String pkRecord) throws BusinessException {
        if (pkRecord == null) {
            ExceptionUtils.wrapBusinessException("记录主键不能为空");
        }
        String sql = "update forest_trap_record set dr = 1, modifier = ?, modifiedtime = ?, ts = ? where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkRecord);
        dao.executeUpdate(sql, param);
    }

    @Override
    public TrapRecordVO queryRecordById(String pkRecord) throws BusinessException {
        String condition = "pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkRecord);
        Collection<TrapRecordVO> records = dao.retrieveByClause(TrapRecordVO.class, condition, param);
        if (records != null && records.size() > 0) {
            return records.iterator().next();
        }
        return null;
    }

    @Override
    public TrapRecordVO[] queryRecordsByStatus(int status, String pkOrg) throws BusinessException {
        String condition = "record_status = ? and pk_org = ? and dr = 0 order by record_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(status);
        param.addParam(pkOrg);
        Collection<TrapRecordVO> records = dao.retrieveByClause(TrapRecordVO.class, condition, param);
        return records != null ? records.toArray(new TrapRecordVO[0]) : new TrapRecordVO[0];
    }

    private void fillAuditFields(TrapRecordVO vo, boolean isNew) {
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
        if (vo.getPk_ranger() == null) {
            vo.setPk_ranger(userId);
        }
        if (vo.getIs_suspect_quarantine() == null) {
            vo.setIs_suspect_quarantine(TrapRecordVO.NOT_SUSPECT);
        }

        if (isNew) {
            vo.setCreator(userId);
            vo.setCreationtime(now);
        }
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }

    private void fireEvent(TrapRecordVO recordVO, String eventType) {
        try {
            BusinessEvent event = new BusinessEvent(SOURCE_ID, eventType, recordVO);
            EventDispatcher.fireEvent(event);
        } catch (Exception e) {
        }
    }
}
