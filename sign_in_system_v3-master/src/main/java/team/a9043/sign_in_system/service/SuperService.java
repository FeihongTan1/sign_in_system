package team.a9043.sign_in_system.service;

import org.springframework.stereotype.Service;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.mapper.SisUserInfoMapper;
import team.a9043.sign_in_system.mapper.SisUserMapper;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.pojo.SisUserExample;
import team.a9043.sign_in_system.pojo.SisUserInfo;
import team.a9043.sign_in_system.pojo.SisUserInfoExample;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidSuccessOperationResponse;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperService {
    @Resource
    private SisUserMapper sisUserMapper;
    @Resource
    private SisUserInfoMapper sisUserInfoMapper;

    public List<SisUser> getAdministrators() {
        SisUserExample sisUserExample = new SisUserExample();
        sisUserExample.createCriteria().andSuAuthoritiesStrLike("%ADMINISTRATOR%");

        List<SisUser> sisUserList = sisUserMapper.selectByExample(sisUserExample);
        if (sisUserList.isEmpty()) return sisUserList;

        List<String> suIdList = sisUserList.stream().map(SisUser::getSuId).collect(Collectors.toList());
        SisUserInfoExample sisUserInfoExample = new SisUserInfoExample();
        sisUserExample.createCriteria().andSuIdIn(suIdList);
        List<SisUserInfo> sisUserInfoList = sisUserInfoMapper.selectByExample(sisUserInfoExample);

        sisUserList.forEach(u -> u.setSisUserInfo(sisUserInfoList.stream()
            .filter(i -> i.getSuId().equals(u.getSuId())).findAny()
            .orElse(null)));

        return sisUserList;
    }

    public VoidOperationResponse modifyGrade(String suId, Integer grade) {
        SisUser sisUser = sisUserMapper.selectByPrimaryKey(suId);
        if (null == sisUser)
            throw new IncorrectParameterException("Invalid User suId");

        SisUserInfo sisUserInfo = sisUserInfoMapper.selectByPrimaryKey(suId);
        if (null == sisUserInfo) {
            sisUserInfo = new SisUserInfo();
            sisUserInfo.setSuId(suId);
        }

        sisUserInfo.setSuiLockGrade(grade);
        sisUserInfoMapper.updateByPrimaryKey(sisUserInfo);

        return VoidSuccessOperationResponse.SUCCESS;
    }

    public Integer getLockGrade(String suId) {
        SisUserInfo sisUserInfo = sisUserInfoMapper.selectByPrimaryKey(suId);
        if (null == sisUserInfo) return null;

        return sisUserInfo.getSuiLockGrade();
    }
}
