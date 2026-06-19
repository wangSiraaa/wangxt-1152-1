package nc.itf.forest.disposal;

import nc.vo.forest.disposal.DisposalPhotoVO;
import nc.vo.forest.disposal.ForestDisposalVO;
import nc.vo.pub.BusinessException;

public interface IForestDisposalService {

    ForestDisposalVO[] queryDisposalsByRecord(String pkTrapRecord, String pkOrg) throws BusinessException;

    ForestDisposalVO[] queryDisposalsByTeam(String pkDisposalTeam, String pkOrg) throws BusinessException;

    ForestDisposalVO saveDisposal(ForestDisposalVO disposalVO) throws BusinessException;

    ForestDisposalVO updateDisposal(ForestDisposalVO disposalVO) throws BusinessException;

    void deleteDisposal(String pkDisposal) throws BusinessException;

    ForestDisposalVO queryDisposalById(String pkDisposal) throws BusinessException;

    ForestDisposalVO closeDisposal(String pkDisposal) throws BusinessException;

    DisposalPhotoVO savePhoto(DisposalPhotoVO photoVO) throws BusinessException;

    DisposalPhotoVO[] queryPhotosByDisposal(String pkDisposal) throws BusinessException;

    void deletePhoto(String pkPhoto) throws BusinessException;
}
