package com.luxpmsoft.luxaipoc.utils

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    var _context: Context? = context
    var PRIVATE_MODE = 0
    private val PREF_NAME = "luxpmsoft"
    var pref: SharedPreferences? = _context!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    var editor: SharedPreferences.Editor? = pref!!.edit();

    fun setToken(token: String) {
        editor!!.putString("token", token)
        editor!!.commit()
    }

    fun setUser(user: String) {
        editor!!.putString("user", user)
        editor!!.commit()
    }

    fun setFullName(fullName: String) {
        editor!!.putString("fullName", fullName)
        editor!!.commit()
    }

    fun setUserId(userId: String) {
        editor!!.putString("userId", userId)
        editor!!.commit()
    }

    fun setUserType(userId: String) {
        editor!!.putString("userType", userId)
        editor!!.commit()
    }

    fun setSubscriptionName(subscriptionName: String) {
        editor!!.putString("subscriptionName", subscriptionName)
        editor!!.commit()
    }

    fun setHasSub(hasSub: String?) {
        editor!!.putString("hasSub", hasSub)
        editor!!.commit()
    }

    fun setOrganizationId(organizationId: String?) {
        editor!!.putString("organizationId", organizationId)
        editor!!.commit()
    }

    fun setProfileImageKey(profileImageKey: String) {
        editor!!.putString("profileImageKey", profileImageKey)
        editor!!.commit()
    }

    fun setOrganizationRole(organizationRole: String) {
        editor!!.putString("organizationRole", organizationRole)
        editor!!.commit()
    }

    fun setTotalNotification(totalNotification: String) {
        editor!!.putString("totalNotification", totalNotification)
        editor!!.commit()
    }

    fun setLocale(languageCode: String) {
        editor!!.putString("locale", languageCode)
        editor!!.commit()
    }

    fun getToken() : String{
        return pref!!.getString("token", "").toString()
    }

    fun getUserId() : String{
        return pref!!.getString("userId", "").toString()
    }

    fun getUserType() : String{
        return pref!!.getString("userType", "").toString()
    }

    fun getSubscriptionName() : String{
        return pref!!.getString("subscriptionName", "").toString()
    }

    fun getUser() : String{
        return pref!!.getString("user", "").toString()
    }

    fun getHasSub(): String {
        return pref!!.getString("hasSub", "").toString()
    }

    fun getOrganizationId(): String {
        return pref!!.getString("organizationId", "").toString()
    }

    fun getOrganizationRole(): String {
        return pref!!.getString("organizationRole", "").toString()
    }

    fun getProfileImageKey(): String {
        return pref!!.getString("profileImageKey", "").toString()
    }

    fun getTotalNotification(): String {
        return pref!!.getString("totalNotification", "").toString()
    }

    fun getLocale(): String {
        return  pref!!.getString("locale", "").toString()
    }

    fun getFullName(): String {
        return  pref!!.getString("fullName", "").toString()
    }
}