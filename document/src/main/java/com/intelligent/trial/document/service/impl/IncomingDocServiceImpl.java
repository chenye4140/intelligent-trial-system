package com.intelligent.trial.document.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.document.entity.IncomingDoc;
import com.intelligent.trial.document.mapper.IncomingDocMapper;
import com.intelligent.trial.document.service.IIncomingDocService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 来文登记服务实现类
 *
 * @author intelligent-trial
 */
@Service
public class IncomingDocServiceImpl extends ServiceImpl<IncomingDocMapper, IncomingDoc> implements IIncomingDocService {

    @Override
    public Page<IncomingDoc> pageIncomingDoc(Integer pageNum, Integer pageSize,
                                             String title, String fromUnit,
                                             Integer status, Date startDate, Date endDate) {
        Page<IncomingDoc> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectIncomingDocPage(page, title, fromUnit, status, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIncomingDoc(IncomingDoc incomingDoc) {
        if (incomingDoc.getTitle() == null || incomingDoc.getTitle().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "来文标题不能为空");
        }
        if (incomingDoc.getReceiveDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "收到日期不能为空");
        }

        if (incomingDoc.getStatus() == null) {
            incomingDoc.setStatus(0); // 默认待处理
        }

        Date now = new Date();
        incomingDoc.setCreateTime(now);
        incomingDoc.setUpdateTime(now);
        baseMapper.insert(incomingDoc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIncomingDoc(IncomingDoc incomingDoc) {
        if (incomingDoc.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "来文ID不能为空");
        }

        IncomingDoc existDoc = baseMapper.selectById(incomingDoc.getId());
        if (existDoc == null) {
            throw new BusinessException(ErrorCode.DOC_NOT_FOUND.getCode(), "来文不存在");
        }

        incomingDoc.setUpdateTime(new Date());
        baseMapper.updateById(incomingDoc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIncomingDoc(Long id) {
        IncomingDoc existDoc = baseMapper.selectById(id);
        if (existDoc == null) {
            throw new BusinessException(ErrorCode.DOC_NOT_FOUND.getCode(), "来文不存在");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        IncomingDoc existDoc = baseMapper.selectById(id);
        if (existDoc == null) {
            throw new BusinessException(ErrorCode.DOC_NOT_FOUND.getCode(), "来文不存在");
        }

        IncomingDoc incomingDoc = new IncomingDoc();
        incomingDoc.setId(id);
        incomingDoc.setStatus(status);
        incomingDoc.setUpdateTime(new Date());

        baseMapper.updateById(incomingDoc);
    }
}
