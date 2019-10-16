package team.a9043.sign_in_system.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.InvalidPermissionException;
import team.a9043.sign_in_system.exception.UnknownServerError;
import team.a9043.sign_in_system.mapper.*;
import team.a9043.sign_in_system.pojo.*;
import team.a9043.sign_in_system.service_pojo.OperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidSuccessOperationResponse;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author a9043
 */
@Service
public class ImportService {
    private Logger logger = LoggerFactory.getLogger(ImportService.class);
    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Resource
    private SisUserMapper sisUserMapper;
    @Resource
    private SisLocationMapper sisLocationMapper;
    @Resource
    private SisDepartmentMapper sisDepartmentMapper;
    @Resource
    private SisCourseMapper sisCourseMapper;
    @Resource
    private SisJoinCourseMapper sisJoinCourseMapper;
    @Resource
    private SisJoinDepartMapper sisJoinDepartMapper;
    @Resource
    private SisScheduleMapper sisScheduleMapper;
    @Resource(name = "sisRedisTemplate")
    private RedisTemplate<String, Object> sisRedisTemplate;

    public OperationResponse<Integer> getProgress(String key) {
        Integer progress = (Integer) sisRedisTemplate.opsForValue().get(key);
        if (null == progress)
            return new OperationResponse<>(false, "No process.");
        if (progress.equals(-1))
            return new OperationResponse<>(false, "Process error");

        OperationResponse<Integer> operationResponse =
            new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(progress);
        operationResponse.setMessage("data => progress");
        return operationResponse;
    }

