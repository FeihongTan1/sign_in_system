package team.a9043.sign_in_system.pojo;

public class SisUserInfo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sis_user_info.su_id
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    private String suId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sis_user_info.sui_lack_num
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    private Integer suiLackNum;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sis_user_info.sui_lock_grade
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    private Integer suiLockGrade;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sis_user_info.su_id
     *
     * @return the value of sis_user_info.su_id
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    public String getSuId() {
        return suId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sis_user_info.su_id
     *
     * @param suId the value for sis_user_info.su_id
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    public void setSuId(String suId) {
        this.suId = suId == null ? null : suId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sis_user_info.sui_lack_num
     *
     * @return the value of sis_user_info.sui_lack_num
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    public Integer getSuiLackNum() {
        return suiLackNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sis_user_info.sui_lack_num
     *
     * @param suiLackNum the value for sis_user_info.sui_lack_num
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    public void setSuiLackNum(Integer suiLackNum) {
        this.suiLackNum = suiLackNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sis_user_info.sui_lock_grade
     *
     * @return the value of sis_user_info.sui_lock_grade
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    public Integer getSuiLockGrade() {
        return suiLockGrade;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sis_user_info.sui_lock_grade
     *
     * @param suiLockGrade the value for sis_user_info.sui_lock_grade
     *
     * @mbg.generated Sun Oct 07 22:29:39 CST 2018
     */
    public void setSuiLockGrade(Integer suiLockGrade) {
        this.suiLockGrade = suiLockGrade;
    }
}