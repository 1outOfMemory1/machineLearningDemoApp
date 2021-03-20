package tech.haonan.server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Jwt 的工具类
 */

@Component
public class JwtUtils {

    private static final String CLAIM_KEY_USERNAME="sub";
    private static final String CLAIM_KEY_CREATED="created_time";

    @Value("${jwt.tokenHeader}")
    private String abc;


    @Value("${jwt.secret}")
    private  String SIGNATURE;  // 从配置文件中读取加密字符串
    @Value("${jwt.expiration}")
    private  Integer EXPIRATION;  //从配置文件中读取过期时间


    /**
     * 根据用户信息生成token
     * @param userDetails
     * @return
     */
    public  String getToken(UserDetails userDetails){
        Map<String, String> claims = new HashMap<>();
        // 用户名 与 创建时间
        claims.put(CLAIM_KEY_USERNAME,userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED,new Date().toString());
        return generateToken(claims);
    }






    /**
     * 根据token获取用户名  如果获取不到用户名 说明token失效 直接返回前端 token失效
     * @param token
     * @return
     */
    public  String getUsernameFromToken(String token){
        String username;
        Map<String, DecodedJWT> claimsFromTokenMap = getClaimsFromToken(token);
        if (claimsFromTokenMap.get("token有效")!= null){
            username = claimsFromTokenMap.get("token有效").getClaim(CLAIM_KEY_USERNAME).asString();
            return username;
        }else
            return null;
    }


    /**
     * 生产token
     * @param claims
     * @return
     */
    private  String generateToken(Map<String,String> claims) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, EXPIRATION);

        JWTCreator.Builder builder = JWT.create();
        claims.forEach(builder::withClaim);
        builder.withExpiresAt(instance.getTime()); //指定过期时间
        return builder.sign(Algorithm.HMAC256(SIGNATURE));
    }


    /**
     * 获取Claims
     * @param token
     * @return
     */
    private  Map<String,DecodedJWT> getClaimsFromToken(String token){
        Map<String,DecodedJWT> map = new HashMap<>();
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SIGNATURE)).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            map.put("token有效",verify);
        } catch (TokenExpiredException e) {
            map.put("token过期",null);
            e.printStackTrace();
        } catch (AlgorithmMismatchException e) {
            map.put( "算法不一致",null);
            e.printStackTrace();
        } catch (Exception e) {
            map.put( "token无效",null);
        }
        return map;
    }


}

/**
 * 生成token
 */
class JwtByHand{
    /**
     * 根据用户信息生成token 手动方式
     * @param
     * @return
     */
    public  String getTokenByHand(String claim_key_username,String  username,
                                  String claim_key_created,String created_time,
                                  Integer expiration,String signature){
        Map<String, String> claims = new HashMap<>();
        // 用户名 与 创建时间
        claims.put(claim_key_username,username);
        claims.put(claim_key_created,created_time);
        return generateTokenByHand(claims,expiration,signature);
    }


    private  String generateTokenByHand(Map<String,String> claims,Integer expiration,String signature) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, expiration);

        JWTCreator.Builder builder = JWT.create();
        claims.forEach(builder::withClaim);
        builder.withExpiresAt(instance.getTime()); //指定过期时间
        return builder.sign(Algorithm.HMAC256(signature));
    }
}

class Main{
    public static void main(String[] args) {
        JwtByHand jwtByHand =  new JwtByHand();

        String CLAIM_KEY_USERNAME="sub";
        String CLAIM_KEY_CREATED="created_time";
        String SIGNATURE = "mysecret4445";  // 从配置文件中读取加密字符串
        Integer EXPIRATION =31530000;  //从配置文件中读取过期时间
        String str = jwtByHand.getTokenByHand(CLAIM_KEY_USERNAME,"admin",CLAIM_KEY_CREATED,
                new Date().toString(),EXPIRATION,SIGNATURE);
        System.out.println(str);;
    }
}