    @Transactional
    @Async
    public void readCozInfo(String key, InputStream inputStream) {
        final String[] rowNameList = {"课程序号", "课程名称", "教师工号", "授课教师", "学年度学期"
            , "上课地点", "上课时间", "年级", "上课院系", "实际人数", "容量"};

        try {
            sisRedisTemplate.opsForValue().set(key, 0);
            List<List<?>> sheetList = readExcel(inputStream);
            sisRedisTemplate.opsForValue().set(key, 10);
            Map<String, Integer> cozMap = getMap(sheetList, rowNameList);
            if (null == cozMap) {
                logger.error("文件不合法");
                sisRedisTemplate.opsForValue().set(key, -1, 10,
                    TimeUnit.MINUTES);
                return;
            }

            //first
            addTeacher(sheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 30);
            addLocation(sheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 40);
            addDepartment(sheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 50);

            //second depend on first
            List<List<?>> newCozSheetList = addCourse(sheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 70);

            //third depend on second
            addJoinCourseTeaching(newCozSheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 80);
            addCourseDepart(newCozSheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 90);
            addCourseSchedule(newCozSheetList, cozMap);
            sisRedisTemplate.opsForValue().set(key, 100, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            sisRedisTemplate.opsForValue().set(key, -1, 10, TimeUnit.MINUTES);
        }
    }

    @Transactional
    @Async
    public void readStuInfo(String key, InputStream inputStream) {
        final String[] rowNameList = {"学号", "姓名", "课程序号"};

        try {
            sisRedisTemplate.opsForValue().set(key, 10);
            List<List<?>> sheetList = readExcel(inputStream);
            sisRedisTemplate.opsForValue().set(key, 30);
            Map<String, Integer> stuMap = getMap(sheetList, rowNameList);
            sisRedisTemplate.opsForValue().set(key, 50);
            if (null == stuMap) {
                logger.error("文件不合法");
                sisRedisTemplate.opsForValue().set(key, -1, 10,
                    TimeUnit.MINUTES);
                return;
            }

            //base
            addStudent(sheetList, stuMap);
            sisRedisTemplate.opsForValue().set(key, 70);
            //depend on base
            addAttendance(sheetList, stuMap);
            sisRedisTemplate.opsForValue().set(key, 100, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            sisRedisTemplate.opsForValue().set(key, -1, 10, TimeUnit.MINUTES);
        }
    }

    private Map<String, Integer> getMap(@Nonnull List<List<?>> sheetList,
                                        @Nonnull String[] rowNameList) {
        if (sheetList.size() <= 0)
            return null;
        List<?> row = sheetList.get(0);
        Map<String, Integer> rowNameMap = new HashMap<>();
        Arrays.stream(rowNameList)
            .forEach(rowName -> {
                int idx = row.indexOf(rowName);
                if (-1 == idx)
                    return;
                rowNameMap.put(rowName, idx);
            });
        if (rowNameMap.size() < rowNameList.length)
            return null;
        return rowNameMap;
    }

    @Transactional
    void addTeacher(List<List<?>> sheetList,
                    Map<String, Integer> cozMap) {
        String encryptPwd = bCryptPasswordEncoder.encode("123456");
        Set<SisUser> sisUserSet = sheetList.stream()
            .skip(1)
            .parallel()
            .map(row -> {
                String[] usrIds =
                    row.get(cozMap.get("教师工号")).toString().split(",");
                String[] usrNames =
                    row.get(cozMap.get("授课教师")).toString().split(",");

                if (usrIds.length != usrNames.length) {
                    logger.error(sheetList.indexOf(row) + " 教师信息错误");
                    return new ArrayList<SisUser>();
                }

                return IntStream.range(0, usrIds.length)
                    .mapToObj(i -> {
                        if ("".equals(usrIds[i].trim()))
                            return null;
                        List<GrantedAuthority> authList = new ArrayList<>();
                        authList.add(new SimpleGrantedAuthority("TEACHER"));
                        SisUser sisUser = new SisUser();
                        sisUser.setSuId(usrIds[i].trim());
                        sisUser.setSuName(usrNames[i].trim());
                        sisUser.setSuAuthorities(authList);
                        sisUser.setSuPassword(encryptPwd);
                        return sisUser;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        List<String> suIdList =
            sisUserSet.stream().map(SisUser::getSuId).collect(Collectors.toList());
        SisUserExample sisUserExample = new SisUserExample();
        sisUserExample.createCriteria().andSuIdIn(suIdList);
        if (suIdList.isEmpty())
            return;

        List<String> oldSuIdList =
            sisUserMapper.selectByExample(sisUserExample)
                .stream()
                .map(SisUser::getSuId)
                .collect(Collectors.toList());
        List<SisUser> insertSisUserList = sisUserSet.stream()
            .filter(sisUser -> {
                String suId = sisUser.getSuId();
                return !oldSuIdList.contains(suId);
            })
            .collect(Collectors.toList());

        if (insertSisUserList.isEmpty())
            return;
        int res = sisUserMapper.insertList(insertSisUserList);
        if (res <= 0) {
            logger.error("error insert teachers");
        } else {
            logger.info("success insert teachers: " + res);
        }
    }

    @Transactional
    void addLocation(List<List<?>> sheetList,
                     Map<String, Integer> cozMap) {
        Set<String> firstLocStrSet = sheetList.stream().skip(1).parallel()
            .map(row -> row.get(cozMap.get("上课地点")).toString().split(","))
            .flatMap(Arrays::stream)
            .map(locStr -> locStr.trim().replaceAll("[a-zA-z0-9\\-、\\s]",
                ""))
            .map(locStr -> locStr.replaceAll("[樓]", "楼"))
            .filter(locStr -> !locStr.equals("") && locStr.length() > 1)
            .collect(Collectors.toSet());
        if (firstLocStrSet.isEmpty())
            return;

        SisLocationExample sisLocationExample = new SisLocationExample();
        sisLocationExample.createCriteria().andSlNameIn(new ArrayList<>(firstLocStrSet));
        List<String> oldSlNameList =
            sisLocationMapper.selectByExample(sisLocationExample)
                .stream()
                .map(SisLocation::getSlName)
                .collect(Collectors.toList());

        List<SisLocation> sisLocationList = firstLocStrSet.stream()
            .filter(s -> !oldSlNameList.contains(s))
            .map(s -> {
                SisLocation sisLocation = new SisLocation();
                sisLocation.setSlName(s);
                return sisLocation;
            })
            .collect(Collectors.toList());

        if (sisLocationList.isEmpty())
            return;
        int res = sisLocationMapper.insertList(sisLocationList);
        if (res <= 0) {
            logger.error("error insert locations");
        } else {
            logger.info("success insert locations: " + res);
        }
    }

    @Transactional
    void addDepartment(List<List<?>> sheetList,
                       Map<String, Integer> cozMap) {
        Set<String> firstDepStrSet = sheetList.stream().skip(1)
            .map(row -> row.get(cozMap.get("上课院系")).toString().split(" "))
            .flatMap(Arrays::stream)
            .map(String::trim)
            .filter(depStr -> !depStr.startsWith("（") && !depStr.startsWith(
                "("))
            .filter(depStr -> !depStr.equals("") && depStr.length() > 1)
            .collect(Collectors.toSet());
        if (firstDepStrSet.isEmpty())
            return;

        SisDepartmentExample sisDepartmentExample = new SisDepartmentExample();
        sisDepartmentExample.createCriteria().andSdNameIn(new ArrayList<>(firstDepStrSet));
        List<String> oldSdNameList =
            sisDepartmentMapper.selectByExample(sisDepartmentExample)
                .stream()
                .map(SisDepartment::getSdName)
                .collect(Collectors.toList());
        List<SisDepartment> insertSisDepartmentList =
            firstDepStrSet.stream()
                .filter(s -> !oldSdNameList.contains(s))
                .map(s -> {
                    SisDepartment sisDepartment = new SisDepartment();
                    sisDepartment.setSdName(s);
                    return sisDepartment;
                })
                .collect(Collectors.toList());

        if (insertSisDepartmentList.isEmpty())
            return;
        int res = sisDepartmentMapper.insertList(insertSisDepartmentList);
        if (res <= 0) {
            logger.error("error insert departments");
        } else {
            logger.info("success insert departments: " + res);
        }
    }

    @Transactional
    List<List<?>> addCourse(List<List<?>> sheetList,
                            Map<String, Integer> cozMap) {
        List<SisCourse> sisCourseList = sheetList.stream().skip(1).parallel()
            .map(row -> {
                String scId = row.get(cozMap.get("课程序号")).toString().trim();
                if ("".equals(scId))
                    return null;
                String scName = row.get(cozMap.get("课程名称")).toString().trim();
                Integer scMaxSize = Double.valueOf(
                    row.get(cozMap.get("容量")).toString()).intValue();
                Integer scActSize = Double.valueOf(
                    row.get(cozMap.get("实际人数")).toString()).intValue();
                Integer scGrade = Optional
                    .of(row.get(cozMap.get("年级")).toString().trim())
                    .filter(gradeStr -> !gradeStr.equals(""))
                    .map(Integer::valueOf)
                    .orElse(null);
                SisCourse sisCourse = new SisCourse();
                sisCourse.setScId(scId);
                sisCourse.setScName(scName);
                sisCourse.setScMaxSize(scMaxSize);
                sisCourse.setScActSize(scActSize);
                sisCourse.setScGrade(scGrade);
                return sisCourse;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (sisCourseList.isEmpty())
            return new ArrayList<>();

        List<String> suIdList =
            sisCourseList.stream().map(SisCourse::getScId).collect(Collectors.toList());
        SisCourseExample sisCourseExample = new SisCourseExample();
        sisCourseExample.createCriteria().andScIdIn(suIdList);
        List<String> scIdList =
            sisCourseMapper.selectByExample(sisCourseExample).stream().map(SisCourse::getScId).collect(Collectors.toList());
        List<SisCourse> insertSisCourseList = sisCourseList.stream()
            .filter(sisCourse -> !scIdList.contains(sisCourse.getScId()))
            .collect(Collectors.toList());

        if (insertSisCourseList.isEmpty())
            return new ArrayList<>();
        int res = sisCourseMapper.insertList(insertSisCourseList);
        if (res <= 0) {
            logger.error("error insert courses");
            return null;
        } else {
            logger.info("success insert courses: " + res);
            List<String> insertScIdList =
                insertSisCourseList.stream().map(SisCourse::getScId).collect(Collectors.toList());
            return sheetList.stream().skip(1).parallel()
                .filter(row -> {
                    String scId = row.get(cozMap.get("课程序号")).toString().trim();
                    if ("".equals(scId))
                        return false;
                    return insertScIdList.contains(scId);
                })
                .collect(Collectors.toList());
        }
    }

    @Transactional
    void addJoinCourseTeaching(List<List<?>> sheetList,
                               Map<String, Integer> cozMap) {
        List<SisJoinCourse> sisJoinCourseList = sheetList.stream()
            .map(row -> {
                String scId = row.get(cozMap.get("课程序号")).toString().trim();
                if ("".equals(scId))
                    return null;
                return Arrays.stream(row.get(cozMap.get("教师工号")).toString().split(","))
                    .map(String::trim)
                    .filter(s -> !s.equals("") && s.length() > 1)
                    .map(s -> {
                        SisJoinCourse sisJoinCourse = new SisJoinCourse();
                        sisJoinCourse.setJoinCourseType(SisJoinCourse.JoinCourseType.TEACHING.ordinal());
                        sisJoinCourse.setSuId(s);
                        sisJoinCourse.setScId(scId);
                        return sisJoinCourse;
                    })
                    .collect(Collectors.toList());
            })
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        if (sisJoinCourseList.isEmpty())
            return;
        int res = sisJoinCourseMapper.insertList(sisJoinCourseList);
        if (res <= 0) {
            logger.error("error insert sisJoinCourses");
        } else {
            logger.info("success insert sisJoinCourses: " + res);
        }
    }

    @Transactional
    void addCourseDepart(List<List<?>> sheetList,
                         Map<String, Integer> cozMap) {
        List<String> sdNameList = sheetList.stream()
            .map(row -> Arrays.stream(row.get(cozMap.get(
                "上课院系")).toString().split(
                " "))
                .map(String::trim)
                .filter(depStr -> !depStr.startsWith("（") && !depStr.startsWith("("))
                .filter(gradeStr -> !gradeStr.equals("") && gradeStr.length() > 1))
            .flatMap(Stream::distinct)
            .collect(Collectors.toList());

        if (sdNameList.isEmpty())
            return;
        SisDepartmentExample sisDepartmentExample = new SisDepartmentExample();
        sisDepartmentExample.createCriteria().andSdNameIn(sdNameList);
        List<SisDepartment> sisDepartmentList =
            sisDepartmentMapper.selectByExample(sisDepartmentExample);

        List<SisJoinDepart> sisJoinDepartList = sheetList.stream()
            .map(row -> {
                String scId = row.get(cozMap.get("课程序号")).toString().trim();
                if ("".equals(scId))
                    return null;
                return Arrays
                    .stream(
                        row.get(cozMap.get("上课院系")).toString().split(" "))
                    .map(String::trim)
                    .filter(depStr -> !depStr.startsWith("（") && !depStr.startsWith("("))
                    .filter(gradeStr -> !gradeStr.equals("") && gradeStr.length() > 1)
                    .map(s -> {
                        Integer sdId =
                            sisDepartmentList.stream()
                                .filter(sisDepartment -> sisDepartment.getSdName().equals(s))
                                .findAny()
                                .map(SisDepartment::getSdId)
                                .orElse(null);
                        if (null == sdId)
                            return null;
                        SisJoinDepart sisJoinDepart = new SisJoinDepart();
                        sisJoinDepart.setScId(scId);
                        sisJoinDepart.setSdId(sdId);
                        return sisJoinDepart;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            })
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        if (sisJoinDepartList.isEmpty())
            return;
        int res = sisJoinDepartMapper.insertList(sisJoinDepartList);
        if (res <= 0) {
            logger.error("error insert sisJoinDeparts");
        } else {
            logger.info("success insert sisJoinDeparts: " + res);
        }
    }

    @Transactional
    void addCourseSchedule(List<List<?>> sheetList,
                           Map<String, Integer> cozMap) {
        final HashMap<String, Integer> dayMap = new HashMap<String, Integer>() {
            private static final long serialVersionUID = 331651763287947723L;

            {
                put("星期一", 1);
                put("星期二", 2);
                put("星期三", 3);
                put("星期四", 4);
                put("星期五", 5);
                put("星期六", 6);
                put("星期日", 7);
            }
        };
        final Pattern pattern = Pattern
            .compile("\\s*\\[(1[0-2]|[1-9])-(1[0-9]|[1-9])([单双])" +
                "?]\\s*星期[一二三四五六日]\\s*(1[0-2]|[1-9])-" +
                "(1[0-2]|[1-9])\\s*"); //上课时间匹配
        final Pattern patternSon1 = Pattern
            .compile("\\s*星期[一二三四五六日]\\s*"); //星期
        final Pattern patternSon2 = Pattern
            .compile("\\s*\\[(1[0-9]|[1-9])-(1[0-9]|[1-9])([单双])?]\\s*"); //周数
        final Pattern patternSon3 = Pattern
            .compile("\\s*(?<!\\[)(1[0-2]|[1-9])-(1[0-2]|[1-9])(?!])\\s*"); //节数

        //get location
        Set<String> slNameList = sheetList.stream()
            .map(row -> row.get(cozMap.get("上课地点")).toString().split(","))
            .flatMap(Arrays::stream)
            .map(locStr -> locStr.trim().replaceAll("[a-zA-Z0-9\\-、\\s]",
                ""))
            .map(locStr -> locStr.replaceAll("[樓]", "楼"))
            .filter(locStr -> !locStr.equals("") && locStr.length() > 1)
            .collect(Collectors.toSet());
        if (slNameList.isEmpty())
            return;

        SisLocationExample sisLocationExample = new SisLocationExample();
        sisLocationExample.createCriteria().andSlNameIn(new ArrayList<>(slNameList));
        List<SisLocation> locationList =
            sisLocationMapper.selectByExample(sisLocationExample);

        //set schedule
        List<SisSchedule> sisScheduleList = sheetList.stream()
            .map(row -> {
                String scId = row.get(cozMap.get("课程序号")).toString().trim();
                if ("".equals(scId)) {
                    logger.error("scId");
                    return null;
                }

                String[] matchStrList =
                    row.get(cozMap.get("上课时间")).toString().split("\\s*,\\s*");
                String[] locMatchStrList =
                    row.get(cozMap.get("上课地点")).toString().split("\\s*,\\s*");
                String yearAndTerm =
                    row.get(cozMap.get("学年度学期")).toString().trim();
                if (matchStrList.length != locMatchStrList.length) {
                    logger.error("row err: 上课时间,上课地点 in " + scId);
                    return null;
                }

                return IntStream.range(0, matchStrList.length)
                    .parallel()
                    .mapToObj(i -> {
                        //获得地点
                        String ssRoom = locMatchStrList[i].trim();
                        String slName = locMatchStrList[i]
                            .replaceAll("[a-zA-Z0-9\\-、\\s]", "")
                            .replaceAll("[樓]", "楼")
                            .trim();
                        if ("".equals(slName)) {
                            return null;
                        }
                        Integer slId = locationList.stream()
                            .filter(sisLocation -> sisLocation.getSlName().equals(slName))
                            .findAny()
                            .map(SisLocation::getSlId)
                            .orElse(null);

                        //开始匹配
                        Matcher matcher;
                        matcher = pattern.matcher(matchStrList[i]);
                        if (!matcher.find())
                            return null;

                        String eStr = matcher.group();
                        //获得星期
                        int day;
                        matcher = patternSon1.matcher(eStr);
                        if (!matcher.find())
                            return null;

                        day = dayMap.get(matcher.group().trim());

                        //获得周数
                        SisSchedule.SsFortnight fortnight;
                        int startWeek;
                        int endWeek;
                        matcher = patternSon2.matcher(eStr);
                        if (!matcher.find())
                            return null;

                        String[] weekStrings =
                            matcher.group().trim().split("-");//获得周数头尾
                        String weekStartStr =
                            weekStrings[0].substring(1);//获得开始周数
                        String weekEndStr =
                            weekStrings[weekStrings.length - 1];//获得结束周数
                        if (weekEndStr.contains("单")) {
                            weekEndStr = weekEndStr.substring(0,
                                weekEndStr.length() - 2);//取出 "]" 和
                            // "周" 字符;
                            fortnight = SisSchedule.SsFortnight.ODD;
                        } else if (weekEndStr.contains("双")) {
                            weekEndStr = weekEndStr.substring(0,
                                weekEndStr.length() - 2);//取出 "]" 和
                            // "周" 字符;
                            fortnight = SisSchedule.SsFortnight.EVEN;
                        } else {
                            weekEndStr = weekEndStr.substring(0,
                                weekEndStr.length() - 1);//取出"]"字符;
                            fortnight = SisSchedule.SsFortnight.FULL;
                        }
                        startWeek = Integer.valueOf(weekStartStr);
                        endWeek = Integer.valueOf(weekEndStr);

                        //获得节数
                        matcher = patternSon3.matcher(eStr);
                        if (!matcher.find())
                            return null;

                        String[] timeStrings =
                            matcher.group().trim().split("-");//获得节数头尾
                        int startInt = Integer.valueOf(timeStrings[0]);
                        int endInt = Integer.valueOf(timeStrings[1]);
                        //插入排课
                        SisSchedule schedule = new SisSchedule();
                        schedule.setSsStartTime(startInt);
                        schedule.setSsStartWeek(startWeek);
                        schedule.setSsDayOfWeek(day);
                        schedule.setSsFortnight(fortnight.ordinal());
                        schedule.setSsEndTime(endInt);
                        schedule.setSsEndWeek(endWeek);
                        schedule.setSsYearEtTerm(yearAndTerm);
                        schedule.setScId(scId);
                        schedule.setSsRoom(ssRoom);
                        schedule.setSlId(slId);
                        return schedule;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            })
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        if (sisScheduleList.isEmpty())
            return;
        int res = sisScheduleMapper.insertList(sisScheduleList);
        if (res <= 0) {
            logger.error("error insert sisSchedules");
        } else {
            logger.info("success insert sisSchedules: " + res);
        }
    }

    @Transactional
    void addStudent(List<List<?>> sheetList,
                    Map<String, Integer> stuMap) {
        String encryptPwd = bCryptPasswordEncoder.encode("123456");
        // new student
        Set<SisUser> firstSisUserList = sheetList.stream().skip(1).parallel()
            .map(row -> {
                String suId = row.get(stuMap.get("学号")).toString().trim();
                if ("".equals(suId))
                    return null;
                List<GrantedAuthority> authList = new ArrayList<>();
                authList.add(new SimpleGrantedAuthority("STUDENT"));
                SisUser sisUser = new SisUser();
                sisUser.setSuId(suId);
                sisUser.setSuName(row.get(stuMap.get("姓名")).toString().trim());
                sisUser.setSuAuthorities(authList);
                sisUser.setSuPassword(encryptPwd);
                return sisUser;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        List<String> suIdList =
            firstSisUserList.stream().map(SisUser::getSuId).collect(Collectors.toList());
        if (suIdList.isEmpty())
            return;

        SisUserExample sisUserExample = new SisUserExample();
        sisUserExample.createCriteria().andSuIdIn(suIdList);
        List<String> oldSuIdList =
            sisUserMapper.selectByExample(sisUserExample).stream().map(SisUser::getSuId).collect(Collectors.toList());

        List<SisUser> sisUserList = firstSisUserList.stream()
            .filter(sisUser -> !oldSuIdList.contains(sisUser.getSuId()))
            .collect(Collectors.toList());

        if (sisUserList.isEmpty())
            return;
        int res = sisUserMapper.insertList(sisUserList);
        if (res <= 0) {
            logger.error("error insert students");
        } else {
            logger.info("success insert students: " + res);
        }
    }

    @Transactional
    void addAttendance(List<List<?>> sheetList,
                       Map<String, Integer> stuMap) {
        SisJoinCourseExample sisJoinCourseExample = new SisJoinCourseExample();

        List<SisJoinCourse> sisJoinCourseList =
            sheetList.stream().skip(1)
                .parallel()
                .map(row -> {
                    String scId = row.get(stuMap.get("课程序号")).toString().trim();
                    if ("".equals(scId))
                        return null;
                    String suId = row.get(stuMap.get("学号")).toString().trim();
                    if ("".equals(suId))
                        return null;
                    SisJoinCourse sisJoinCourse = new SisJoinCourse();
                    sisJoinCourse.setScId(scId);
                    sisJoinCourse.setSuId(suId);
                    sisJoinCourse.setJoinCourseType(SisJoinCourse.JoinCourseType.ATTENDANCE.ordinal());
                    sisJoinCourseExample.or()
                        .andScIdEqualTo(scId)
                        .andSuIdEqualTo(suId);
                    return sisJoinCourse;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (sisJoinCourseList.isEmpty())
            return;

        //get course
        List<String> scIdList = sisJoinCourseList.stream()
            .map(SisJoinCourse::getScId)
            .distinct()
            .collect(Collectors.toList());

        SisCourseExample sisCourseExample = new SisCourseExample();
        sisCourseExample.createCriteria().andScIdIn(scIdList);
        List<String> extScIdList =
            sisCourseMapper.selectByExample(sisCourseExample).stream()
                .map(SisCourse::getScId)
                .collect(Collectors.toList());

        //get old join course
        sisJoinCourseExample.getOredCriteria().removeIf(Objects::isNull);
        List<SisJoinCourse> oldSisJoinCourseList =
            sisJoinCourseMapper.selectByExample(sisJoinCourseExample);

        List<SisJoinCourse> insertSisJoinCourse =
            sisJoinCourseList.stream()
                .filter(sisJoinCourse ->
                    !oldSisJoinCourseList.contains(sisJoinCourse) &&
                        extScIdList.contains(sisJoinCourse.getScId()))
                .collect(Collectors.toList());

        if (insertSisJoinCourse.isEmpty())
            return;
        int res = sisJoinCourseMapper.insertList(insertSisJoinCourse);
        if (res <= 0) {
            logger.error("error insert student sisJoinCourses");
        } else {
            logger.info("success insert student sisJoinCourses: " + res);
        }
    }

    List<List<?>> readExcel(InputStream inputStream) throws IOException,
        InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        return StreamSupport.stream(sheet.spliterator(), true)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(Row::getRowNum))
            .map(row -> StreamSupport.stream(row.spliterator(), false)
                .map(cell -> {
                    if (null == cell || CellType.BLANK == cell.getCellTypeEnum())
                        return "";
                    switch (cell.getCellTypeEnum()) {
                        case STRING:
                            return cell.getStringCellValue();
                        case NUMERIC:
                            return cell.getNumericCellValue();
                        case BOOLEAN:
                            return cell.getBooleanCellValue();
                        case BLANK:
                            return "";
                        default:
                            return cell.toString();
                    }
                })
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
    }

    /*------------------------------------------------*/

    @Transactional
    public VoidOperationResponse deleteCourse(String scId) throws IncorrectParameterException {
        SisCourse sisCourse = sisCourseMapper.selectByPrimaryKey(scId);
        if (null == sisCourse)
            throw new IncorrectParameterException("Course not found: " + scId);

        sisCourseMapper.deleteByPrimaryKey(scId);
        logger.info("Delete course success: " + scId);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    @Transactional
    public VoidOperationResponse deleteJoinCourse(Integer sjcId) throws IncorrectParameterException {
        SisJoinCourse sisJoinCourse =
            sisJoinCourseMapper.selectByPrimaryKey(sjcId);
        if (null == sisJoinCourse)
            throw new IncorrectParameterException("JoinCourse not found: " + sjcId);

        sisJoinCourseMapper.deleteByPrimaryKey(sjcId);
        logger.info("Delete joinCourse success: " + sjcId);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public VoidOperationResponse modifyStudent(String suId, String suName,
                                               List<String> scIdList) throws IncorrectParameterException {
        SisUser sisUser = sisUserMapper.selectByPrimaryKey(suId);
        if (null == sisUser)
            throw new IncorrectParameterException("User not found: " + suId);

        sisUser.setSuName(suName);
        sisUserMapper.updateByPrimaryKey(sisUser);

        SisJoinCourseExample sisJoinCourseExample = new SisJoinCourseExample();
        sisJoinCourseExample.createCriteria()
            .andSuIdEqualTo(suId)
            .andJoinCourseTypeEqualTo(SisJoinCourse.JoinCourseType.ATTENDANCE.ordinal());
        sisJoinCourseMapper.deleteByExample(sisJoinCourseExample);

        if (!scIdList.isEmpty()) {
            SisCourseExample sisCourseExample = new SisCourseExample();
            sisCourseExample.createCriteria().andScIdIn(scIdList);
            List<SisCourse> sisCourseList =
                sisCourseMapper.selectByExample(sisCourseExample);
            if (sisCourseList.size() != scIdList.size())
                throw new IncorrectParameterException(
                    "ScIdList error: found course " + sisCourseList.size());

            List<SisJoinCourse> sisJoinCourseList =
                sisCourseList.stream()
                    .map(sisCourse -> {
                        SisJoinCourse sisJoinCourse = new SisJoinCourse();
                        sisJoinCourse.setJoinCourseType(SisJoinCourse.JoinCourseType.ATTENDANCE.ordinal());
                        sisJoinCourse.setScId(sisCourse.getScId());
                        sisJoinCourse.setSuId(suId);
                        return sisJoinCourse;
                    })
                    .collect(Collectors.toList());

            sisJoinCourseMapper.insertList(sisJoinCourseList);
        }

        return VoidSuccessOperationResponse.SUCCESS;
    }

    @SuppressWarnings("Duplicates")
    public VoidOperationResponse createStudent(String suId, String suName,
                                               List<String> scIdList,
                                               Boolean force) throws IncorrectParameterException, InvalidPermissionException {
        SisUser sisUser = sisUserMapper.selectByPrimaryKey(suId);
        if (null != sisUser) {
            if (force)
                return modifyStudent(suId, suName, scIdList);
            else
                throw new InvalidPermissionException(
                    "User exist (add force param to modify):" + suId);
        }

        SisUser newUser = new SisUser();
        newUser.setSuId(suId);
        newUser.setSuName(suName);
        newUser.setSuAuthoritiesStr("STUDENT");
        newUser.setSuPassword(bCryptPasswordEncoder.encode("123456"));
        sisUserMapper.insert(newUser);

        if (!scIdList.isEmpty()) {
            SisCourseExample sisCourseExample = new SisCourseExample();
            sisCourseExample.createCriteria().andScIdIn(scIdList);
            List<SisCourse> sisCourseList =
                sisCourseMapper.selectByExample(sisCourseExample);
            if (sisCourseList.size() != scIdList.size()) {
                throw new IncorrectParameterException(
                    "ScIdList error: found course " + sisCourseList.size());
            }

            List<SisJoinCourse> sisJoinCourseList =
                sisCourseList.stream()
                    .map(sisCourse -> {
                        SisJoinCourse sisJoinCourse = new SisJoinCourse();
                        sisJoinCourse.setJoinCourseType(SisJoinCourse.JoinCourseType.ATTENDANCE.ordinal());
                        sisJoinCourse.setScId(sisCourse.getScId());
                        sisJoinCourse.setSuId(suId);
                        return sisJoinCourse;
                    })
                    .collect(Collectors.toList());

            sisJoinCourseMapper.insertList(sisJoinCourseList);
        }

        return VoidSuccessOperationResponse.SUCCESS;
    }

    public VoidOperationResponse createCourse(String scId, SisCourse sisCourse,
                                              List<SisSchedule> sisScheduleList,
                                              List<SisDepartment> sisDepartmentList) throws IncorrectParameterException, UnknownServerError {
        SisCourse stdSisCourse = sisCourseMapper.selectByPrimaryKey(scId);
        if (null != stdSisCourse)
            throw new IncorrectParameterException("SisCourse exists: " + scId);
        if (null == sisCourse.getScName() || sisCourse.getScName().trim().isEmpty())
            throw new IncorrectParameterException(
                "Course Name can not be blank: " + sisCourse.getScName());
        if (null != sisCourse.getScActSize() && sisCourse.getScActSize() < 0)
            throw new IncorrectParameterException(
                "Invalid scActSize: " + sisCourse.getScActSize());
        if (null != sisCourse.getScMaxSize() && sisCourse.getScMaxSize() < 0)
            throw new IncorrectParameterException(
                "Invalid scMaxSize: " + sisCourse.getScActSize());
        if (null != sisCourse.getScGrade() && (sisCourse.getScGrade() < 2010 || sisCourse.getScGrade() > 2050))
            throw new IncorrectParameterException(
                "Invalid scActSize (must between [2010, 2050]): " + sisCourse.getScGrade());
        if (null == sisCourse.getScNeedMonitor())
            sisCourse.setScNeedMonitor(false);

        sisCourse.setSuId(null);
        sisCourse.setScId(scId);
        sisCourse.setScAttRate(null);
        sisCourseMapper.insert(sisCourse);

        SisJoinDepartExample sisJoinDepartExample =
            new SisJoinDepartExample();
        sisJoinDepartExample.createCriteria().andScIdEqualTo(scId);
        sisJoinDepartMapper.deleteByExample(sisJoinDepartExample);

        insertJoinDepart(scId, sisDepartmentList);

        if (insertScheduleList(sisScheduleList, scId))
            throw new UnknownServerError("insert schedule list error.");

        return VoidSuccessOperationResponse.SUCCESS;
    }


    public VoidOperationResponse modifyCourse(String scId, SisCourse sisCourse,
                                              List<SisSchedule> mSisScheduleList,
                                              List<SisSchedule> nSisScheduleList,
                                              List<SisDepartment> sisDepartmentList) throws IncorrectParameterException, UnknownServerError {
        SisCourse stdSisCourse = sisCourseMapper.selectByPrimaryKey(scId);
        if (null == stdSisCourse)
            throw new IncorrectParameterException("SisCourse not found: " + scId);
        if (null == sisCourse.getScName() || sisCourse.getScName().trim().isEmpty())
            throw new IncorrectParameterException(
                "Course Name can not be blank: " + sisCourse.getScName());
        if (null != sisCourse.getScActSize() && sisCourse.getScActSize() < 0)
            throw new IncorrectParameterException(
                "Invalid scActSize: " + sisCourse.getScActSize());
        if (null != sisCourse.getScMaxSize() && sisCourse.getScMaxSize() < 0)
            throw new IncorrectParameterException(
                "Invalid scMaxSize: " + sisCourse.getScActSize());
        if (null != sisCourse.getScGrade() && (sisCourse.getScGrade() < 2010 || sisCourse.getScGrade() > 2050))
            throw new IncorrectParameterException(
                "Invalid scActSize (must between [2010, 2050]): " + sisCourse.getScGrade());
        if (null == sisCourse.getScNeedMonitor())
            sisCourse.setScNeedMonitor(false);

        if (sisCourse.getScNeedMonitor().equals(false))
            sisCourse.setSuId(null);
        else
            sisCourse.setSuId(stdSisCourse.getSuId());

        sisCourse.setScGrade(stdSisCourse.getScGrade());
        sisCourse.setScId(stdSisCourse.getScId());
        sisCourse.setScAttRate(stdSisCourse.getScAttRate());
        sisCourseMapper.updateByPrimaryKey(sisCourse);

        SisJoinDepartExample sisJoinDepartExample =
            new SisJoinDepartExample();
        sisJoinDepartExample.createCriteria().andScIdEqualTo(scId);
        sisJoinDepartMapper.deleteByExample(sisJoinDepartExample);

        insertJoinDepart(scId, sisDepartmentList);

        if (mSisScheduleList.stream().anyMatch(s -> null == s.getSsId()))
            throw new IncorrectParameterException(
                "There are schedules with illegal ssId in the list");
        List<SisSchedule> stdScheduleList;
        if (!mSisScheduleList.isEmpty()) {
            SisScheduleExample sisScheduleExample = new SisScheduleExample();
            sisScheduleExample.createCriteria()
                .andSsIdIn(mSisScheduleList.stream().map(SisSchedule::getSsId).collect(Collectors.toList()));
            stdScheduleList =
                sisScheduleMapper.selectByExample(sisScheduleExample);
            if (stdScheduleList.size() != mSisScheduleList.size())
                throw new IncorrectParameterException(
                    "There are illegal schedule's ssId in mScheduleList");
        } else {
            stdScheduleList = new ArrayList<>();
        }

        if (mSisScheduleList.isEmpty()) {
            SisScheduleExample sisScheduleExample = new SisScheduleExample();
            sisScheduleExample.createCriteria()
                .andScIdEqualTo(scId);

            sisScheduleMapper.deleteByExample(sisScheduleExample);
        } else {
            List<Integer> ssIdList =
                mSisScheduleList.stream().map(SisSchedule::getSsId).collect(Collectors.toList());
            SisScheduleExample sisScheduleExample = new SisScheduleExample();
            SisScheduleExample.Criteria criteria =
                sisScheduleExample.createCriteria();
            criteria.andScIdEqualTo(scId);
            if (!ssIdList.isEmpty())
                criteria.andSsIdNotIn(ssIdList);
            sisScheduleMapper.deleteByExample(sisScheduleExample);
        }

        if (!modifyScheduleList(mSisScheduleList, stdScheduleList, scId))
            throw new UnknownServerError("update schedule list error.");

        if (insertScheduleList(nSisScheduleList, scId))
            throw new UnknownServerError("insert schedule list error.");

        return VoidSuccessOperationResponse.SUCCESS;
    }

    private void insertJoinDepart(String scId,
                                  List<SisDepartment> sisDepartmentList) throws IncorrectParameterException, UnknownServerError {
        if (!sisDepartmentList.isEmpty()) {
            List<Integer> sdIdList =
                sisDepartmentList.stream().map(SisDepartment::getSdId).filter(Objects::nonNull).collect(Collectors.toList());
            if (sdIdList.isEmpty())
                throw new IncorrectParameterException("There are illegal " +
                    "department in departmentList");
            SisDepartmentExample sisDepartmentExample =
                new SisDepartmentExample();
            sisDepartmentExample.createCriteria().andSdIdIn(sdIdList);
            List<SisDepartment> stdSisDepartList =
                sisDepartmentMapper.selectByExample(sisDepartmentExample);
            if (stdSisDepartList.size() != sisDepartmentList.size())
                throw new IncorrectParameterException("There are illegal " +
                    "department in departmentList");


            List<SisJoinDepart> sisJoinDepartList =
                stdSisDepartList.stream().map(d -> {
                    SisJoinDepart sisJoinDepart = new SisJoinDepart();
                    sisJoinDepart.setSdId(d.getSdId());
                    sisJoinDepart.setScId(scId);
                    return sisJoinDepart;
                }).collect(Collectors.toList());

            boolean resJoinDepart =
                sisJoinDepartMapper.insertList(sisJoinDepartList) > 0;
            if (!resJoinDepart)
                throw new UnknownServerError("insert joinDepart error");
        }
    }

    private boolean modifyScheduleList(List<SisSchedule> mSisScheduleList,
                                       List<SisSchedule> stdScheduleList,
                                       String scId) throws IncorrectParameterException {
        if (mSisScheduleList.isEmpty())
            return true;
        if (mSisScheduleList.stream().anyMatch(m -> null == m.getSsId()))
            throw new IncorrectParameterException(
                "The ssId in the schedule to be modified can not be null.");


        List<Integer> slIdList =
            mSisScheduleList.stream().map(SisSchedule::getSlId).filter(Objects::nonNull).collect(Collectors.toList());
        List<SisLocation> sisLocationList = getSisLocations(slIdList);

        for (SisSchedule m : mSisScheduleList) {
            SisSchedule stdSchedule =
                stdScheduleList.stream()
                    .filter(s -> s.getSsId().equals(m.getSsId()))
                    .findAny()
                    .orElseThrow(() -> new IncorrectParameterException(
                        "schedule not found: " + m.getSsId()));
            if (null == m.getSsStartWeek())
                throw new IncorrectParameterException(
                    "ssStartWeek can not be null: " + m.getSsId());
            if (null == m.getSsEndWeek())
                throw new IncorrectParameterException(
                    "ssEndWeek can not be null: " + m.getSsId());
            if (m.getSsStartWeek() < 1 || m.getSsStartWeek() > 25)
                throw new IncorrectParameterException(
                    "ssStartWeek can only between [1, 25]: " + m.getSsId());
            if (m.getSsEndWeek() < 1 || m.getSsEndWeek() > 25)
                throw new IncorrectParameterException(
                    "ssEndWeek can only between [1, 25]: " + m.getSsId());
            if (m.getSsStartWeek() > m.getSsEndWeek())
                throw new IncorrectParameterException(
                    "ssEndWeek is small than ssStartWeek (fxxk you): " + m.getSsId());
            if (null == m.getSsFortnight())
                throw new IncorrectParameterException(
                    "ssFortNight can not be null: " + m.getSsId());
            try {
                SisSchedule.SsFortnight.valueOf(m.getSsFortnight());
            } catch (IndexOutOfBoundsException e) {
                throw new IncorrectParameterException(
                    "Illegal ssFortNight: " + m.getSsId() + " , message: "
                        + e.getMessage());
            }
            if (null == m.getSsStartTime())
                throw new IncorrectParameterException(
                    "ssStartTime can not be null: " + m.getSsId());
            if (null == m.getSsEndTime())
                throw new IncorrectParameterException(
                    "ssEndTime can not be null: " + m.getSsId());
            if (m.getSsStartTime() < 1 || m.getSsStartTime() > 12)
                throw new IncorrectParameterException(
                    "ssStartTime can only between [1, 12]: " + m.getSsId());
            if (m.getSsEndTime() < 1 || m.getSsEndTime() > 25)
                throw new IncorrectParameterException(
                    "ssEndTime can only between [1, 12]: " + m.getSsId());
            if (m.getSsStartTime() > m.getSsEndTime())
                throw new IncorrectParameterException(
                    "ssEndTime is small than ssStartTime (fxxk you): " + m.getSsId());
            if (!m.getSsYearEtTerm().matches("\\d{4}-\\d{4}-[12]"))
                throw new IncorrectParameterException(
                    "Illegal ssYearEtTerm: " + m.getSsId() + " " + m.getSsYearEtTerm());
            try {
                if (Integer.valueOf(m.getSsYearEtTerm().substring(0, 4)) >= Integer.valueOf(m.getSsYearEtTerm().substring(5, 9)))
                    throw new IncorrectParameterException(
                        "Illegal ssYearEtTerm endYear is equal or smaller" +
                            " than startYear: " + m.getSsId());
            } catch (NumberFormatException e) {
                throw new IncorrectParameterException(
                    "Illegal ssYearEtTerm: " + m.getSsId() + " " + m.getSsYearEtTerm());
            }
            if (m.getSsDayOfWeek() < 1 || m.getSsDayOfWeek() > 7)
                throw new IncorrectParameterException(
                    "Illegal ssDayOfWeek in ssId: " + m.getSsId());
            if (null != m.getSlId() && sisLocationList.stream().noneMatch(l -> l.getSlId().equals(m.getSlId())))
                throw new IncorrectParameterException(
                    "Illegal slId in ssId: " + m.getSsId() + " slId: " + m.getSlId());
            m.setScId(scId);
            m.setSsSuspension(stdSchedule.getSsSuspension());

            if (sisScheduleMapper.updateByPrimaryKey(m) <= 0)
                return false;
        }
        return true;
    }

    private boolean insertScheduleList(List<SisSchedule> nSisScheduleList,
                                       String scId) throws IncorrectParameterException {
        if (nSisScheduleList.isEmpty())
            return false;
        List<Integer> slIdList =
            nSisScheduleList.stream().map(SisSchedule::getSlId).filter(Objects::nonNull).collect(Collectors.toList());
        List<SisLocation> sisLocationList;
        sisLocationList = getSisLocations(slIdList);

        for (SisSchedule m : nSisScheduleList) {
            if (null == m.getSsStartWeek())
                throw new IncorrectParameterException(
                    "ssStartWeek can not be null: " + m.getSsId());
            if (null == m.getSsEndWeek())
                throw new IncorrectParameterException(
                    "ssEndWeek can not be null: " + m.getSsId());
            if (m.getSsStartWeek() < 1 || m.getSsStartWeek() > 25)
                throw new IncorrectParameterException(
                    "ssStartWeek can only between [1, 25]: " + m.getSsId());
            if (m.getSsEndWeek() < 1 || m.getSsEndWeek() > 25)
                throw new IncorrectParameterException(
                    "ssEndWeek can only between [1, 25]: " + m.getSsId());
            if (m.getSsStartWeek() > m.getSsEndWeek())
                throw new IncorrectParameterException(
                    "ssEndWeek is small than ssStartWeek (fxxk you): " + m.getSsId());
            if (null == m.getSsFortnight())
                throw new IncorrectParameterException(
                    "ssFortNight can not be null: " + m.getSsId());
            try {
                SisSchedule.SsFortnight.valueOf(m.getSsFortnight());
            } catch (IndexOutOfBoundsException e) {
                throw new IncorrectParameterException(
                    "Illegal ssFortNight: " + m.getSsId() + " , message: "
                        + e.getMessage());
            }
            if (null == m.getSsStartTime())
                throw new IncorrectParameterException(
                    "ssStartTime can not be null: " + m.getSsId());
            if (null == m.getSsEndTime())
                throw new IncorrectParameterException(
                    "ssEndTime can not be null: " + m.getSsId());
            if (m.getSsStartTime() < 1 || m.getSsStartTime() > 12)
                throw new IncorrectParameterException(
                    "ssStartTime can only between [1, 12]: " + m.getSsId());
            if (m.getSsEndTime() < 1 || m.getSsEndTime() > 25)
                throw new IncorrectParameterException(
                    "ssEndTime can only between [1, 12]: " + m.getSsId());
            if (m.getSsStartTime() > m.getSsEndTime())
                throw new IncorrectParameterException(
                    "ssEndTime is small than ssStartTime (fxxk you): " + m.getSsId());
            if (!m.getSsYearEtTerm().matches("\\d{4}-\\d{4}-[12]"))
                throw new IncorrectParameterException(
                    "Illegal ssYearEtTerm: " + m.getSsId() + " " + m.getSsYearEtTerm());
            try {
                if (Integer.valueOf(m.getSsYearEtTerm().substring(0, 4)) >= Integer.valueOf(m.getSsYearEtTerm().substring(5, 9)))
                    throw new IncorrectParameterException(
                        "Illegal ssYearEtTerm endYear is equal or smaller" +
                            " than startYear: " + m.getSsId());
            } catch (NumberFormatException e) {
                throw new IncorrectParameterException(
                    "Illegal ssYearEtTerm: " + m.getSsId() + " " + m.getSsYearEtTerm());
            }
            if (m.getSsDayOfWeek() < 1 || m.getSsDayOfWeek() > 7)
                throw new IncorrectParameterException(
                    "Illegal ssDayOfWeek in ssId: " + m.getSsId());
            if (null != m.getSlId() && sisLocationList.stream().noneMatch(l -> l.getSlId().equals(m.getSlId())))
                throw new IncorrectParameterException(
                    "Illegal slId in ssId: " + m.getSsId() + " slId: " + m.getSlId());
            m.setScId(scId);
            m.setSsSuspension("");
        }
        return sisScheduleMapper.insertList(nSisScheduleList) <= 0;
    }

    private List<SisLocation> getSisLocations(List<Integer> slIdList) {
        List<SisLocation> sisLocationList;
        if (slIdList.isEmpty())
            sisLocationList = new ArrayList<>();
        else {
            SisLocationExample sisLocationExample =
                new SisLocationExample();
            sisLocationExample.createCriteria().andSlIdIn(slIdList);
            sisLocationList =
                sisLocationMapper.selectByExample(sisLocationExample);
        }
        return sisLocationList;
    }

    public VoidOperationResponse modifyDepartment(Integer sdId,
                                                  String sdName) throws IncorrectParameterException {
        if ("".equals(sdName.trim()))
            throw new IncorrectParameterException("sdName can not be blank");
        SisDepartment sisDepartment =
            sisDepartmentMapper.selectByPrimaryKey(sdId);
        if (null == sisDepartment)
            throw new IncorrectParameterException("department not found: " + sdId);

        sisDepartment.setSdName(sdName.trim());
        sisDepartmentMapper.updateByPrimaryKey(sisDepartment);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public VoidOperationResponse deleteDepartment(Integer sdId) throws IncorrectParameterException {
        SisDepartment sisDepartment =
            sisDepartmentMapper.selectByPrimaryKey(sdId);
        if (null == sisDepartment)
            throw new IncorrectParameterException("department not found: " + sdId);
        sisDepartmentMapper.deleteByPrimaryKey(sdId);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public VoidOperationResponse addDepartment(String sdName) throws IncorrectParameterException {
        if ("".equals(sdName.trim()))
            throw new IncorrectParameterException("sdName can not be blank");
        SisDepartment sisDepartment = new SisDepartment();
        sisDepartment.setSdName(sdName);
        sisDepartmentMapper.insert(sisDepartment);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public VoidOperationResponse modifyJoinCourses(String scId,
                                                   List<SisJoinCourse> sisJoinCourseList) throws IncorrectParameterException, UnknownServerError {
        SisCourse sisCourse = sisCourseMapper.selectByPrimaryKey(scId);
        if (null == sisCourse)
            throw new IncorrectParameterException("course not found: " + scId);

        List<String> suIdList =
            sisJoinCourseList.stream()
                .filter(sjc -> null != sjc.getSuId())
                .map(SisJoinCourse::getSuId)
                .collect(Collectors.toList());
        List<SisUser> sisUserList;
        if (suIdList.isEmpty()) {
            sisUserList = new ArrayList<>();
        } else {
            SisUserExample sisUserExample = new SisUserExample();
            sisUserExample.createCriteria().andSuIdIn(suIdList);
            sisUserList = sisUserMapper.selectByExample(sisUserExample);
        }

        sisJoinCourseList =
            sisJoinCourseList.stream()
                .filter(sjc -> null != sjc.getSuId())
                .peek(sjc -> {
                    sjc.setJoinCourseType(SisJoinCourse.JoinCourseType.ATTENDANCE.ordinal());
                    sjc.setScId(scId);
                })
                .distinct()
                .collect(Collectors.toList());

        List<SisJoinCourse> mSisJoinCourseList =
            sisJoinCourseList.stream().filter(sjc -> null != sjc.getSjcId()).collect(Collectors.toList());

        //delete
        if (!mSisJoinCourseList.isEmpty()) {
            SisJoinCourseExample sisJoinCourseExample =
                new SisJoinCourseExample();
            SisJoinCourseExample.Criteria criteria =
                sisJoinCourseExample.createCriteria();
            criteria.andScIdEqualTo(scId)
                .andJoinCourseTypeEqualTo(SisJoinCourse.JoinCourseType.ATTENDANCE.ordinal());

            List<Integer> sjcList =
                mSisJoinCourseList.stream().map(SisJoinCourse::getSjcId).collect(Collectors.toList());
            if (!sjcList.isEmpty())
                criteria.
                    andSjcIdNotIn(mSisJoinCourseList.stream().map(SisJoinCourse::getSjcId).collect(Collectors.toList()));
            if (sisJoinCourseMapper.deleteByExample(sisJoinCourseExample) < 0)
                throw new UnknownServerError("delete join course error");
        }

        //add
        List<SisJoinCourse> nSisJoinCourseList =
            sisJoinCourseList.stream().filter(sjc -> null == sjc.getSjcId()).collect(Collectors.toList());
        if (!nSisJoinCourseList.isEmpty()) {
            if (nSisJoinCourseList.stream()
                .anyMatch(sjc -> sisUserList.stream()
                    .noneMatch(u -> u.getSuId().equals(sjc.getSuId())))) {
                throw new IncorrectParameterException("found invalid suId");
            }

            sisJoinCourseMapper.insertList(nSisJoinCourseList);
        }

        return VoidSuccessOperationResponse.SUCCESS;
    }
}
