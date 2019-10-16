package team.a9043.sign_in_system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import team.a9043.sign_in_system.pojo.SisSignIn;
import team.a9043.sign_in_system.pojo.SisSignInExample;

public interface SisSignInMapper {
    long countByExample(SisSignInExample example);

    int deleteByExample(SisSignInExample example);

    int deleteByPrimaryKey(Integer ssiId);

    int insert(SisSignIn record);

    int insertSelective(SisSignIn record);

    List<SisSignIn> selectByExampleWithBLOBs(SisSignInExample example);

    List<SisSignIn> selectByExample(SisSignInExample example);

    SisSignIn selectByPrimaryKey(Integer ssiId);

    int updateByExampleSelective(@Param("record") SisSignIn record, @Param("example") SisSignInExample example);

    int updateByExampleWithBLOBs(@Param("record") SisSignIn record, @Param("example") SisSignInExample example);

    int updateByExample(@Param("record") SisSignIn record, @Param("example") SisSignInExample example);

    int updateByPrimaryKeySelective(SisSignIn record);

    int updateByPrimaryKeyWithBLOBs(SisSignIn record);

    int updateByPrimaryKey(SisSignIn record);
}