package nc.itf.forest.record;

import nc.vo.forest.record.TrapRecordVO;
import nc.vo.pub.BusinessException;

public interface ITrapRecordService {

    TrapRecordVO[] queryRecordsByTrap(String pkTrap, String pkOrg) throws BusinessException;

    TrapRecordVO[] queryPendingReviewRecords(String pkOrg) throws BusinessException;

    TrapRecordVO[] queryRecordsByRanger(String pkRanger, String pkOrg) throws BusinessException;

    TrapRecordVO saveRecord(TrapRecordVO recordVO) throws BusinessException;

    TrapRecordVO updateRecord(TrapRecordVO recordVO) throws BusinessException;

    void deleteRecord(String pkRecord) throws BusinessException;

    TrapRecordVO queryRecordById(String pkRecord) throws BusinessException;

    TrapRecordVO[] queryRecordsByStatus(int status, String pkOrg) throws BusinessException;
}
