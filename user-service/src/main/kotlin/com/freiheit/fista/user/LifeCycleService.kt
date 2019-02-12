package com.freiheit.fista.user

enum class Readiness {
    READY,
    STARTING,
    FAILURE
}

enum class Healthiness {
    HEALTHY,
    FAILURE
}

interface ReadinessProbe {
    fun ready(): Readiness
}

interface HealthinessProbe {
    fun healthy(): Healthiness
}

/**
 * Each micro service must always be able to report its readiness (important during rolling out) and its healthiness
 * (important during lifetime). This service is a simple version of a [LifeCycleService] that takes a set of
 * readiness- and healthiness-probes.
 */
class LifeCycleService(
    private val readinessProbes: Set<ReadinessProbe> = emptySet(),
    private val healthinessProbes: Set<HealthinessProbe> = emptySet()
) : ReadinessProbe, HealthinessProbe {

    /**
     * Returns only ready if all [readinessProbes] are ready.
     */
    override fun ready(): Readiness {
        return when {
            readinessProbes.any { Readiness.FAILURE == it.ready() } -> Readiness.FAILURE
            readinessProbes.any { Readiness.STARTING == it.ready() } -> Readiness.STARTING
            else -> Readiness.READY
        }
    }

    /**
     * Returns only healthy if all [healthinessProbes] are ready.
     */
    override fun healthy(): Healthiness {
        return when {
            healthinessProbes.any { Healthiness.FAILURE == it.healthy() } -> Healthiness.FAILURE
            else -> Healthiness.HEALTHY
        }
    }
}