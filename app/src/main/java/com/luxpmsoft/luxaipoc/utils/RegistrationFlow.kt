package com.luxpmsoft.luxaipoc.utils

sealed class RegistrationFlow(val destination: String) {
    object AddOrganization : RegistrationFlow("add")
    object JoinOrganization : RegistrationFlow("join")
    object CreatedAccount : RegistrationFlow("created")
}

const val DESTINATION = "destination"
const val ORGANIZATION_NAME = "organization_name"
