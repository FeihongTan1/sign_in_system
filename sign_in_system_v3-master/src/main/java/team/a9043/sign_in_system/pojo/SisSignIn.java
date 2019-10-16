package team.a9043.sign_in_system.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import team.a9043.sign_in_system.convertor.Byte2ImgSerializer;

import java.time.LocalDateTime;
import java.util.List;

public class SisSignIn {
    private Integer ssiId;

    private Integer ssId;

    private Integer ssiWeek;

    private LocalDateTime ssiCreateTime;

    private Double ssiAttRate;

    @JsonSerialize(using = Byte2ImgSerializer.class)
    private byte[] ssiPicture;

    public Integer getSsiId() {
        return ssiId;
    }

    public void setSsiId(Integer ssiId) {
        this.ssiId = ssiId;
    }

    public Integer getSsId() {
        return ssId;
    }

    public void setSsId(Integer ssId) {
        this.ssId = ssId;
    }

    public Integer getSsiWeek() {
        return ssiWeek;
    }

    public void setSsiWeek(Integer ssiWeek) {
        this.ssiWeek = ssiWeek;
    }

    public LocalDateTime getSsiCreateTime() {
        return ssiCreateTime;
    }

    public void setSsiCreateTime(LocalDateTime ssiCreateTime) {
        this.ssiCreateTime = ssiCreateTime;
    }

    public Double getSsiAttRate() {
        return ssiAttRate;
    }

    public void setSsiAttRate(Double ssiAttRate) {
        this.ssiAttRate = ssiAttRate;
    }

    public byte[] getSsiPicture() {
        return ssiPicture;
    }

    public void setSsiPicture(byte[] ssiPicture) {
        this.ssiPicture = ssiPicture;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SisSignInDetail> sisSignInDetailList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SisSchedule sisSchedule;

    public List<SisSignInDetail> getSisSignInDetailList() {
        return sisSignInDetailList;
    }

    public void setSisSignInDetailList(List<SisSignInDetail> sisSignInDetailList) {
        this.sisSignInDetailList = sisSignInDetailList;
    }

    public SisSchedule getSisSchedule() {
        return sisSchedule;
    }

    public void setSisSchedule(SisSchedule sisSchedule) {
        this.sisSchedule = sisSchedule;
    }
}