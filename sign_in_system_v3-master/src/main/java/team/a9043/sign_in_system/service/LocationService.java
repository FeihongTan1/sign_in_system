package team.a9043.sign_in_system.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.mapper.SisLocationMapper;
import team.a9043.sign_in_system.mapper.SisScheduleMapper;
import team.a9043.sign_in_system.pojo.SisLocation;
import team.a9043.sign_in_system.pojo.SisLocationExample;
import team.a9043.sign_in_system.pojo.SisSchedule;
import team.a9043.sign_in_system.service_pojo.OperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidSuccessOperationResponse;
import team.a9043.sign_in_system.util.Gps;
import team.a9043.sign_in_system.util.LocationUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author a9043
 */
@Service
@Slf4j
public class LocationService {
    @Resource
    private SisLocationMapper sisLocationMapper;
    @Resource
    private SisScheduleMapper sisScheduleMapper;

    @Transactional
    public OperationResponse<SisSchedule> modifyScheduleLocation(Integer ssId,
                                                                 Integer slId) throws IncorrectParameterException {
        SisSchedule sisSchedule = sisScheduleMapper.selectByPrimaryKey(ssId);
        if (null == sisSchedule)
            throw new IncorrectParameterException("Schedule not found: " + ssId);
        SisLocation sisLocation = sisLocationMapper.selectByPrimaryKey(slId);
        if (null == sisLocation)
            throw new IncorrectParameterException("Location not found: " + slId);

        sisSchedule.setSlId(sisLocation.getSlId());
        sisScheduleMapper.updateByPrimaryKey(sisSchedule);

        log.info("Success in update schedule location: ssId " + ssId + " " +
            "slId " + slId);
        OperationResponse<SisSchedule> operationResponse =
            new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(sisSchedule);
        operationResponse.setMessage("data => sisSchedule");
        return operationResponse;
    }

    public SisLocation getLocation(Integer slId) throws IncorrectParameterException {
        SisLocation sisLocation = sisLocationMapper.selectByPrimaryKey(slId);
        if (null == sisLocation)
            throw new IncorrectParameterException("Incorrecr location slId: " + slId);

        log.info("get location by id: " + slId);
        return sisLocation;
    }

    public PageInfo<SisLocation> getLocations(Integer page, Integer pageSize,
                                              Integer slId, String slName) throws IncorrectParameterException {
        if (null == page)
            throw new IncorrectParameterException("Incorrect page: " + null);
        if (page < 1)
            throw new IncorrectParameterException("Incorrect page: " + page +
                " (must equal or bigger than 1)");
        if (pageSize <= 0 || pageSize > 500)
            throw new IncorrectParameterException(
                "pageSize must between [1, 500]");

        SisLocationExample sisLocationExample = new SisLocationExample();
        SisLocationExample.Criteria criteria =
            sisLocationExample.createCriteria();
        if (null != slId) criteria.andSlIdEqualTo(slId);
        if (null != slName)
            criteria.andSlNameLike(CourseService.getFuzzySearch(slName));

        PageHelper.startPage(page, pageSize);
        List<SisLocation> sisLocationList =
            sisLocationMapper.selectByExample(sisLocationExample);

        return PageInfo.of(sisLocationList);
    }

    @Transactional
    public OperationResponse<SisLocation> createLocation(SisLocation sisLocation) {
        SisLocationExample sisLocationExample = new SisLocationExample();
        sisLocationExample.createCriteria().andSlNameLike("%" + sisLocation.getSlName() + "%");
        SisLocation stdSisLocation =
            sisLocationMapper.selectByExample(sisLocationExample)
                .stream()
                .findAny()
                .orElse(null);
        if (null != stdSisLocation) return new OperationResponse<>(false,
            "location exist: " + stdSisLocation.getSlId() + ", " + stdSisLocation.getSlName());

        sisLocationMapper.insert(sisLocation);
        log.info("success insert location: " + sisLocation.getSlName());

        OperationResponse<SisLocation> operationResponse =
            new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(sisLocation);
        operationResponse.setMessage("data => sisLocation");
        return operationResponse;
    }

    @Transactional
    public OperationResponse<SisLocation> modifyLocation(Integer slId,
                                                         SisLocation sisLocation) throws IncorrectParameterException {
        SisLocation stdSisLocation = sisLocationMapper.selectByPrimaryKey(slId);
        if (null == stdSisLocation)
            throw new IncorrectParameterException("Incorrect location: slid " + slId);

        Gps gps = LocationUtil.gcj2Wgs84(sisLocation.getSlLat(), sisLocation.getSlLong());
        stdSisLocation.setSlLat(gps.getWgLat());
        stdSisLocation.setSlLong(gps.getWgLon());

        sisLocationMapper.updateByPrimaryKeySelective(stdSisLocation);
        log.info("Success in modifying location: " + slId);

        OperationResponse<SisLocation> operationResponse =
            new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(sisLocation);
        operationResponse.setMessage("data => sisLocation");
        return operationResponse;
    }

    @Transactional
    public VoidOperationResponse deleteLocation(Integer slId) throws IncorrectParameterException {
        SisLocation sisLocation = sisLocationMapper.selectByPrimaryKey(slId);
        if (null == sisLocation)
            throw new IncorrectParameterException("Location not found: " + slId);

        sisLocationMapper.deleteByPrimaryKey(slId);
        log.info("Delete location success: " + slId);

        return VoidSuccessOperationResponse.SUCCESS;
    }

}
