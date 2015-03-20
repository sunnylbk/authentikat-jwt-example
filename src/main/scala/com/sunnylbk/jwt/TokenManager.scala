package com.sunnylbk.jwt

import java.util.UUID

import authentikat.jwt.{JwtClaimsSet, JwtHeader, JsonWebToken}

object TokenManager {

  lazy val secretKey = UUID.randomUUID().toString
  
  def createToken(username: String): String = {
    JsonWebToken(JwtHeader("HS256"), JwtClaimsSet(Map("u" -> username)), secretKey)
  }
  
  def validateToken(jwt: String): Boolean = {
    try {
      JsonWebToken.validate(jwt, secretKey)
    } catch {
      case _: Throwable => false
    }
  }
  
  def parseTokenClaimUsername(jwt: String): Option[String] = {
    jwt match {
      case JsonWebToken(header, claimSet, signature) => 
        Some(claimSet.asSimpleMap.get("u"))
      case x => None
    }
  }
}
