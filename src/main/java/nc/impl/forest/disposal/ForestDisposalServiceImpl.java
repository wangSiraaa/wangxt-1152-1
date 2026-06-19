package nc.impl.forest.disposal;

import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.EventDispatcher;
import nc.bs.businessevent.IEventType;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.itf.forest.disposal.IForestDisposalService;
import nc.jdbc.framework.SQLParameter;
import nc.rule.forest.ForestBusinessValidator;
import nc.vo.forest.disposal.DisposalPhotoVO;
import nc.vo.forest.disposal.ForestDisposalVO;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

import java.util.Collection;

public class ForestDisposalServiceImpl implements IForestDisposalService {

    public static final String SOURCE_ID = "FOREST_DISPOSAL";

    private BaseDAO dao = new BaseDAO();

    @Override
    public ForestDisposalVO[] queryDisposalsByRecord(String pkTrapRecord, String pkOrg) throws BusinessException {
        String condition = "pk_trap_record = ? and pk_org = ? and dr = 0 order by disposal_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        param.addParam(pkOrg);
        Collection<ForestDisposalVO> disposals = dao.retrieveByClause(ForestDisposalVO.class, condition, param);
        return disposals != null ? disposals.toArray(new ForestDisposalVO[0]) : new ForestDisposalVO[0];
    }

    @Override
    public ForestDisposalVO[] queryDisposalsByTeam(String pkDisposalTeam, String pkOrg) throws BusinessException {
        String condition = "pk_disposal_team = ? and pk_org = ? and dr = 0 order by disposal_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkDisposalTeam);
        param.addParam(pkOrg);
        Collection<ForestDisposalVO> disposals = dao.retrieveByClause(ForestDisposalVO.class, condition, param);
        return disposals != null ? disposals.toArray(new ForestDisposalVO[0]) : new ForestDisposalVO[0];
    }

    @Override
    public ForestDisposalVO saveDisposal(ForestDisposalVO disposalVO) throws BusinessException {
        if (disposalVO.getPk_trap_record() == null) {
            ExceptionUtils.wrapBusinessException("诱捕记录不能为空");
        }
        if (disposalVO.getDisposal_date() == null) {
            ExceptionUtils.wrapBusinessException("处置日期不能为空");
        }

        ForestBusinessValidator.validateAllowDisposal(disposalVO.getPk_trap_record());

        ForestBusinessValidator.validateTrapRecordStatus(
                disposalVO.getPk_trap_record(),
                TrapRecordVO.STATUS_REVIEWED
        );

        fillAuditFields(disposalVO, true);
        disposalVO.setStatus(VOStatus.NEW);
        dao.insertVO(disposalVO);

        updateTrapRecordStatus(disposalVO.getPk_trap_record(), TrapRecordVO.STATUS_DISPOSED);

        fireEvent(disposalVO, IEventType.TYPE_INSERT_AFTER);

        return disposalVO;
    }

    @Override
    public ForestDisposalVO updateDisposal(ForestDisposalVO disposalVO) throws BusinessException {
        if (disposalVO.getPk_forest_disposal() == null) {
            ExceptionUtils.wrapBusinessException("处置记录主键不能为空");
        }

        fillAuditFields(disposalVO, false);
        disposalVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(disposalVO);

        fireEvent(disposalVO, IEventType.TYPE_UPDATE_AFTER);

        return disposalVO;
    }

    @Override
    public void deleteDisposal(String pkDisposal) throws BusinessException {
        if (pkDisposal == null) {
            ExceptionUtils.wrapBusinessException("处置记录主键不能为空");
        }
        String sql = "update forest_disposal set dr = 1, modifier = ?, modifiedtime = ?, ts = ? where pk_forest_disposal = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkDisposal);
        dao.executeUpdate(sql, param);
    }

    @Override
    public ForestDisposalVO queryDisposalById(String pkDisposal) throws BusinessException {
        String condition = "pk_forest_disposal = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkDisposal);
        Collection<ForestDisposalVO> disposals = dao.retrieveByClause(ForestDisposalVO.class, condition, param);
        if (disposals != null && disposals.size() > 0) {
            return disposals.iterator().next();
        }
        return null;
    }

