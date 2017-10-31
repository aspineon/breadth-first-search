package com.template.structures

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class SearchResult(val agreements: Set<BilateralAgreement>)