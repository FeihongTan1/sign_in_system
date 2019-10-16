package team.a9043.sign_in_system.mapper;

import org.apache.ibatis.annotations.Param;
import team.a9043.sign_in_system.pojo.SisCourse;

import java.util.List;

/**
 * @author a9043
 */
public interface OtherMapper {
    List<SisCourse> selectStuCozTable(@Param("suId") String suId,
                                      @Param("isStu") boolean isStu);
}
