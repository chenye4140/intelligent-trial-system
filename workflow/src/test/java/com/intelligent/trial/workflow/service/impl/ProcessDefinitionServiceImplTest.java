package com.intelligent.trial.workflow.service.impl;

import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.workflow.vo.ProcessDefinitionVO;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProcessDefinitionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceImplTest {

    @InjectMocks
    private ProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private DeploymentBuilder mockDeploymentBuilder;

    @Mock
    private Deployment mockDeployment;

    @Mock
    private ProcessDefinitionQuery mockProcessDefQuery;

    @Mock
    private ProcessDefinition mockProcessDef;

    // ==================== deployProcessByKey Tests ====================

    @Test
    void deployProcessByKey_shouldThrow_whenKeyEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> processDefinitionService.deployProcessByKey(null));
        assertThrows(IllegalArgumentException.class,
                () -> processDefinitionService.deployProcessByKey(""));
    }

    @Test
    void deployProcessByKey_shouldBuildCorrectResourcePath() {
        when(repositoryService.createDeployment()).thenReturn(mockDeploymentBuilder);
        when(mockDeploymentBuilder.name(anyString())).thenReturn(mockDeploymentBuilder);
        when(mockDeploymentBuilder.addClasspathResource(anyString())).thenReturn(mockDeploymentBuilder);
        when(mockDeploymentBuilder.deploy()).thenReturn(mockDeployment);
        when(mockDeployment.getId()).thenReturn("deployment-001");

        String result = processDefinitionService.deployProcessByKey("case-review");

        assertEquals("deployment-001", result);
        verify(mockDeploymentBuilder).addClasspathResource("processes/case-review.bpmn20.xml");
    }

    // ==================== deployProcess Tests ====================

    @Test
    void deployProcess_shouldThrow_whenPathEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> processDefinitionService.deployProcess(null));
        assertThrows(IllegalArgumentException.class,
                () -> processDefinitionService.deployProcess(""));
    }

    @Test
    void deployProcess_shouldDeploySuccessfully() {
        when(repositoryService.createDeployment()).thenReturn(mockDeploymentBuilder);
        when(mockDeploymentBuilder.name("processes/case-review.bpmn20.xml")).thenReturn(mockDeploymentBuilder);
        when(mockDeploymentBuilder.addClasspathResource("processes/case-review.bpmn20.xml"))
                .thenReturn(mockDeploymentBuilder);
        when(mockDeploymentBuilder.deploy()).thenReturn(mockDeployment);
        when(mockDeployment.getId()).thenReturn("deployment-002");

        String result = processDefinitionService.deployProcess("processes/case-review.bpmn20.xml");

        assertEquals("deployment-002", result);
        verify(mockDeploymentBuilder).name("processes/case-review.bpmn20.xml");
    }

    // ==================== listProcessDefinitions Tests ====================

    @Test
    void listProcessDefinitions_shouldReturnAllDefinitions() {
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.orderByProcessDefinitionVersion()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.desc()).thenReturn(mockProcessDefQuery);

        List<ProcessDefinition> defs = new ArrayList<>();
        ProcessDefinition def1 = mock(ProcessDefinition.class);
        when(def1.getId()).thenReturn("def-1:1");
        when(def1.getKey()).thenReturn("case-review");
        when(def1.getName()).thenReturn("案件审理审批");
        when(def1.getVersion()).thenReturn(1);
        when(def1.getResourceName()).thenReturn("processes/case-review.bpmn20.xml");
        when(def1.getDescription()).thenReturn("案件审理流程");
        when(def1.getDeploymentId()).thenReturn("dep-1");
        when(def1.isSuspended()).thenReturn(false);
        when(def1.getDiagramResourceName()).thenReturn(null);
        defs.add(def1);

        ProcessDefinition def2 = mock(ProcessDefinition.class);
        when(def2.getId()).thenReturn("def-2:1");
        when(def2.getKey()).thenReturn("punishment-flow");
        when(def2.getName()).thenReturn("处分执行流程");
        when(def2.getVersion()).thenReturn(1);
        when(def2.getResourceName()).thenReturn("processes/punishment.bpmn20.xml");
        when(def2.getDescription()).thenReturn(null);
        when(def2.getDeploymentId()).thenReturn("dep-2");
        when(def2.isSuspended()).thenReturn(true);
        when(def2.getDiagramResourceName()).thenReturn(null);
        defs.add(def2);

        when(mockProcessDefQuery.list()).thenReturn(defs);

        List<ProcessDefinitionVO> result = processDefinitionService.listProcessDefinitions();

        assertEquals(2, result.size());
        assertEquals("案件审理审批", result.get(0).getName());
        assertEquals(1, result.get(0).getVersion());
        assertFalse(result.get(0).getSuspended());
        assertTrue(result.get(1).getSuspended());
    }

    @Test
    void listProcessDefinitions_shouldReturnEmpty_whenNoDefinitions() {
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.orderByProcessDefinitionVersion()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.desc()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.list()).thenReturn(Collections.emptyList());

        List<ProcessDefinitionVO> result = processDefinitionService.listProcessDefinitions();

        assertTrue(result.isEmpty());
    }

    // ==================== getLatestProcessDefinition Tests ====================

    @Test
    void getLatestProcessDefinition_shouldThrow_whenKeyEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> processDefinitionService.getLatestProcessDefinition(null));
    }

    @Test
    void getLatestProcessDefinition_shouldReturnLatestVersion() {
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.processDefinitionKey("case-review")).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.latestVersion()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.singleResult()).thenReturn(mockProcessDef);
        when(mockProcessDef.getId()).thenReturn("case-review:3");
        when(mockProcessDef.getKey()).thenReturn("case-review");
        when(mockProcessDef.getName()).thenReturn("案件审理审批");
        when(mockProcessDef.getVersion()).thenReturn(3);
        when(mockProcessDef.getResourceName()).thenReturn("processes/case-review.bpmn20.xml");
        when(mockProcessDef.getDescription()).thenReturn("案件审理审批流程");
        when(mockProcessDef.getDeploymentId()).thenReturn("dep-1");
        when(mockProcessDef.isSuspended()).thenReturn(false);
        when(mockProcessDef.getDiagramResourceName()).thenReturn(null);

        ProcessDefinitionVO result = processDefinitionService.getLatestProcessDefinition("case-review");

        assertEquals("case-review:3", result.getId());
        assertEquals(3, result.getVersion());
        assertEquals("case-review", result.getKey());
    }

    @Test
    void getLatestProcessDefinition_shouldThrow_whenNotFound() {
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.processDefinitionKey("nonexistent")).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.latestVersion()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.singleResult()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> processDefinitionService.getLatestProcessDefinition("nonexistent"));
        assertTrue(ex.getMessage().contains("未找到流程定义"));
    }

    // ==================== getProcessDefinitionById Tests ====================

    @Test
    void getProcessDefinitionById_shouldThrow_whenIdEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> processDefinitionService.getProcessDefinitionById(null));
    }

    @Test
    void getProcessDefinitionById_shouldReturnDefinition() {
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.processDefinitionId("def-001")).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.singleResult()).thenReturn(mockProcessDef);
        when(mockProcessDef.getId()).thenReturn("def-001");
        when(mockProcessDef.getKey()).thenReturn("case-review");
        when(mockProcessDef.getName()).thenReturn("审批流程");
        when(mockProcessDef.getVersion()).thenReturn(1);
        when(mockProcessDef.getResourceName()).thenReturn("processes/case-review.bpmn20.xml");
        when(mockProcessDef.getDescription()).thenReturn(null);
        when(mockProcessDef.getDeploymentId()).thenReturn("dep-1");
        when(mockProcessDef.isSuspended()).thenReturn(false);
        when(mockProcessDef.getDiagramResourceName()).thenReturn(null);

        ProcessDefinitionVO result = processDefinitionService.getProcessDefinitionById("def-001");

        assertNotNull(result);
        assertEquals("def-001", result.getId());
    }

    @Test
    void getProcessDefinitionById_shouldThrow_whenNotFound() {
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.processDefinitionId("def-999")).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.singleResult()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> processDefinitionService.getProcessDefinitionById("def-999"));
        assertTrue(ex.getMessage().contains("未找到流程定义"));
    }

    // ==================== Suspend/Activate/Delete Tests ====================

    @Test
    void suspendProcessDefinition_shouldCallRepositoryService() {
        processDefinitionService.suspendProcessDefinition("def-001");
        verify(repositoryService).suspendProcessDefinitionById("def-001");
    }

    @Test
    void activateProcessDefinition_shouldCallRepositoryService() {
        processDefinitionService.activateProcessDefinition("def-001");
        verify(repositoryService).activateProcessDefinitionById("def-001");
    }

    @Test
    void deleteProcessDefinition_shouldDeleteWithCascade() {
        processDefinitionService.deleteProcessDefinition("dep-001", true);
        verify(repositoryService).deleteDeployment("dep-001", true);
    }

    @Test
    void deleteProcessDefinition_shouldDeleteWithoutCascade() {
        processDefinitionService.deleteProcessDefinition("dep-001", false);
        verify(repositoryService).deleteDeployment("dep-001", false);
    }

    // ==================== convertToVO Tests ====================

    @Test
    void convertToVO_shouldMapAllFields() {
        when(mockProcessDef.getId()).thenReturn("def-1:2");
        when(mockProcessDef.getKey()).thenReturn("case-review");
        when(mockProcessDef.getName()).thenReturn("案件审理审批");
        when(mockProcessDef.getVersion()).thenReturn(2);
        when(mockProcessDef.getResourceName()).thenReturn("processes/case-review.bpmn20.xml");
        when(mockProcessDef.getDescription()).thenReturn("描述信息");
        when(mockProcessDef.getDeploymentId()).thenReturn("dep-1");
        when(mockProcessDef.isSuspended()).thenReturn(false);
        when(mockProcessDef.getDiagramResourceName()).thenReturn("processes/case-review.png");

        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.processDefinitionId("def-1:2")).thenReturn(mockProcessDefQuery);
        when(mockProcessDefQuery.singleResult()).thenReturn(mockProcessDef);

        ProcessDefinitionVO vo = processDefinitionService.getProcessDefinitionById("def-1:2");

        assertEquals("def-1:2", vo.getId());
        assertEquals("case-review", vo.getKey());
        assertEquals("案件审理审批", vo.getName());
        assertEquals(2, vo.getVersion());
        assertEquals("processes/case-review.bpmn20.xml", vo.getResourceName());
        assertEquals("描述信息", vo.getDescription());
        assertEquals("dep-1", vo.getDeploymentId());
        assertFalse(vo.getSuspended());
        assertEquals("processes/case-review.png", vo.getDiagramResourceName());
    }
}
