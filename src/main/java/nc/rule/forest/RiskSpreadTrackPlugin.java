package nc.rule.forest;

import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.IBusinessListener;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.jdbc.framework.SQLParameter;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.forest.track.RiskSpreadTrackVO;
import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

public class RiskSpreadTrackPlugin implements IBusinessListener {

    private BaseDAO dao = new BaseDAO();

    @Override
    public void doAction(BusinessEvent event) throws BusinessException {
        try {
            Object data = event.getUserObject();
            if (data == null) {
                return;
            }

            TrapRecordVO recordVO = null;
            if (data instanceof TrapRecordVO) {
                recordVO = (TrapRecordVO) data;
            } else if (data instanceof String) {
                String pkRecord = (String) data;
                recordVO = queryTrapRecord(pkRecord);
            }

            if (recordVO == null || recordVO.getRisk_level() == null) {
                return;
            }

            ForestTrapVO trapVO = queryForestTrap(recordVO.getPk_forest_trap());
            if (trapVO == null) {
                return;
            }

            int riskTrend = calculateRiskTrend(recordVO);
            UFDouble spreadRadius = calculateSpreadRadius(recordVO, trapVO);

            RiskSpreadTrackVO trackVO = new RiskSpreadTrackVO();
            trackVO.setPk_forest_trap(recordVO.getPk_forest_trap());
            trackVO.setPk_trap_record(recordVO.getPk_trap_record());
            trackVO.setTrack_date(recordVO.getRecord_date());
            trackVO.setLongitude(trapVO.getLongitude());
            trackVO.setLatitude(trapVO.getLatitude());
            trackVO.setRisk_level(recordVO.getRisk_level());
            trackVO.setRisk_trend(riskTrend);
            trackVO.setInsect_type(recordVO.getInsect_type());
            trackVO.setInsect_count(recordVO.getInsect_count());
            trackVO.setForest_block(trapVO.getForest_block());
            trackVO.setTree_species(trapVO.getTree_species());
            trackVO.setSpread_radius(spreadRadius);
            trackVO.setTrack_remark(buildTrackRemark(recordVO, trapVO, riskTrend));

            fillAuditFields(trackVO);
            trackVO.setStatus(VOStatus.NEW);
            dao.insertVO(trackVO);

        } catch (Exception e) {
            ExceptionUtils.wrapBusinessException("记录风险扩散轨迹失败: " + e.getMessage());
        }
    }

    private int calculateRiskTrend(TrapRecordVO currentRecord) throws BusinessException {
        String sql = "select risk_level from forest_trap_record "
                + "where pk_forest_trap = ? and dr = 0 and risk_level is not null and pk_trap_record <> ? "
                + "order by record_date desc, creationtime desc limit 1";
        SQLParameter param = new SQLParameter();
        param.addParam(currentRecord.getPk_forest_trap());
        param.addParam(currentRecord.getPk_trap_record());
        Object result = dao.executeQuery(sql, param, new nc.jdbc.framework.processor.ColumnProcessor());

        if (result == null) {
            return RiskSpreadTrackVO.TREND_STABLE;
        }

        int previousRisk = Integer.parseInt(result.toString());
        int currentRisk = currentRecord.getRisk_level();

        if (currentRisk > previousRisk) {
            return RiskSpreadTrackVO.TREND_UP;
        } else if (currentRisk < previousRisk) {
            return RiskSpreadTrackVO.TREND_DOWN;
        } else {
            if (currentRisk >= TrapRecordVO.RISK_HIGH) {
                return RiskSpreadTrackVO.TREND_SPREAD;
            }
            return RiskSpreadTrackVO.TREND_STABLE;
        }
    }

    private UFDouble calculateSpreadRadius(TrapRecordVO recordVO, ForestTrapVO trapVO) {
        int riskLevel = recordVO.getRisk_level() != null ? recordVO.getRisk_level() : 1;
        int insectCount = recordVO.getInsect_count() != null ? recordVO.getInsect_count() : 0;

        double baseRadius = 50.0;
        double riskFactor = riskLevel * 30.0;
        double countFactor = Math.min(insectCount * 0.5, 100.0);

        double radius = baseRadius + riskFactor + countFactor;

        int cycle = trapVO.getTrap_cycle() != null ? trapVO.getTrap_cycle() : 7;
        double cycleFactor = Math.max(1.0, cycle / 7.0);
        radius = radius * cycleFactor;

        return new UFDouble(Math.round(radius * 100.0) / 100.0);
    }

    private String buildTrackRemark(TrapRecordVO recordVO, ForestTrapVO trapVO, int trend) {
        StringBuilder sb = new StringBuilder();
        sb.append("风险等级").append(getRiskLevelDesc(recordVO.getRisk_level()));
        sb.append("，虫类:").append(recordVO.getInsect_type());
        sb.append("，数量:").append(recordVO.getInsect_count());
        sb.append("，趋势:").append(getTrendDesc(trend));
        if (TrapRecordVO.IS_SUSPECT == recordVO.getIs_suspect_quarantine()) {
            sb.append("，含疑似检疫对象");
        }
        return sb.toString();
    }

    private String getRiskLevelDesc(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case TrapRecordVO.RISK_LOW:
                return "低风险";
            case TrapRecordVO.RISK_MEDIUM:
                return "中风险";
            case TrapRecordVO.RISK_HIGH:
                return "高风险";
            default:
                return "未知";
        }
    }

    private String getTrendDesc(int trend) {
        switch (trend) {
            case RiskSpreadTrackVO.TREND_STABLE:
                return "稳定";
            case RiskSpreadTrackVO.TREND_UP:
                return "上升";
            case RiskSpreadTrackVO.TREND_DOWN:
                return "下降";
            case RiskSpreadTrackVO.TREND_SPREAD:
                return "扩散";
            default:
                return "未知";
        }
    }

    private TrapRecordVO queryTrapRecord(String pkRecord) throws BusinessException {
        String sql = "select * from forest_trap_record where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkRecord);
        return (TrapRecordVO) dao.executeQuery(sql, param,
                new nc.jdbc.framework.processor.BeanProcessor(TrapRecordVO.class));
    }

    private ForestTrapVO queryForestTrap(String pkForestTrap) throws BusinessException {
        if (pkForestTrap == null) {
            return null;
        }
        String sql = "select * from forest_trap where pk_forest_trap = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkForestTrap);
        return (ForestTrapVO) dao.executeQuery(sql, param,
                new nc.jdbc.framework.processor.BeanProcessor(ForestTrapVO.class));
    }

    private void fillAuditFields(RiskSpreadTrackVO vo) {
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
        vo.setCreator(userId);
        vo.setCreationtime(now);
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }
}
