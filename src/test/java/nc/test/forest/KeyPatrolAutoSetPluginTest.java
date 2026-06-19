package nc.test.forest;

import nc.bs.businessevent.BusinessEvent;
import nc.rule.forest.ForestBusinessValidator;
import nc.rule.forest.KeyPatrolAutoSetPlugin;
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
import nc.bs.framework.common.InvocationInfoProxy;
import nc.jdbc.framework.SQLParameter;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyPatrolAutoSetPlugin.class, ForestBusinessValidator.class, InvocationInfoProxy.class})
public class KeyPatrolAutoSetPluginTest {

    @Mock
    private BaseDAO mockDao;

    private KeyPatrolAutoSetPlugin plugin;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        plugin = new KeyPatrolAutoSetPlugin();
        PowerMockito.whenNew(BaseDAO.class).withNoArguments().thenReturn(mockDao);
        PowerMockito.mockStatic(InvocationInfoProxy.class);
        PowerMockito.mockStatic(ForestBusinessValidator.class);
    }

    @Test
    public void testDoAction_ThreeHighRisk_SetKeyPatrol() throws Exception {
        ForestReviewVO reviewVO = new ForestReviewVO();
        reviewVO.setPk_trap_record("test_record_pk");
        reviewVO.setRisk_level(3);

        when(ForestBusinessValidator.countContinuousHighRisk(anyString())).thenReturn(3);
        when(mockDao.executeQuery(anyString(), any(SQLParameter.class), any())).thenReturn("test_trap_pk");
        when(mockDao.executeUpdate(anyString(), any(SQLParameter.class))).thenReturn(1);

        BusinessEvent event = new BusinessEvent("FOREST_REVIEW", "TYPE_INSERT_AFTER", reviewVO);

        try {
            plugin.doAction(event);
            verify(mockDao, times(1)).executeUpdate(anyString(), any(SQLParameter.class));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDoAction_TwoHighRisk_NoSetKeyPatrol() throws Exception {
        ForestReviewVO reviewVO = new ForestReviewVO();
        reviewVO.setPk_trap_record("test_record_pk");
        reviewVO.setRisk_level(3);

        when(ForestBusinessValidator.countContinuousHighRisk(anyString())).thenReturn(2);

        BusinessEvent event = new BusinessEvent("FOREST_REVIEW", "TYPE_INSERT_AFTER", reviewVO);

        try {
            plugin.doAction(event);
            verify(mockDao, never()).executeUpdate(anyString(), any(SQLParameter.class));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDoAction_LowRisk_NoSetKeyPatrol() throws Exception {
        ForestReviewVO reviewVO = new ForestReviewVO();
        reviewVO.setPk_trap_record("test_record_pk");
        reviewVO.setRisk_level(1);

        BusinessEvent event = new BusinessEvent("FOREST_REVIEW", "TYPE_INSERT_AFTER", reviewVO);

        try {
            plugin.doAction(event);
            verify(mockDao, never()).executeUpdate(anyString(), any(SQLParameter.class));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDoAction_NullEvent_NoException() throws Exception {
        BusinessEvent event = new BusinessEvent("FOREST_REVIEW", "TYPE_INSERT_AFTER", null);

        try {
            plugin.doAction(event);
            assertTrue(true);
        } catch (BusinessException e) {
            fail("不应抛出异常");
        }
    }

    @Test
    public void testDoAction_WrongSourceId_NoProcessing() throws Exception {
        ForestReviewVO reviewVO = new ForestReviewVO();
        reviewVO.setPk_trap_record("test_record_pk");
        reviewVO.setRisk_level(3);

        BusinessEvent event = new BusinessEvent("OTHER_SOURCE", "TYPE_INSERT_AFTER", reviewVO);

        try {
            plugin.doAction(event);
            verify(ForestBusinessValidator, never()).countContinuousHighRisk(anyString());
        } catch (BusinessException e) {
            fail("不应抛出异常");
        }
    }
}
