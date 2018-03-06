package com.coinwallet.rechage.dao;

import com.coinwallet.rechage.entity.userCoinLog;
import com.coinwallet.rechage.entity.userCoinLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface userCoinLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(userCoinLog record);

    int insertSelective(userCoinLog record);

    List<userCoinLog> selectByExample(userCoinLogExample example);

    userCoinLog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") userCoinLog record, @Param("example") userCoinLogExample example);

    int updateByExample(@Param("record") userCoinLog record, @Param("example") userCoinLogExample example);

    int updateByPrimaryKeySelective(userCoinLog record);

    int updateByPrimaryKey(userCoinLog record);
}