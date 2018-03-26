package com.coinwallet.rechage.dao;

import com.coinwallet.rechage.entity.TransactionOrder;
import com.coinwallet.rechage.entity.TransactionOrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TransactionOrderMapper {
    int deleteByPrimaryKey(String txHash);

    int insert(TransactionOrder record);

    int insertSelective(TransactionOrder record);

    List<TransactionOrder> selectByExample(TransactionOrderExample example);

    TransactionOrder selectByPrimaryKey(String txHash);

    int updateByExampleSelective(@Param("record") TransactionOrder record, @Param("example") TransactionOrderExample example);

    int updateByExample(@Param("record") TransactionOrder record, @Param("example") TransactionOrderExample example);

    int updateByPrimaryKeySelective(TransactionOrder record);

    int updateByPrimaryKey(TransactionOrder record);

    int updateByPrimaryKeyAndOrderStatus(TransactionOrder record);

    List<TransactionOrder> selectUnConfirmOrder(Integer nodeUnconfirmRow);
}