package com.template.services

import net.corda.core.crypto.SecureHash
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class PersistentState(val services: ServiceHub) : SingletonSerializeAsToken() {
    val seenRequests: MutableSet<SecureHash> = mutableSetOf()
}