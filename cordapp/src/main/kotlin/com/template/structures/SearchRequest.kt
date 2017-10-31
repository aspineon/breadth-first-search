package com.template.structures

import net.corda.core.crypto.SecureHash
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class SearchRequest<T : BilateralAgreement>(val id: SecureHash, val type: Class<T>, private val depth: Int = 1) {
    fun propagate() = copy(depth = depth + 1)
}