package nc.test.forest;

import nc.rule.forest.ForestBusinessValidator;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.forest.review.ForestReviewVO;
import nc.vo.pub.BusinessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ForestBusinessValidator.class})
public class ForestBusinessValidatorTest {

    @Mock
    private BaseDAO mockDao;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(BaseDAO.class).withNoArguments().thenReturn(mockDao);
    }

    @Test
    public void testValidateAllowDisposal_NotSuspect() throws Exception {
        TrapRecordVO recordVO = new TrapRecordVO();
        recordVO.setIs_suspect_quarantine(TrapRecordVO.NOT_SUSPECT);

        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(BeanProcessor.class)))
                .thenReturn(recordVO);

        try {
            ForestBusinessValidator.validateAllowDisposal("test_record_pk");
            assertTrue(true);
        } catch (BusinessException e) {
            fail("不应抛出异常");
        }
    }

    @Test
    public void testValidateAllowDisposal_SuspectWithReview() throws Exception {
        TrapRecordVO recordVO = new TrapRecordVO();
        recordVO.setIs_suspect_quarantine(TrapRecordVO.IS_SUSPECT);

        ForestReviewVO reviewVO = new ForestReviewVO();
        reviewVO.setIs_allow_disposal(ForestReviewVO.ALLOW_DISPOSAL);

        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(BeanProcessor.class)))
                .thenReturn(recordVO)
                .thenReturn(reviewVO);

        try {
            ForestBusinessValidator.validateAllowDisposal("test_record_pk");
            assertTrue(true);
        } catch (BusinessException e) {
            fail("不应抛出异常");
        }
    }

    @Test(expected = BusinessException.class)
    public void testValidateAllowDisposal_SuspectWithoutReview() throws Exception {
        TrapRecordVO recordVO = new TrapRecordVO();
        recordVO.setIs_suspect_quarantine(TrapRecordVO.IS_SUSPECT);

        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(BeanProcessor.class)))
                .thenReturn(recordVO)
                .thenReturn(null);

        ForestBusinessValidator.validateAllowDisposal("test_record_pk");
    }

    @Test(expected = BusinessException.class)
    public void testValidateAllowDisposal_SuspectReviewNotAllow() throws Exception {
        TrapRecordVO recordVO = new TrapRecordVO();
        recordVO.setIs_suspect_quarantine(TrapRecordVO.IS_SUSPECT);

        ForestReviewVO reviewVO = new ForestReviewVO();
        reviewVO.setIs_allow_disposal(ForestReviewVO.NOT_ALLOW_DISPOSAL);

        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(BeanProcessor.class)))
                .thenReturn(recordVO)
                .thenReturn(reviewVO);

        ForestBusinessValidator.validateAllowDisposal("test_record_pk");
    }

    @Test
    public void testCountContinuousHighRisk_ThreeHigh() throws Exception {
        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(ColumnProcessor.class)))
                .thenReturn(3);

        int count = ForestBusinessValidator.countContinuousHighRisk("test_trap_pk");
        assertEquals(3, count);
    }

    @Test
    public void testCountContinuousHighRisk_TwoHigh() throws Exception {
        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(ColumnProcessor.class)))
                .thenReturn(2);

        int count = ForestBusinessValidator.countContinuousHighRisk("test_trap_pk");
        assertEquals(2, count);
    }

    @Test
    public void testCountContinuousHighRisk_ZeroHigh() throws Exception {
        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(ColumnProcessor.class)))
                .thenReturn(0);

        int count = ForestBusinessValidator.countContinuousHighRisk("test_trap_pk");
        assertEquals(0, count);
    }

    @Test(expected = BusinessException.class)
    public void testValidateTrapRecordStatus_InvalidStatus() throws Exception {
        TrapRecordVO recordVO = new TrapRecordVO();
        recordVO.setRecord_status(TrapRecordVO.STATUS_DISPOSED);

        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(BeanProcessor.class)))
                .thenReturn(recordVO);

        ForestBusinessValidator.validateTrapRecordStatus("test_record_pk", TrapRecordVO.STATUS_PENDING_REVIEW);
    }

    @Test
    public void testValidateTrapRecordStatus_ValidStatus() throws Exception {
        TrapRecordVO recordVO = new TrapRecordVO();
        recordVO.setRecord_status(TrapRecordVO.STATUS_PENDING_REVIEW);

        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any(BeanProcessor.class)))
                .thenReturn(recordVO);

        try {
            ForestBusinessValidator.validateTrapRecordStatus("test_record_pk", TrapRecordVO.STATUS_PENDING_REVIEW);
            assertTrue(true);
        } catch (BusinessException e) {
            fail("不应抛出异常");
        }
    }
}