    @Override
    public ForestDisposalVO closeDisposal(String pkDisposal) throws BusinessException {
        ForestBusinessValidator.validateCloseDisposal(pkDisposal);

        ForestDisposalVO disposalVO = queryDisposalById(pkDisposal);
        if (disposalVO == null) {
            ExceptionUtils.wrapBusinessException("处置记录不存在");
        }

        disposalVO.setDisposal_status(ForestDisposalVO.STATUS_COMPLETED);
        fillAuditFields(disposalVO, false);
        disposalVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(disposalVO);

        fireEvent(disposalVO, IEventType.TYPE_UPDATE_AFTER);

        return disposalVO;
    }

    @Override
    public DisposalPhotoVO savePhoto(DisposalPhotoVO photoVO) throws BusinessException {
        if (photoVO.getPk_forest_disposal() == null) {
            ExceptionUtils.wrapBusinessException("处置记录主键不能为空");
        }
        if (photoVO.getPhoto_url() == null) {
            ExceptionUtils.wrapBusinessException("照片地址不能为空");
        }

        fillPhotoAuditFields(photoVO);
        photoVO.setStatus(VOStatus.NEW);
        dao.insertVO(photoVO);

        updateDisposalHasPhoto(photoVO.getPk_forest_disposal());

        return photoVO;
    }

    @Override
    public DisposalPhotoVO[] queryPhotosByDisposal(String pkDisposal) throws BusinessException {
        String condition = "pk_forest_disposal = ? and dr = 0 order by upload_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkDisposal);
        Collection<DisposalPhotoVO> photos = dao.retrieveByClause(DisposalPhotoVO.class, condition, param);
        return photos != null ? photos.toArray(new DisposalPhotoVO[0]) : new DisposalPhotoVO[0];
    }

    @Override
    public void deletePhoto(String pkPhoto) throws BusinessException {
        if (pkPhoto == null) {
            ExceptionUtils.wrapBusinessException("照片主键不能为空");
        }
        String sql = "update forest_disposal_photo set dr = 1 where pk_disposal_photo = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkPhoto);
        dao.executeUpdate(sql, param);
    }

    private void updateDisposalHasPhoto(String pkDisposal) throws BusinessException {
        String sql = "update forest_disposal set has_photo = ?, modifier = ?, modifiedtime = ?, ts = ? where pk_forest_disposal = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(ForestDisposalVO.HAS_PHOTO_YES);
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkDisposal);
        dao.executeUpdate(sql, param);
    }

    private void updateTrapRecordStatus(String pkTrapRecord, int status) throws BusinessException {
        String sql = "update forest_trap_record set record_status = ?, modifier = ?, modifiedtime = ?, ts = ? where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(status);
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkTrapRecord);
        dao.executeUpdate(sql, param);
    }

    private void fillAuditFields(ForestDisposalVO vo, boolean isNew) {
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
        if (vo.getPk_disposal_team() == null) {
            vo.setPk_disposal_team(userId);
        }
        if (vo.getHas_photo() == null) {
            vo.setHas_photo(ForestDisposalVO.NO_PHOTO);
        }
        if (vo.getDisposal_status() == null) {
            vo.setDisposal_status(ForestDisposalVO.STATUS_PROCESSING);
        }

        if (isNew) {
            vo.setCreator(userId);
            vo.setCreationtime(now);
        }
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }

    private void fillPhotoAuditFields(DisposalPhotoVO vo) {
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
        if (vo.getUpload_date() == null) {
            vo.setUpload_date(now);
        }

        vo.setCreator(userId);
        vo.setCreationtime(now);
    }

    private void fireEvent(ForestDisposalVO disposalVO, String eventType) {
        try {
            BusinessEvent event = new BusinessEvent(SOURCE_ID, eventType, disposalVO);
            EventDispatcher.fireEvent(event);
        } catch (Exception e) {
        }
    }
}
