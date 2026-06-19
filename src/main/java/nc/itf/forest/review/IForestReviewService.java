package nc.itf.forest.review;

import nc.vo.forest.review.ForestReviewVO;
import nc.vo.pub.BusinessException;

public interface IForestReviewService {

    ForestReviewVO[] queryReviewsByRecord(String pkTrapRecord, String pkOrg) throws BusinessException;

    ForestReviewVO[] queryReviewsByQuarantine(String pkQuarantine, String pkOrg) throws BusinessException;

    ForestReviewVO saveReview(ForestReviewVO reviewVO) throws BusinessException;

    ForestReviewVO updateReview(ForestReviewVO reviewVO) throws BusinessException;

    void deleteReview(String pkReview) throws BusinessException;

    ForestReviewVO queryReviewById(String pkReview) throws BusinessException;

    ForestReviewVO queryLatestReview(String pkTrapRecord) throws BusinessException;
}
