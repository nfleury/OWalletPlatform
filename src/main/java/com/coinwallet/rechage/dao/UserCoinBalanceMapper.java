package com.coinwallet.rechage.dao;

import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.entity.UserCoinBalanceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCoinBalanceMapper {
    int deleteByPrimaryKey(Integer userid);

    int insert(UserCoinBalance record);

    int insertSelective(UserCoinBalance record);

    List<UserCoinBalance> selectByExample(UserCoinBalanceExample example);

    UserCoinBalance selectByPrimaryKey(Integer userid);

    int updateByExampleSelective(@Param("record") UserCoinBalance record, @Param("example") UserCoinBalanceExample example);

    int updateByExample(@Param("record") UserCoinBalance record, @Param("example") UserCoinBalanceExample example);

    int updateByPrimaryKeySelective(UserCoinBalance record);

    int updateByPrimaryKey(UserCoinBalance record);
}