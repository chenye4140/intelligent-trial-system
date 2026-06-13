package com.intelligent.trial.casemanage.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.casemanage.management.dto.CaseInfoDTO;
import com.intelligent.trial.casemanage.management.dto.CaseSearchDTO;
import com.intelligent.trial.casemanage.management.entity.CaseInfo;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
import com.intelligent.trial.casemanage.management.mapper.CaseInfoMapper;
import com.intelligent.trial.casemanage.management.mapper.CasePartyMapper;
import com.intelligent.trial.casemanage.management.mapper.CaseViolationFactMapper;
import com.intelligent.trial.casemanage.management.service.ICaseInfoService;
import com.intelligent.trial.casemanage.management.vo.CaseInfoVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 案件管理服务实现类
 */
@Service
public class CaseInfoServiceImpl extends ServiceImpl<CaseInfoMapper, CaseInfo> implements ICaseInfoService {

    private static final Logger log = LoggerFactory.getLogger(CaseInfoServiceImpl.class);

    @Autowired
    private CasePartyMapper casePartyMapper;

    @Autowired
    private CaseViolationFactMapper caseViolationFactMapper;

    @Override
    public Page<CaseInfoVO> pageCase(Integer pageNum, Integer pageSize, CaseSearchDTO search) {
        Page<CaseInfoVO> page = new Page<>(pageNum, pageSize);
        String caseCode = search != null ? search.getCaseCode() : null;
        String caseName = search != null ? search.getCaseName() : null;
        Integer caseType = search != null ? search.getCaseType() : null;
        Integer status = search != null ? search.getStatus() : null;
        String respondentName = search != null ? search.getRespondentName() : null;
        Long handlingDeptId = search != null ? search.getHandlingDeptId() : null;
        Date startDate = search != null ? search.getStartDate() : null;
        Date endDate = search != null ? search.getEndDate() : null;
        return baseMapper.selectCasePage(page, caseCode, caseName, caseType, status,
                respondentName, handlingDeptId, startDate, endDate);
    }

    @Override
    public CaseInfoVO getCaseDetail(Long id) {
        CaseInfoVO vo = baseMapper.selectCaseDetailById(id);
        if (vo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCase(CaseInfoDTO dto) {
        if (dto.getCaseName() == null || dto.getCaseName().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "案件名称不能为空");
        }

        CaseInfo caseInfo = new CaseInfo();
        BeanUtils.copyProperties(dto, caseInfo);

        // 自动生成案件编号: AJ + yyyyMMdd + 4位序号
        String caseCode = generateCaseCode();
        caseInfo.setCaseCode(caseCode);

        if (caseInfo.getStatus() == null) {
            caseInfo.setStatus(0); // 默认草稿状态
        }

        Date now = new Date();
        caseInfo.setCreateTime(now);
        caseInfo.setUpdateTime(now);
        baseMapper.insert(caseInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCase(CaseInfoDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "案件ID不能为空");
        }

        CaseInfo existCase = baseMapper.selectById(dto.getId());
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }

        CaseInfo caseInfo = new CaseInfo();
        BeanUtils.copyProperties(dto, caseInfo);
        caseInfo.setUpdateTime(new Date());
        baseMapper.updateById(caseInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCase(Long id) {
        CaseInfo existCase = baseMapper.selectById(id);
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }

        // 删除关联的当事人
        LambdaQueryWrapper<CaseParty> partyWrapper = new LambdaQueryWrapper<>();
        partyWrapper.eq(CaseParty::getCaseId, id);
        casePartyMapper.delete(partyWrapper);

        // 删除关联的违纪事实
        LambdaQueryWrapper<CaseViolationFact> factWrapper = new LambdaQueryWrapper<>();
        factWrapper.eq(CaseViolationFact::getCaseId, id);
        caseViolationFactMapper.delete(factWrapper);

        // 删除案件
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        CaseInfo existCase = baseMapper.selectById(id);
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }

        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setId(id);
        caseInfo.setStatus(status);
        caseInfo.setUpdateTime(new Date());

        // 如果状态变为已完结或已归档，自动设置结案日期
        if (status != null && (status == 2 || status == 3) && existCase.getCloseDate() == null) {
            caseInfo.setCloseDate(new Date());
        }

        baseMapper.updateById(caseInfo);
    }

    @Override
    public List<CaseParty> getParties(Long caseId) {
        CaseInfo existCase = baseMapper.selectById(caseId);
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }
        return casePartyMapper.selectByCaseId(caseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addParty(CaseParty party) {
        if (party.getCaseId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "案件ID不能为空");
        }
        if (party.getPartyName() == null || party.getPartyName().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "当事人姓名不能为空");
        }

        CaseInfo existCase = baseMapper.selectById(party.getCaseId());
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }

        Date now = new Date();
        party.setCreateTime(now);
        party.setUpdateTime(now);
        casePartyMapper.insert(party);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteParty(Long partyId) {
        CaseParty existParty = casePartyMapper.selectById(partyId);
        if (existParty == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "当事人不存在");
        }
        casePartyMapper.deleteById(partyId);
    }

    @Override
    public List<CaseViolationFact> getViolationFacts(Long caseId) {
        CaseInfo existCase = baseMapper.selectById(caseId);
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }
        return caseViolationFactMapper.selectByCaseIdOrderBySort(caseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addViolationFact(CaseViolationFact fact) {
        if (fact.getCaseId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "案件ID不能为空");
        }
        if (fact.getFactTitle() == null || fact.getFactTitle().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "事实标题不能为空");
        }

        CaseInfo existCase = baseMapper.selectById(fact.getCaseId());
        if (existCase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在");
        }

        Date now = new Date();
        fact.setCreateTime(now);
        fact.setUpdateTime(now);
        if (fact.getSort() == null) {
            fact.setSort(0);
        }
        caseViolationFactMapper.insert(fact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateViolationFact(CaseViolationFact fact) {
        if (fact.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "违纪事实ID不能为空");
        }

        CaseViolationFact existFact = caseViolationFactMapper.selectById(fact.getId());
        if (existFact == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "违纪事实不存在");
        }

        fact.setUpdateTime(new Date());
        caseViolationFactMapper.updateById(fact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteViolationFact(Long factId) {
        CaseViolationFact existFact = caseViolationFactMapper.selectById(factId);
        if (existFact == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "违纪事实不存在");
        }
        caseViolationFactMapper.deleteById(factId);
    }

    /**
     * 生成案件编号: AJ + yyyyMMdd + 4位序号
     */
    private String generateCaseCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());

        // 查询当天最大案件编号
        LambdaQueryWrapper<CaseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(CaseInfo::getCaseCode, "AJ" + dateStr)
               .orderByDesc(CaseInfo::getCaseCode)
               .last("LIMIT 1");

        CaseInfo lastCase = baseMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastCase != null && lastCase.getCaseCode() != null) {
            String lastCode = lastCase.getCaseCode();
            if (lastCode.length() >= 12) {
                try {
                    String seqStr = lastCode.substring(10);
                    sequence = Integer.parseInt(seqStr) + 1;
                } catch (NumberFormatException e) {
                    log.debug("解析上一个案件编号序号失败，从 1 开始: lastCode={}", lastCode);
                    sequence = 1;
                }
            }
        }

        return String.format("AJ%s%04d", dateStr, sequence);
    }
}
