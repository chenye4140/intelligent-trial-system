package com.intelligent.trial.casemanage.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.casemanage.management.dto.CaseInfoDTO;
import com.intelligent.trial.casemanage.management.dto.CaseSearchDTO;
import com.intelligent.trial.casemanage.management.entity.CaseInfo;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
import com.intelligent.trial.casemanage.management.mapper.CaseInfoMapper;
import com.intelligent.trial.casemanage.management.mapper.CasePartyMapper;
import com.intelligent.trial.casemanage.management.mapper.CaseViolationFactMapper;
import com.intelligent.trial.casemanage.management.vo.CaseInfoVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CaseInfoServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CaseInfoServiceImplTest {

    @InjectMocks
    private CaseInfoServiceImpl caseInfoService;

    @Mock
    private CaseInfoMapper baseMapper;

    @Mock
    private CasePartyMapper casePartyMapper;

    @Mock
    private CaseViolationFactMapper caseViolationFactMapper;

    private CaseInfoDTO createValidDTO() {
        CaseInfoDTO dto = new CaseInfoDTO();
        dto.setCaseName("测试案件");
        dto.setCaseType(1);
        dto.setCaseSource("信访举报");
        dto.setRespondentName("张三");
        dto.setRespondentDept("某单位");
        dto.setRespondentPosition("科长");
        dto.setStatus(0);
        return dto;
    }

    private CaseInfo createCaseInfo(Long id, String caseCode, String caseName) {
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setId(id);
        caseInfo.setCaseCode(caseCode);
        caseInfo.setCaseName(caseName);
        caseInfo.setCaseType(1);
        caseInfo.setStatus(0);
        caseInfo.setCreateTime(new Date());
        caseInfo.setUpdateTime(new Date());
        return caseInfo;
    }

    // ========== pageCase tests ==========

    @Test
    void pageCase_shouldReturnPageResults() {
        CaseSearchDTO search = new CaseSearchDTO();
        search.setCaseName("测试");
        search.setStatus(1);

        Page<CaseInfoVO> mockPage = new Page<>(1, 10);
        CaseInfoVO vo = new CaseInfoVO();
        vo.setId(1L);
        vo.setCaseName("测试案件");
        mockPage.setRecords(Arrays.asList(vo));
        mockPage.setTotal(1);

        when(baseMapper.selectCasePage(any(Page.class), isNull(), eq("测试"), isNull(), eq(1),
                isNull(), isNull(), isNull(), isNull())).thenReturn(mockPage);

        Page<CaseInfoVO> result = caseInfoService.pageCase(1, 10, search);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("测试案件", result.getRecords().get(0).getCaseName());
    }

    @Test
    void pageCase_shouldHandleNullSearch() {
        Page<CaseInfoVO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList());
        mockPage.setTotal(0);

        when(baseMapper.selectCasePage(any(Page.class), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull())).thenReturn(mockPage);

        Page<CaseInfoVO> result = caseInfoService.pageCase(1, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    // ========== getCaseDetail tests ==========

    @Test
    void getCaseDetail_shouldReturnCaseVO() {
        CaseInfoVO vo = new CaseInfoVO();
        vo.setId(1L);
        vo.setCaseCode("AJ202605170001");
        vo.setCaseName("测试案件");

        when(baseMapper.selectCaseDetailById(1L)).thenReturn(vo);

        CaseInfoVO result = caseInfoService.getCaseDetail(1L);

        assertNotNull(result);
        assertEquals("AJ202605170001", result.getCaseCode());
        verify(baseMapper).selectCaseDetailById(1L);
    }

    @Test
    void getCaseDetail_shouldThrowWhenCaseNotFound() {
        when(baseMapper.selectCaseDetailById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.getCaseDetail(999L));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== addCase tests ==========

    @Test
    void addCase_shouldCreateCaseWithAutoCode() {
        CaseInfoDTO dto = createValidDTO();

        // No existing case for today, so sequence = 1
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(baseMapper.insert(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.addCase(dto);

        verify(baseMapper).insert(argThat((CaseInfo c) -> {
            if (c.getCaseName() == null || !c.getCaseName().equals("测试案件")) return false;
            if (c.getCaseCode() == null || !c.getCaseCode().startsWith("AJ")) return false;
            if (c.getStatus() == null || c.getStatus() != 0) return false;
            return true;
        }));
    }

    @Test
    void addCase_shouldIncrementCaseCode() {
        CaseInfoDTO dto = createValidDTO();

        // Simulate existing case with code AJ202605170003
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String today = sdf.format(new Date());
        CaseInfo lastCase = new CaseInfo();
        lastCase.setCaseCode("AJ" + today + "0003");

        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(lastCase);
        when(baseMapper.insert(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.addCase(dto);

        verify(baseMapper).insert(argThat((CaseInfo c) -> {
            if (c.getCaseCode() == null) return false;
            return c.getCaseCode().equals("AJ" + today + "0004");
        }));
    }

    @Test
    void addCase_shouldSetDefaultStatusWhenNull() {
        CaseInfoDTO dto = createValidDTO();
        dto.setStatus(null);

        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(baseMapper.insert(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.addCase(dto);

        verify(baseMapper).insert(argThat((CaseInfo c) ->
                c.getStatus() != null && c.getStatus() == 0));
    }

    @Test
    void addCase_shouldThrowWhenCaseNameEmpty() {
        CaseInfoDTO dto = createValidDTO();
        dto.setCaseName("");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addCase(dto));
        assertTrue(ex.getMessage().contains("案件名称不能为空"));
        verify(baseMapper, never()).insert(any());
    }

    @Test
    void addCase_shouldThrowWhenCaseNameNull() {
        CaseInfoDTO dto = createValidDTO();
        dto.setCaseName(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addCase(dto));
        assertTrue(ex.getMessage().contains("案件名称不能为空"));
        verify(baseMapper, never()).insert(any());
    }

    // ========== updateCase tests ==========

    @Test
    void updateCase_shouldUpdateExistingCase() {
        CaseInfoDTO dto = createValidDTO();
        dto.setId(1L);

        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "旧名称");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.updateCase(dto);

        verify(baseMapper).updateById(argThat((CaseInfo c) ->
                Long.valueOf(1L).equals(c.getId()) && c.getUpdateTime() != null));
    }

    @Test
    void updateCase_shouldThrowWhenIdNull() {
        CaseInfoDTO dto = createValidDTO();
        dto.setId(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.updateCase(dto));
        assertTrue(ex.getMessage().contains("案件ID不能为空"));
    }

    @Test
    void updateCase_shouldThrowWhenCaseNotFound() {
        CaseInfoDTO dto = createValidDTO();
        dto.setId(999L);

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.updateCase(dto));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== deleteCase tests ==========

    @Test
    void deleteCase_shouldDeleteCaseAndRelations() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(casePartyMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(caseViolationFactMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(baseMapper.deleteById(1L)).thenReturn(1);

        caseInfoService.deleteCase(1L);

        verify(casePartyMapper).delete(argThat((LambdaQueryWrapper<CaseParty> w) -> true));
        verify(caseViolationFactMapper).delete(argThat((LambdaQueryWrapper<CaseViolationFact> w) -> true));
        verify(baseMapper).deleteById(1L);
    }

    @Test
    void deleteCase_shouldThrowWhenCaseNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.deleteCase(999L));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== changeStatus tests ==========

    @Test
    void changeStatus_shouldUpdateStatus() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        existing.setCloseDate(null);

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.changeStatus(1L, 1);

        verify(baseMapper).updateById(argThat((CaseInfo c) ->
                Long.valueOf(1L).equals(c.getId()) && Integer.valueOf(1).equals(c.getStatus())));
    }

    @Test
    void changeStatus_shouldSetCloseDateWhenCompleted() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        existing.setCloseDate(null);

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.changeStatus(1L, 2); // 2 = 已完结

        verify(baseMapper).updateById(argThat((CaseInfo c) ->
                Long.valueOf(1L).equals(c.getId()) && c.getCloseDate() != null));
    }

    @Test
    void changeStatus_shouldSetCloseDateWhenArchived() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        existing.setCloseDate(null);

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.changeStatus(1L, 3); // 3 = 已归档

        verify(baseMapper).updateById(argThat((CaseInfo c) ->
                Long.valueOf(1L).equals(c.getId()) && c.getCloseDate() != null));
    }

    @Test
    void changeStatus_shouldNotSetCloseDateWhenAlreadySet() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        Date originalCloseDate = new Date();
        existing.setCloseDate(originalCloseDate);

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(CaseInfo.class))).thenReturn(1);

        caseInfoService.changeStatus(1L, 2);

        // When closeDate is already set, the service should NOT override it
        verify(baseMapper).updateById(argThat((CaseInfo c) ->
                Long.valueOf(1L).equals(c.getId()) && c.getCloseDate() == null));
    }

    @Test
    void changeStatus_shouldThrowWhenCaseNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.changeStatus(999L, 1));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== getParties tests ==========

    @Test
    void getParties_shouldReturnPartyList() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        CaseParty party = new CaseParty();
        party.setId(1L);
        party.setPartyName("张三");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(casePartyMapper.selectByCaseId(1L)).thenReturn(Arrays.asList(party));

        List<CaseParty> result = caseInfoService.getParties(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getPartyName());
    }

    @Test
    void getParties_shouldThrowWhenCaseNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.getParties(999L));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== addParty tests ==========

    @Test
    void addParty_shouldCreateParty() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        CaseParty party = new CaseParty();
        party.setCaseId(1L);
        party.setPartyName("李四");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(casePartyMapper.insert(any(CaseParty.class))).thenReturn(1);

        caseInfoService.addParty(party);

        verify(casePartyMapper).insert(argThat((CaseParty p) ->
                Long.valueOf(1L).equals(p.getCaseId()) && "李四".equals(p.getPartyName())));
    }

    @Test
    void addParty_shouldThrowWhenCaseIdNull() {
        CaseParty party = new CaseParty();
        party.setCaseId(null);
        party.setPartyName("李四");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addParty(party));
        assertTrue(ex.getMessage().contains("案件ID不能为空"));
    }

    @Test
    void addParty_shouldThrowWhenPartyNameEmpty() {
        CaseParty party = new CaseParty();
        party.setCaseId(1L);
        party.setPartyName("");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addParty(party));
        assertTrue(ex.getMessage().contains("当事人姓名不能为空"));
    }

    @Test
    void addParty_shouldThrowWhenCaseNotFound() {
        CaseParty party = new CaseParty();
        party.setCaseId(999L);
        party.setPartyName("李四");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addParty(party));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== deleteParty tests ==========

    @Test
    void deleteParty_shouldDeleteParty() {
        CaseParty existing = new CaseParty();
        existing.setId(1L);
        existing.setPartyName("张三");

        when(casePartyMapper.selectById(1L)).thenReturn(existing);
        when(casePartyMapper.deleteById(1L)).thenReturn(1);

        caseInfoService.deleteParty(1L);

        verify(casePartyMapper).deleteById(1L);
    }

    @Test
    void deleteParty_shouldThrowWhenPartyNotFound() {
        when(casePartyMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.deleteParty(999L));
        assertTrue(ex.getMessage().contains("当事人不存在"));
    }

    // ========== getViolationFacts tests ==========

    @Test
    void getViolationFacts_shouldReturnFactList() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        CaseViolationFact fact = new CaseViolationFact();
        fact.setId(1L);
        fact.setFactTitle("违纪事实一");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(caseViolationFactMapper.selectByCaseIdOrderBySort(1L)).thenReturn(Arrays.asList(fact));

        List<CaseViolationFact> result = caseInfoService.getViolationFacts(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("违纪事实一", result.get(0).getFactTitle());
    }

    @Test
    void getViolationFacts_shouldThrowWhenCaseNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.getViolationFacts(999L));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== addViolationFact tests ==========

    @Test
    void addViolationFact_shouldCreateFact() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        CaseViolationFact fact = new CaseViolationFact();
        fact.setCaseId(1L);
        fact.setFactTitle("违纪事实一");
        fact.setSort(1);

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(caseViolationFactMapper.insert(any(CaseViolationFact.class))).thenReturn(1);

        caseInfoService.addViolationFact(fact);

        verify(caseViolationFactMapper).insert(argThat((CaseViolationFact f) ->
                Long.valueOf(1L).equals(f.getCaseId()) && "违纪事实一".equals(f.getFactTitle())));
    }

    @Test
    void addViolationFact_shouldSetDefaultSortWhenNull() {
        CaseInfo existing = createCaseInfo(1L, "AJ202605170001", "测试案件");
        CaseViolationFact fact = new CaseViolationFact();
        fact.setCaseId(1L);
        fact.setFactTitle("违纪事实一");
        fact.setSort(null);

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(caseViolationFactMapper.insert(any(CaseViolationFact.class))).thenReturn(1);

        caseInfoService.addViolationFact(fact);

        verify(caseViolationFactMapper).insert(argThat((CaseViolationFact f) ->
                Integer.valueOf(0).equals(f.getSort())));
    }

    @Test
    void addViolationFact_shouldThrowWhenCaseIdNull() {
        CaseViolationFact fact = new CaseViolationFact();
        fact.setCaseId(null);
        fact.setFactTitle("违纪事实一");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addViolationFact(fact));
        assertTrue(ex.getMessage().contains("案件ID不能为空"));
    }

    @Test
    void addViolationFact_shouldThrowWhenTitleEmpty() {
        CaseViolationFact fact = new CaseViolationFact();
        fact.setCaseId(1L);
        fact.setFactTitle("");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addViolationFact(fact));
        assertTrue(ex.getMessage().contains("事实标题不能为空"));
    }

    @Test
    void addViolationFact_shouldThrowWhenCaseNotFound() {
        CaseViolationFact fact = new CaseViolationFact();
        fact.setCaseId(999L);
        fact.setFactTitle("违纪事实一");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.addViolationFact(fact));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    // ========== updateViolationFact tests ==========

    @Test
    void updateViolationFact_shouldUpdateFact() {
        CaseViolationFact existing = new CaseViolationFact();
        existing.setId(1L);
        existing.setFactTitle("旧标题");

        CaseViolationFact fact = new CaseViolationFact();
        fact.setId(1L);
        fact.setFactTitle("新标题");

        when(caseViolationFactMapper.selectById(1L)).thenReturn(existing);
        when(caseViolationFactMapper.updateById(any(CaseViolationFact.class))).thenReturn(1);

        caseInfoService.updateViolationFact(fact);

        verify(caseViolationFactMapper).updateById(argThat((CaseViolationFact f) ->
                Long.valueOf(1L).equals(f.getId()) && f.getUpdateTime() != null));
    }

    @Test
    void updateViolationFact_shouldThrowWhenIdNull() {
        CaseViolationFact fact = new CaseViolationFact();
        fact.setId(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.updateViolationFact(fact));
        assertTrue(ex.getMessage().contains("违纪事实ID不能为空"));
    }

    @Test
    void updateViolationFact_shouldThrowWhenFactNotFound() {
        CaseViolationFact fact = new CaseViolationFact();
        fact.setId(999L);

        when(caseViolationFactMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.updateViolationFact(fact));
        assertTrue(ex.getMessage().contains("违纪事实不存在"));
    }

    // ========== deleteViolationFact tests ==========

    @Test
    void deleteViolationFact_shouldDeleteFact() {
        CaseViolationFact existing = new CaseViolationFact();
        existing.setId(1L);

        when(caseViolationFactMapper.selectById(1L)).thenReturn(existing);
        when(caseViolationFactMapper.deleteById(1L)).thenReturn(1);

        caseInfoService.deleteViolationFact(1L);

        verify(caseViolationFactMapper).deleteById(1L);
    }

    @Test
    void deleteViolationFact_shouldThrowWhenFactNotFound() {
        when(caseViolationFactMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> caseInfoService.deleteViolationFact(999L));
        assertTrue(ex.getMessage().contains("违纪事实不存在"));
    }
}
