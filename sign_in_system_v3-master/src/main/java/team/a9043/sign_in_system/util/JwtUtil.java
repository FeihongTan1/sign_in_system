package team.a9043.sign_in_system.util;

import io.jsonwebtoken.*;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Token Util
 *
 * @author a9043
 */
public class JwtUtil {
    private static SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    private static String stringKey = "a9043_knowledge_sharing";
    private static byte[] encodedKey = Base64.encodeBase64(stringKey.getBytes());
    private static SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

    /**
     * 生成Token
     *
     * @param claims 声明
     * @return token
     */
    public static String createJWT(Map<String, Object> claims) {
        return createJWT(claims, Calendar.HOUR, 2);
    }

    /**
     * 自定义过期时间Token
     *
     * @param claims       声明
     * @param expireField  时间位
     * @param expireAmount 时间长
     * @return token
     */
    public static String createJWT(Map<String, Object> claims, int expireField, int expireAmount) {
        Date now = Calendar.getInstance().getTime();
        Calendar expireCal = Calendar.getInstance();
        expireCal.add(expireField, expireAmount);
        Date expire = expireCal.getTime();

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setHeaderParam("alg", "HS512")
                .setIssuedAt(now)
                .setClaims(claims)
                .setIssuer("a9043")
                .setExpiration(expire)
                .signWith(signatureAlgorithm, key);
        return builder.compact();
    }

    /**
     * Token parser
     *
     * @param JwtStr token
     * @return claims
     * @throws SignatureException  err
     * @throws ExpiredJwtException err
     */
    public static Claims parseJwt(String JwtStr) throws SignatureException, ExpiredJwtException {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(JwtStr)
                .getBody();
        return Optional
                .ofNullable(claims)
                .filter(claim -> claim.getExpiration() == null || !new Date().after(claim.getExpiration()))
                .orElse(null);
    }
}
